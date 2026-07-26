package com.asensiodev.carbura.core.domain.sync

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId

enum class LocalDataDecision {
    Import,
    Exclude,
}

data class LocalDataCounts(
    val vehicles: Int,
    val maintenanceRecords: Int,
    val reminders: Int,
) {
    val total: Int = vehicles + maintenanceRecords + reminders
}

data class LocalDataSnapshot(
    val digest: String,
    val counts: LocalDataCounts,
)

interface LocalDataAdoptionGateway {
    fun unresolvedSnapshot(): LocalDataSnapshot?

    fun decision(
        userId: UserId,
        familyId: FamilyId,
        snapshotDigest: String,
    ): LocalDataDecision?

    suspend fun import(
        userId: UserId,
        familyId: FamilyId,
        approvedSnapshotDigest: String,
    )

    suspend fun exclude(
        userId: UserId,
        familyId: FamilyId,
        approvedSnapshotDigest: String,
    )

    fun cancel() = Unit
}
