package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationPayload
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlert
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class DesiredNotificationPayloadCodec(
    private val json: Json = Json,
) {
    fun encode(payload: DesiredNotificationPayload): String = json.encodeToString(payload.toDto())

    fun decode(value: String): DesiredNotificationPayload = json.decodeFromString<DesiredNotificationPayloadDto>(value).toDomain()
}

@Serializable
private data class DesiredNotificationPayloadDto(
    val reminderId: String,
    val familyId: String,
    val vehicleId: String,
    val maintenanceTypeId: String?,
    val title: String,
    val dueDate: String?,
    val dueOdometerKm: Int?,
    val notifyDaysBefore: Int,
    val isCompleted: Boolean,
    val alerts: List<ReminderAlertDto>,
)

@Serializable
private data class ReminderAlertDto(
    val kind: String,
    val daysBefore: Int,
)

private fun DesiredNotificationPayload.toDto(): DesiredNotificationPayloadDto =
    DesiredNotificationPayloadDto(
        reminderId = reminder.id.value,
        familyId = reminder.familyId.value,
        vehicleId = reminder.vehicleId.value,
        maintenanceTypeId = reminder.maintenanceTypeId?.value,
        title = reminder.title,
        dueDate = reminder.dueDate?.iso8601,
        dueOdometerKm = reminder.dueOdometerKm,
        notifyDaysBefore = reminder.notifyDaysBefore,
        isCompleted = reminder.isCompleted,
        alerts = alerts.map { ReminderAlertDto(it.kind.name, it.daysBefore) },
    )

private fun DesiredNotificationPayloadDto.toDomain(): DesiredNotificationPayload =
    DesiredNotificationPayload(
        reminder =
            Reminder(
                id = ReminderId(reminderId),
                familyId = FamilyId(familyId),
                vehicleId = VehicleId(vehicleId),
                maintenanceTypeId = maintenanceTypeId?.let(::MaintenanceTypeId),
                title = title,
                dueDate = dueDate?.let(::CalendarDate),
                dueOdometerKm = dueOdometerKm,
                notifyDaysBefore = notifyDaysBefore,
                isCompleted = isCompleted,
            ),
        alerts = alerts.map { ReminderAlert(ReminderAlertKind.valueOf(it.kind), it.daysBefore) },
    )
