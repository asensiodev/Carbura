package com.asensiodev.carbura.core.model

@JvmInline
value class FamilyId(
    val value: String,
)

data class ActiveFamilyScope(
    val userId: UserId?,
    val familyId: FamilyId,
    val generation: Long,
)

@JvmInline
value class UserProfileId(
    val value: String,
)

@JvmInline
value class UserId(
    val value: String,
)

@JvmInline
value class VehicleId(
    val value: String,
)

@JvmInline
value class MaintenanceTypeId(
    val value: String,
)

@JvmInline
value class MaintenanceRecordId(
    val value: String,
)

@JvmInline
value class ReminderId(
    val value: String,
)
