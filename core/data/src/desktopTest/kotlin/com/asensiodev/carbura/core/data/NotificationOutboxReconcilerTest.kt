package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationAction
import com.asensiodev.carbura.core.model.FamilyId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationOutboxReconcilerTest {
    @Test
    fun legacyRemindersAreClassifiedOnce() =
        runTest {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            try {
                CarburaDatabase.Schema.create(driver)
                val database = CarburaDatabase(driver)
                val scope = database.activateTestFamily(FamilyId("family"))
                insertReminder(database, id = "active", isCompleted = 0, deletedAt = null, dueDate = "2027-07-01")
                insertReminder(database, id = "completed", isCompleted = 1, deletedAt = null, dueDate = "2027-07-01")
                insertReminder(database, id = "deleted", isCompleted = 0, deletedAt = 2, dueDate = "2027-07-01")
                insertReminder(database, id = "unscheduled", isCompleted = 0, deletedAt = null, dueDate = null)
                val reconciler = NotificationOutboxReconciler(database)
                val outbox = SqlDelightNotificationOutbox(database)

                reconciler.reconcileExistingReminders(scope)

                assertEquals(
                    listOf(
                        DesiredNotificationAction.Schedule,
                        DesiredNotificationAction.Cancel,
                        DesiredNotificationAction.Cancel,
                        DesiredNotificationAction.Cancel,
                    ),
                    outbox.pending(scope).map { it.action },
                )
                outbox.pending(scope).forEach { outbox.acknowledge(scope, it.reminderId, it.revision) }

                reconciler.reconcileExistingReminders(scope)

                assertTrue(outbox.pending(scope).isEmpty())
            } finally {
                driver.close()
            }
        }

    private fun insertReminder(
        database: CarburaDatabase,
        id: String,
        isCompleted: Long,
        deletedAt: Long?,
        dueDate: String?,
    ) {
        database.carburaDatabaseQueries.upsertReminder(
            id = id,
            familyId = "family",
            vehicleId = "vehicle",
            maintenanceTypeId = null,
            title = id,
            dueDate = dueDate,
            dueOdometerKm = null,
            notifyDaysBefore = 7,
            isCompleted = isCompleted,
            updatedAt = 1,
            pendingSync = 0,
            deletedAt = deletedAt,
        )
    }
}
