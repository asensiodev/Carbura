package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotification
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationAction
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationPayload
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutbox
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxDrainResult
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationPermissionDeniedException
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationRevision
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlert
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationOutboxProcessorTest {
    @Test
    fun scheduleAndCancelActionsAreAppliedAndAcknowledged() =
        runTest {
            val outbox = FakeNotificationOutbox(scheduleDesired(), cancelDesired())
            val scheduler = RecordingOutboxScheduler()

            val result = NotificationOutboxProcessor(outbox, scheduler).drain(testFamilyScope)

            assertEquals(NotificationOutboxDrainResult.Drained, result)
            assertEquals(listOf(ReminderId("schedule-1")), scheduler.scheduled.map { it.reminder.id })
            assertEquals(listOf(ReminderId("cancel-1")), scheduler.cancelled)
            assertTrue(outbox.pending(testFamilyScope).isEmpty())
        }

    @Test
    fun ordinarySchedulerFailureLeavesActionPendingForReplay() =
        runTest {
            val desired = scheduleDesired()
            val outbox = FakeNotificationOutbox(desired)
            val scheduler = RecordingOutboxScheduler(scheduleError = IllegalStateException("Temporary failure"))

            val result = NotificationOutboxProcessor(outbox, scheduler).drain(testFamilyScope)

            assertEquals(NotificationOutboxDrainResult.RetryableWorkRemaining, result)
            assertEquals(listOf(desired), outbox.pending(testFamilyScope))
        }

    @Test
    fun permissionFailureIsClassifiedWithoutAcknowledgement() =
        runTest {
            val desired = scheduleDesired()
            val outbox = FakeNotificationOutbox(desired)
            val scheduler = RecordingOutboxScheduler(scheduleError = NotificationPermissionDeniedException("Denied"))

            assertEquals(
                NotificationOutboxDrainResult.PermissionDenied,
                NotificationOutboxProcessor(outbox, scheduler).drain(testFamilyScope),
            )
            assertEquals(listOf(desired), outbox.pending(testFamilyScope))
        }

    @Test
    fun cancellationBeforeApplicationLeavesActionPending() =
        runTest {
            val desired = scheduleDesired()
            val outbox = FakeNotificationOutbox(desired)
            val scheduler = RecordingOutboxScheduler(scheduleError = CancellationException("Cancelled"))

            assertFailsWith<CancellationException> { NotificationOutboxProcessor(outbox, scheduler).drain(testFamilyScope) }

            assertEquals(listOf(desired), outbox.pending(testFamilyScope))
        }

    @Test
    fun cancellationAfterApplicationBeforeAcknowledgementReplaysIdempotently() =
        runTest {
            val desired = scheduleDesired()
            val outbox = FakeNotificationOutbox(desired, acknowledgeError = CancellationException("Cancelled"))
            val scheduler = RecordingOutboxScheduler()
            val processor = NotificationOutboxProcessor(outbox, scheduler)

            assertFailsWith<CancellationException> { processor.drain(testFamilyScope) }
            assertEquals(listOf(desired), outbox.pending(testFamilyScope))
            outbox.acknowledgeError = null

            val recreatedProcessor = NotificationOutboxProcessor(outbox, scheduler)
            assertEquals(NotificationOutboxDrainResult.Drained, recreatedProcessor.drain(testFamilyScope))
            assertEquals(2, scheduler.scheduled.size)
            assertEquals(desired.revision, scheduler.scheduled.last().revision)
            assertTrue(outbox.pending(testFamilyScope).isEmpty())
        }

    @Test
    fun concurrentDrainsAreSerialized() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val outbox = FakeNotificationOutbox(scheduleDesired())
            val scheduler = RecordingOutboxScheduler(scheduleGate = gate)
            val processor = NotificationOutboxProcessor(outbox, scheduler)

            val first = async { processor.drain(testFamilyScope) }
            val second = async { processor.drain(testFamilyScope) }
            runCurrent()
            assertEquals(1, scheduler.scheduleAttempts)

            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(NotificationOutboxDrainResult.Drained, first.await())
            assertEquals(NotificationOutboxDrainResult.Drained, second.await())
            assertEquals(1, scheduler.scheduleAttempts)
        }

    private fun scheduleDesired(): DesiredNotification {
        val reminder = reminder("schedule-1")
        return DesiredNotification(
            reminderId = reminder.id,
            action = DesiredNotificationAction.Schedule,
            payload = DesiredNotificationPayload(reminder, listOf(ReminderAlert(ReminderAlertKind.Manual, 7))),
            revision = NotificationRevision(1),
        )
    }

    private fun cancelDesired(): DesiredNotification =
        DesiredNotification(
            reminderId = ReminderId("cancel-1"),
            action = DesiredNotificationAction.Cancel,
            payload = null,
            revision = NotificationRevision(1),
        )

    private fun reminder(id: String): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = FamilyId("family-1"),
            vehicleId = VehicleId("vehicle-1"),
            maintenanceTypeId = null,
            title = "ITV",
            dueDate = CalendarDate("2026-09-01"),
        )
}

private class FakeNotificationOutbox(
    vararg desired: DesiredNotification,
    var acknowledgeError: Throwable? = null,
) : NotificationOutbox {
    private val rows = desired.associateByTo(mutableMapOf(), DesiredNotification::reminderId)

    override suspend fun pending(scope: com.asensiodev.carbura.core.model.ActiveFamilyScope): List<DesiredNotification> =
        rows.values.toList()

    override suspend fun acknowledge(
        scope: com.asensiodev.carbura.core.model.ActiveFamilyScope,
        reminderId: ReminderId,
        revision: NotificationRevision,
    ) {
        acknowledgeError?.let { throw it }
        if (rows[reminderId]?.revision == revision) rows.remove(reminderId)
    }
}

private class RecordingOutboxScheduler(
    private val scheduleError: Throwable? = null,
    private val scheduleGate: CompletableDeferred<Unit>? = null,
) : ReminderNotificationScheduler {
    val scheduled = mutableListOf<ReminderNotificationPlan>()
    val cancelled = mutableListOf<ReminderId>()
    var scheduleAttempts = 0

    override suspend fun schedule(
        scope: com.asensiodev.carbura.core.model.ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) {
        scheduleAttempts += 1
        scheduleGate?.await()
        scheduleError?.let { throw it }
        scheduled += plan
    }

    override suspend fun cancel(
        scope: com.asensiodev.carbura.core.model.ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        cancelled += reminderId
    }
}
