package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.plannedMaintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.deriveGeneratedMaintenanceReminder
import com.asensiodev.carbura.core.domain.reminder.usecase.derivePlannedMaintenanceReminder
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.VehicleId

data class UpdateMaintenanceRecordInput(
    val scope: ActiveFamilyScope,
    val recordId: MaintenanceRecordId,
    val expectedFamilyId: FamilyId,
    val expectedVehicleId: VehicleId,
    val maintenanceTypeCode: MaintenanceTypeCode,
    val customTypeLabel: String?,
    val performedOn: String,
    val odometerKm: String,
    val cost: String,
    val workshop: String,
    val notes: String,
    val nextDueDate: String,
    val currentDate: CalendarDate,
)

sealed interface UpdateMaintenanceRecordResult {
    data class Success(
        val record: MaintenanceRecord,
        val reminderRetained: Boolean,
    ) : UpdateMaintenanceRecordResult

    data class ValidationError(
        val reason: ValidationFailure,
    ) : UpdateMaintenanceRecordResult

    data object NotFound : UpdateMaintenanceRecordResult
}

class UpdateMaintenanceRecordUseCase(
    private val maintenanceRepository: MaintenanceRecordRepository,
    private val reminderRepository: ReminderRepository,
) {
    suspend operator fun invoke(input: UpdateMaintenanceRecordInput): UpdateMaintenanceRecordResult {
        if (input.expectedFamilyId != input.scope.familyId) return UpdateMaintenanceRecordResult.NotFound
        val existing =
            maintenanceRepository.getActiveMaintenanceRecord(
                input.recordId,
                input.scope,
                input.expectedVehicleId,
            ) ?: return UpdateMaintenanceRecordResult.NotFound
        val parsed =
            CreateMaintenanceRecordInput(
                id = existing.id,
                familyId = existing.familyId,
                vehicleId = existing.vehicleId,
                performedOn = input.performedOn,
                odometerKm = input.odometerKm,
                cost = input.cost,
                workshop = input.workshop,
                notes = input.notes,
                maintenanceTypeCode = input.maintenanceTypeCode,
                customTypeLabel = input.customTypeLabel,
                nextDueDate = input.nextDueDate,
            ).toMaintenanceRecord()
        val candidate =
            when (parsed) {
                is DomainResult.Success -> parsed.value.copy(currency = existing.currency)
                is DomainResult.ValidationError -> return UpdateMaintenanceRecordResult.ValidationError(parsed.reason)
            }

        val generated = deriveGeneratedMaintenanceReminder(candidate)
        val mutations =
            mutableListOf<ReminderNotificationMutation>(
                generated?.let { ReminderNotificationMutation.Upsert(it.reminder, it.notificationPlan) }
                    ?: ReminderNotificationMutation.Delete(maintenanceReminderId(candidate.id)),
            )
        val plannedId = plannedMaintenanceReminderId(candidate.id)
        val existingPlanned = reminderRepository.getActiveReminder(input.scope, plannedId)
        var plannedRetained = false
        if (existingPlanned != null) {
            if (candidate.performedOn > input.currentDate) {
                val planned = derivePlannedMaintenanceReminder(candidate)
                mutations += ReminderNotificationMutation.Upsert(planned, manualReminderNotificationPlan(planned))
                plannedRetained = true
            } else {
                mutations += ReminderNotificationMutation.Delete(plannedId)
            }
        }

        val updated =
            maintenanceRepository.updateMaintenanceRecordWithNotifications(
                record = candidate,
                scope = input.scope,
                expectedVehicleId = input.expectedVehicleId,
                mutations = mutations,
            )
        return if (updated) {
            UpdateMaintenanceRecordResult.Success(candidate, generated != null || plannedRetained)
        } else {
            UpdateMaintenanceRecordResult.NotFound
        }
    }
}
