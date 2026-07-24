package com.asensiodev.carbura.core.domain

sealed interface DomainResult<out T> {
    data class Success<T>(
        val value: T,
    ) : DomainResult<T>

    data class ValidationError(
        val reason: ValidationFailure,
    ) : DomainResult<Nothing>
}

enum class ValidationFailure {
    BlankVehicleName,
    InvalidVehicleOdometer,
    InvalidVehicleServiceOdometer,
    NegativeVehicleOdometer,
    NegativeVehicleServiceOdometer,
    BlankMaintenanceType,
    InvalidMaintenanceDate,
    InvalidMaintenancePerformedDate,
    InvalidMaintenanceNextDueDate,
    InvalidMaintenanceCost,
    InvalidMaintenanceOdometer,
    NegativeMaintenanceOdometer,
    NegativeMaintenanceCost,
    BlankReminderTitle,
    MissingReminderVehicle,
    MissingReminderDueTarget,
    InvalidReminderDueOdometer,
    NegativeReminderDueOdometer,
}
