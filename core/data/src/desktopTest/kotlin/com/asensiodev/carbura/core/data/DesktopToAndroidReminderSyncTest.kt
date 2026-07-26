package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationAction
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxDrainResult
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DesktopToAndroidReminderSyncTest {
    @Test
    fun desktopDateReminderReachesAndroidNotificationScheduler() =
        runTest {
            val desktopDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            val androidDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            try {
                CarburaDatabase.Schema.create(desktopDriver)
                CarburaDatabase.Schema.create(androidDriver)
                val desktopDatabase = CarburaDatabase(desktopDriver)
                val androidDatabase = CarburaDatabase(androidDriver)
                val desktopFamilyScope = SqlDelightActiveFamilyScopeGateway(desktopDatabase)
                val androidFamilyScope = SqlDelightActiveFamilyScopeGateway(androidDatabase)
                val desktopScope = desktopFamilyScope.activateAuthenticated(USER_ID, FAMILY_ID)
                val reminder =
                    Reminder(
                        id = ReminderId("desktop-reminder"),
                        familyId = FAMILY_ID,
                        vehicleId = VehicleId("vehicle"),
                        maintenanceTypeId = null,
                        title = "ITV",
                        dueDate = CalendarDate("2027-07-01"),
                        notifyDaysBefore = 7,
                    )
                LocalReminderRepository(desktopDatabase, familyScope = desktopFamilyScope)
                    .saveReminder(desktopScope, reminder)
                val remote = SharedRemoteState()

                assertIs<SyncResult.Success>(
                    syncManager(desktopDatabase, desktopFamilyScope, remote).syncNow(),
                )

                val uploaded = remote.reminders.single()
                assertEquals(reminder.id.value, uploaded.id)
                assertEquals(reminder.dueDate?.iso8601, uploaded.dueDate)

                assertIs<SyncResult.Success>(
                    syncManager(androidDatabase, androidFamilyScope, remote).syncNow(),
                )

                val androidScope = androidFamilyScope.current()
                val androidLocal = SqlDelightLocalSyncDataSource(androidDatabase, familyScope = androidFamilyScope)
                val pulled = androidLocal.getReminders(androidScope).single()
                assertEquals(reminder.id.value, pulled.id)
                assertEquals(reminder.dueDate?.iso8601, pulled.dueDate)
                val outbox = SqlDelightNotificationOutbox(androidDatabase, familyScope = androidFamilyScope)
                val desired = outbox.pending(androidScope).single()
                assertEquals(DesiredNotificationAction.Schedule, desired.action)
                assertEquals(reminder.id, desired.reminderId)
                assertEquals(reminder.dueDate, desired.payload?.reminder?.dueDate)

                val scheduler = RecordingReminderNotificationScheduler()
                assertEquals(
                    NotificationOutboxDrainResult.Drained,
                    NotificationOutboxProcessor(outbox, scheduler).drain(androidScope),
                )

                val dispatched = scheduler.scheduled.single()
                assertEquals(reminder.id, dispatched.reminder.id)
                assertEquals(reminder.dueDate, dispatched.reminder.dueDate)
                assertTrue(outbox.pending(androidScope).isEmpty())
            } finally {
                androidDriver.close()
                desktopDriver.close()
            }
        }

    private fun syncManager(
        database: CarburaDatabase,
        familyScope: SqlDelightActiveFamilyScopeGateway,
        remote: RemoteSyncDataSource,
    ) = LocalFirstSyncManager(
        authGateway = TestAuthGateway,
        profileGateway = TestProfileGateway,
        local = SqlDelightLocalSyncDataSource(database, familyScope = familyScope),
        remote = remote,
        familyScope = familyScope,
    )

    private object TestAuthGateway : AuthGateway {
        override suspend fun currentSession() = AuthSession("token", AuthUser(USER_ID.value, null, null))

        override suspend fun signInWithGoogle() = requireNotNull(currentSession())

        override suspend fun signInWithGoogle(idToken: String) = requireNotNull(currentSession())

        override suspend fun signOut() = Unit

        override suspend fun deleteAccount() = Unit
    }

    private object TestProfileGateway : RemoteUserProfileGateway {
        override suspend fun getProfileForUser(userId: UserId) = RemoteUserProfile(userId, FAMILY_ID, "Family", "User", null)

        override suspend fun ensureProfile(
            displayName: String,
            email: String?,
        ) = requireNotNull(getProfileForUser(USER_ID))
    }

    private class SharedRemoteState : RemoteSyncDataSource {
        val reminders = mutableListOf<SyncReminder>()

        override suspend fun upsertVehicles(vehicles: List<SyncVehicle>) = Unit

        override suspend fun upsertMaintenanceRecords(records: List<SyncMaintenanceRecord>) = Unit

        override suspend fun upsertReminders(reminders: List<SyncReminder>) {
            reminders.forEach { reminder ->
                this.reminders.removeAll { it.familyId == reminder.familyId && it.id == reminder.id }
                this.reminders += reminder
            }
        }

        override suspend fun getVehicles(familyId: FamilyId) = emptyList<SyncVehicle>()

        override suspend fun getMaintenanceRecords(familyId: FamilyId) = emptyList<SyncMaintenanceRecord>()

        override suspend fun getReminders(familyId: FamilyId) = reminders.filter { it.familyId == familyId.value }
    }

    private class RecordingReminderNotificationScheduler : ReminderNotificationScheduler {
        val scheduled = mutableListOf<ReminderNotificationPlan>()

        override suspend fun schedule(
            scope: ActiveFamilyScope,
            plan: ReminderNotificationPlan,
        ) {
            scheduled += plan
        }

        override suspend fun cancel(
            scope: ActiveFamilyScope,
            reminderId: ReminderId,
        ) = Unit
    }

    private companion object {
        val FAMILY_ID = FamilyId("family")
        val USER_ID = UserId("user")
    }
}
