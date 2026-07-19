package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CancellationConvergenceTest {
    @Test
    fun manualReminderCommitSurvivesCancellationAndReplaysSameIdentity() =
        withDatabase { database ->
            val reminder = reminder("manual")
            val repository = LocalReminderRepository(database, CancellingRecovery)

            assertFailsWith<CancellationException> {
                repository.saveReminderWithNotification(reminder, manualReminderNotificationPlan(reminder))
            }

            assertEquals(listOf(reminder), LocalReminderRepository(database).getPendingReminders(FAMILY_ID))
            val scheduler = RecordingScheduler()
            NotificationOutboxProcessor(SqlDelightNotificationOutbox(database), scheduler).drain()
            assertEquals(listOf(reminder.id), scheduler.scheduled.map { it.reminder.id })
            assertTrue(SqlDelightNotificationOutbox(database).pending().isEmpty())
        }

    @Test
    fun completionAndDeletionCommitsSurviveCancellationAndReplayCancel() =
        withDatabase { database ->
            val first = reminder("complete")
            val second = reminder("delete")
            val setup = LocalReminderRepository(database)
            setup.saveReminder(first)
            setup.saveReminder(second)
            val repository = LocalReminderRepository(database, CancellingRecovery)

            assertFailsWith<CancellationException> { repository.markReminderCompletedWithNotification(first.id) }
            assertFailsWith<CancellationException> { repository.deleteReminderWithNotification(second.id) }

            val rows = database.carburaDatabaseQueries.selectSyncRemindersByFamily(FAMILY_ID.value).executeAsList()
            assertEquals(1L, rows.single { it.id == first.id.value }.isCompleted)
            assertNotNull(rows.single { it.id == second.id.value }.deletedAt)
            val scheduler = RecordingScheduler()
            NotificationOutboxProcessor(SqlDelightNotificationOutbox(database), scheduler).drain()
            assertEquals(setOf(first.id, second.id), scheduler.cancelled.toSet())
        }

    @Test
    fun maintenanceCreationAndDeletionCommitsSurviveCancellation() =
        withDatabase { database ->
            val record = maintenanceRecord()
            val generated = reminder("maintenance-reminder")
            val repository = LocalMaintenanceRecordRepository(database, CancellingRecovery)

            assertFailsWith<CancellationException> {
                repository.saveMaintenanceRecordWithNotification(
                    record,
                    ReminderNotificationMutation.Upsert(generated, manualReminderNotificationPlan(generated)),
                )
            }
            val scheduleScheduler = RecordingScheduler()
            NotificationOutboxProcessor(SqlDelightNotificationOutbox(database), scheduleScheduler).drain()
            assertEquals(listOf(generated.id), scheduleScheduler.scheduled.map { it.reminder.id })

            assertFailsWith<CancellationException> {
                repository.deleteMaintenanceRecordWithNotifications(record.id, listOf(generated.id))
            }
            val cancelScheduler = RecordingScheduler()
            NotificationOutboxProcessor(SqlDelightNotificationOutbox(database), cancelScheduler).drain()
            assertEquals(listOf(generated.id), cancelScheduler.cancelled)
            assertTrue(LocalMaintenanceRecordRepository(database).getVehicleHistory(VEHICLE_ID).isEmpty())
        }

    @Test
    fun proactiveVehicleReconciliationAndDeletionSurviveCancellation() =
        withDatabase { database ->
            val vehicle = vehicle()
            val itv = reminder("vehicle-reminder:vehicle:itv")
            val service = reminder("vehicle-reminder:vehicle:service", dueDate = null)
            val repository = LocalVehicleRepository(database, CancellingRecovery)

            assertFailsWith<CancellationException> {
                repository.saveVehicleWithNotifications(
                    vehicle,
                    listOf(
                        ReminderNotificationMutation.Upsert(itv, manualReminderNotificationPlan(itv)),
                        ReminderNotificationMutation.Upsert(service, null),
                        ReminderNotificationMutation.Delete(ReminderId("vehicle-reminder:vehicle:insurance")),
                    ),
                )
            }
            val reconciliationScheduler = RecordingScheduler()
            NotificationOutboxProcessor(SqlDelightNotificationOutbox(database), reconciliationScheduler).drain()
            assertEquals(listOf(itv.id), reconciliationScheduler.scheduled.map { it.reminder.id })
            assertEquals(2, reconciliationScheduler.cancelled.size)

            assertFailsWith<CancellationException> { repository.deleteVehicleWithNotifications(vehicle.id) }
            val deletionScheduler = RecordingScheduler()
            NotificationOutboxProcessor(SqlDelightNotificationOutbox(database), deletionScheduler).drain()
            assertEquals(setOf(itv.id, service.id), deletionScheduler.cancelled.toSet())
            assertTrue(LocalVehicleRepository(database).observeVehicles(FAMILY_ID).isEmpty())
        }

    private fun withDatabase(block: suspend (CarburaDatabase) -> Unit) =
        runTest {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            try {
                CarburaDatabase.Schema.create(driver)
                block(CarburaDatabase(driver))
            } finally {
                driver.close()
            }
        }

    private fun reminder(
        id: String,
        dueDate: String? = "2027-07-01",
    ) = Reminder(
        id = ReminderId(id),
        familyId = FAMILY_ID,
        vehicleId = VEHICLE_ID,
        maintenanceTypeId = null,
        title = id,
        dueDate = dueDate?.let(::CalendarDate),
    )

    private fun maintenanceRecord() =
        MaintenanceRecord(
            id = MaintenanceRecordId("maintenance"),
            familyId = FAMILY_ID,
            vehicleId = VEHICLE_ID,
            maintenanceTypeId = MaintenanceTypeId("oil"),
            maintenanceTypeCode = MaintenanceTypeCode.OilChange,
            performedOn = CalendarDate("2026-07-01"),
            odometerKm = 10_000,
        )

    private fun vehicle() =
        Vehicle(
            id = VEHICLE_ID,
            familyId = FAMILY_ID,
            name = "Vehicle",
            type = VehicleType.Car,
            currentOdometerKm = 10_000,
        )

    private object CancellingRecovery : NotificationOutboxRecovery {
        override fun request(): Unit = throw CancellationException("Cancelled after commit")
    }

    private class RecordingScheduler : ReminderNotificationScheduler {
        val scheduled = mutableListOf<ReminderNotificationPlan>()
        val cancelled = mutableListOf<ReminderId>()

        override suspend fun schedule(plan: ReminderNotificationPlan) {
            scheduled += plan
        }

        override suspend fun cancel(reminderId: ReminderId) {
            cancelled += reminderId
        }
    }

    private companion object {
        val FAMILY_ID = FamilyId("family")
        val VEHICLE_ID = VehicleId("vehicle")
    }
}
