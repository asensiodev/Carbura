package com.asensiodev.carbura.core.data

import android.content.Context
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
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
