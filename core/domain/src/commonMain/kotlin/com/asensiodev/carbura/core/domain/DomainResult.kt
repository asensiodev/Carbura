package com.asensiodev.carbura.core.domain

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class ValidationError(val reason: ValidationFailure) : DomainResult<Nothing>
}

enum class ValidationFailure {
    BlankVehicleName,
    NegativeVehicleOdometer,
    BlankMaintenanceType,
    InvalidMaintenanceDate,
    InvalidMaintenanceCost,
    NegativeMaintenanceOdometer,
    NegativeMaintenanceCost,
    BlankReminderTitle,
    MissingReminderVehicle,
    MissingReminderDueTarget,
    NegativeReminderDueOdometer,
}
