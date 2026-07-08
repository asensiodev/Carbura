package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
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
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalRepositoriesTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")

    @Test
    fun vehicleRepositoryReadsSavedVehiclesFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val vehicleRepository = LocalVehicleRepository(firstDatabase)
        vehicleRepository.saveVehicle(
            Vehicle(
                id = vehicleId,
                familyId = familyId,
                name = "Coche familiar",
                type = VehicleType.Car,
                currentOdometerKm = 12000,
            ),
        )

        val recreatedRepository = LocalVehicleRepository(recreatedDatabase)

        val vehicles = recreatedRepository.observeVehicles(familyId)
        assertEquals(1, vehicles.size)
        assertEquals("Coche familiar", vehicles.single().name)
        assertEquals(12000, vehicles.single().currentOdometerKm)
    }

    @Test
    fun maintenanceRepositoryReadsHistoryOrderedByDateFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val repository = LocalMaintenanceRecordRepository(firstDatabase)
        repository.saveMaintenanceRecord(record("old", "2026-01-01"))
        repository.saveMaintenanceRecord(record("new", "2026-07-04"))

        val recreatedRepository = LocalMaintenanceRecordRepository(recreatedDatabase)

        val history = recreatedRepository.getVehicleHistory(vehicleId)
        assertEquals(listOf("new", "old"), history.map { it.id.value })
    }

    @Test
    fun maintenanceRepositoryDeletesRecordFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val repository = LocalMaintenanceRecordRepository(firstDatabase)
        repository.saveMaintenanceRecord(record("oil", "2026-07-04"))
        repository.deleteMaintenanceRecord(MaintenanceRecordId("oil"))

        val recreatedRepository = LocalMaintenanceRecordRepository(recreatedDatabase)

        assertEquals(emptyList(), recreatedRepository.getVehicleHistory(vehicleId))
    }

    @Test
    fun reminderRepositoryReadsPendingRemindersFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val repository = LocalReminderRepository(firstDatabase)
        repository.saveReminder(reminder("late", dueDate = "2026-08-01"))
        repository.saveReminder(reminder("early", dueDate = "2026-07-01"))

        val recreatedRepository = LocalReminderRepository(recreatedDatabase)

        val reminders = recreatedRepository.getPendingReminders(familyId)
        assertEquals(listOf("early", "late"), reminders.map { it.id.value })
    }

    @Test
    fun reminderRepositoryHidesCompletedRemindersFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val repository = LocalReminderRepository(firstDatabase)
        repository.saveReminder(reminder("completed", dueOdometerKm = 20000))
        repository.markReminderCompleted(ReminderId("completed"))

        val recreatedRepository = LocalReminderRepository(recreatedDatabase)

        assertEquals(emptyList(), recreatedRepository.getPendingReminders(familyId))
    }

    @Test
    fun reminderRepositoryDeletesReminderFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val repository = LocalReminderRepository(firstDatabase)
        repository.saveReminder(reminder("itv", dueDate = "2026-08-01"))
        repository.deleteReminder(ReminderId("itv"))

        val recreatedRepository = LocalReminderRepository(recreatedDatabase)

        assertEquals(emptyList(), recreatedRepository.getPendingReminders(familyId))
    }

    @Test
    fun deletingVehicleRemovesVehicleMaintenanceAndReminders() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val vehicleRepository = LocalVehicleRepository(firstDatabase)
        val maintenanceRepository = LocalMaintenanceRecordRepository(firstDatabase)
        val reminderRepository = LocalReminderRepository(firstDatabase)
        vehicleRepository.saveVehicle(
            Vehicle(
                id = vehicleId,
                familyId = familyId,
                name = "Moto",
                type = VehicleType.Motorcycle,
                currentOdometerKm = 3000,
            ),
        )
        maintenanceRepository.saveMaintenanceRecord(record("oil", "2026-07-01"))
        reminderRepository.saveReminder(reminder("itv", dueDate = "2026-08-01"))

        vehicleRepository.deleteVehicle(vehicleId)

        val recreatedVehicleRepository = LocalVehicleRepository(recreatedDatabase)
        val recreatedMaintenanceRepository = LocalMaintenanceRecordRepository(recreatedDatabase)
        val recreatedReminderRepository = LocalReminderRepository(recreatedDatabase)
        assertEquals(emptyList(), recreatedVehicleRepository.observeVehicles(familyId))
        assertEquals(emptyList(), recreatedMaintenanceRepository.getVehicleHistory(vehicleId))
        assertEquals(emptyList(), recreatedReminderRepository.getPendingReminders(familyId))
    }

    private fun runTestWithRecreatedDatabase(
        block: suspend (CarburaDatabase, CarburaDatabase) -> Unit,
    ) = kotlinx.coroutines.test.runTest {
        val databaseFile = File.createTempFile("carbura-test", ".db").also { it.deleteOnExit() }
        val firstDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        CarburaDatabase.Schema.create(firstDriver)
        val recreatedDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")

        block(CarburaDatabase(firstDriver), CarburaDatabase(recreatedDriver))
    }

    private fun record(id: String, performedOn: String): MaintenanceRecord = MaintenanceRecord(
        id = MaintenanceRecordId(id),
        familyId = familyId,
        vehicleId = vehicleId,
        maintenanceTypeId = MaintenanceTypeId("type-$id"),
        maintenanceTypeCode = MaintenanceTypeCode.Custom,
        performedOn = CalendarDate(performedOn),
        odometerKm = 1,
    )

    private fun reminder(
        id: String,
        dueDate: String? = null,
        dueOdometerKm: Int? = null,
    ): Reminder = Reminder(
        id = ReminderId(id),
        familyId = familyId,
        vehicleId = vehicleId,
        maintenanceTypeId = null,
        title = "Recordatorio $id",
        dueDate = dueDate?.let(::CalendarDate),
        dueOdometerKm = dueOdometerKm,
    )
}
