package com.asensiodev.carbura.core.data

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.coredata.R
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidReminderNotificationReceiverTest {
    @Test
    fun alarmValidationRequiresAnActiveReminderWithMatchingRevision() {
        val context: Context = RuntimeEnvironment.getApplication()
        val driver = AndroidSqliteDriver(CarburaDatabase.Schema, context, "receiver-validation.db")
        val database = CarburaDatabase(driver)
        val scope = ActiveFamilyScope(UserId("user"), FamilyId("family"), 2)
        val queries = database.carburaDatabaseQueries
        queries.upsertReminder(
            id = "active",
            familyId = "family",
            vehicleId = "vehicle",
            maintenanceTypeId = null,
            title = "Active",
            dueDate = "2027-07-01",
            dueOdometerKm = null,
            notifyDaysBefore = 7,
            isCompleted = 0,
            updatedAt = 1,
            pendingSync = 0,
            deletedAt = null,
        )
        queries.replaceNotificationRevision("family", "active", 2)

        assertTrue(isCurrentReminderAlarm(database, scope, scope, "active", 2))
        assertFalse(isCurrentReminderAlarm(database, scope, scope, "active", 1))
        assertFalse(isCurrentReminderAlarm(database, scope, scope, "missing", 2))
        assertFalse(isCurrentReminderAlarm(database, scope.copy(generation = 3), scope, "active", 2))

        queries.markReminderCompleted(updatedAt = 2, familyId = "family", id = "active")
        assertFalse(isCurrentReminderAlarm(database, scope, scope, "active", 2))

        queries.upsertReminder(
            id = "deleted",
            familyId = "family",
            vehicleId = "vehicle",
            maintenanceTypeId = null,
            title = "Deleted",
            dueDate = "2027-07-01",
            dueOdometerKm = null,
            notifyDaysBefore = 7,
            isCompleted = 0,
            updatedAt = 1,
            pendingSync = 0,
            deletedAt = 2,
        )
        queries.replaceNotificationRevision("family", "deleted", 1)
        assertFalse(isCurrentReminderAlarm(database, scope, scope, "deleted", 1))
        driver.close()
        context.deleteDatabase("receiver-validation.db")
    }

    @Test
    fun isoExpirationDateIsParsedAndFormattedForLocale() {
        assertEquals("Jul 1, 2027", localizedExpirationDate("2027-07-01", Locale.US))
        assertFalse(localizedExpirationDate("2027-07-01", Locale.US).orEmpty().contains("2027-07-01"))
    }

    @Test
    fun malformedOrMissingExpirationDateReturnsNoFormattedDate() {
        assertNull(localizedExpirationDate("2027-02-30", Locale.US))
        assertNull(localizedExpirationDate("", Locale.US))
    }

    @Test
    fun generatedCopyFallsBackSafelyWhenExpirationDateIsMalformed() {
        val context: Context = RuntimeEnvironment.getApplication()

        val (_, body) = notificationCopy(context, ReminderAlertKind.Insurance45Days, "bad-date", "Fallback")

        assertTrue(body.contains(context.getString(R.string.reminder_notification_unknown_date)))
        assertFalse(body.contains("bad-date"))
    }
}
