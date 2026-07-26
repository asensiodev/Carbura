package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.sync.LocalDataAdoptionGateway
import com.asensiodev.carbura.core.domain.sync.LocalDataCounts
import com.asensiodev.carbura.core.domain.sync.LocalDataDecision
import com.asensiodev.carbura.core.domain.sync.LocalDataSnapshot
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.VehicleId

internal class SqlDelightLocalDataAdoptionGateway(
    private val database: CarburaDatabase,
    private val remote: RemoteSyncDataSource? = null,
) : LocalDataAdoptionGateway {
    private val notificationPayloadCodec = DesiredNotificationPayloadCodec()

    override fun unresolvedSnapshot(): LocalDataSnapshot? {
        val vehicles = database.carburaDatabaseQueries.selectUnresolvedLocalVehicles().executeAsList()
        val maintenance = database.carburaDatabaseQueries.selectUnresolvedLocalMaintenanceRecords().executeAsList()
        val reminders = database.carburaDatabaseQueries.selectUnresolvedLocalReminders().executeAsList()
        if (vehicles.isEmpty() && maintenance.isEmpty() && reminders.isEmpty()) return null
        val stableRows =
            buildList {
                vehicles.forEach { add(stableRow("vehicle", it.id, it.familyId, it.deletedAt, it.updatedAt, it.pendingSync)) }
                maintenance.forEach { add(stableRow("maintenance", it.id, it.familyId, it.deletedAt, it.updatedAt, it.pendingSync)) }
                reminders.forEach { add(stableRow("reminder", it.id, it.familyId, it.deletedAt, it.updatedAt, it.pendingSync)) }
            }.sorted().joinToString("\n")
        return LocalDataSnapshot(
            digest = stableSha256(stableRows),
            counts = LocalDataCounts(vehicles.size, maintenance.size, reminders.size),
        )
    }

    override fun decision(
        userId: UserId,
        familyId: FamilyId,
        snapshotDigest: String,
    ): LocalDataDecision? =
        database.carburaDatabaseQueries
            .selectLocalDataDecision(userId.value, familyId.value, snapshotDigest)
            .executeAsOneOrNull()
            ?.let(LocalDataDecision::valueOf)

    override suspend fun import(
        userId: UserId,
        familyId: FamilyId,
        approvedSnapshotDigest: String,
    ) {
        val remoteIds = remoteTargetIds(familyId)
        database.carburaDatabaseQueries.transaction {
            if (decision(userId, familyId, approvedSnapshotDigest) == LocalDataDecision.Import) return@transaction
            requireSnapshot(approvedSnapshotDigest)
            adopt(familyId, approvedSnapshotDigest, remoteIds)
            recordDecision(userId, familyId, approvedSnapshotDigest, LocalDataDecision.Import)
        }
    }

    override suspend fun exclude(
        userId: UserId,
        familyId: FamilyId,
        approvedSnapshotDigest: String,
    ) {
        val remoteIds = remoteTargetIds(familyId)
        database.carburaDatabaseQueries.transaction {
            requireSnapshot(approvedSnapshotDigest)
            remapExcluded(approvedSnapshotDigest, remoteIds)
            recordDecision(userId, familyId, approvedSnapshotDigest, LocalDataDecision.Exclude)
            unresolvedSnapshot()?.digest?.let { resultingDigest ->
                recordDecision(userId, familyId, resultingDigest, LocalDataDecision.Exclude)
            }
        }
    }

    private fun requireSnapshot(expectedDigest: String) {
        require(unresolvedSnapshot()?.digest == expectedDigest) { "Local data changed after approval" }
    }

    private fun adopt(
        familyId: FamilyId,
        digest: String,
        remoteIds: RemoteTargetIds,
    ) {
        val queries = database.carburaDatabaseQueries
        val vehicles = queries.selectUnresolvedLocalVehicles().executeAsList()
        val maintenance = queries.selectUnresolvedLocalMaintenanceRecords().executeAsList()
        val reminders = queries.selectUnresolvedLocalReminders().executeAsList()
        val vehicleIds = vehicles.associate { row ->
            row.id to collisionSafeId("vehicle", row.id, familyId, digest) {
                it in remoteIds.vehicleIds || queries.selectVehicleByFamilyAndId(familyId.value, it).executeAsOneOrNull() != null
            }
        }
        val maintenanceIds = maintenance.associate { row ->
            row.id to collisionSafeId("maintenance", row.id, familyId, digest) {
                it in remoteIds.maintenanceRecordIds ||
                    queries.selectMaintenanceRecordByFamilyAndId(familyId.value, it).executeAsOneOrNull() != null ||
                    "maintenance-reminder:$it" in remoteIds.reminderIds ||
                    "planned-maintenance-reminder:$it" in remoteIds.reminderIds ||
                    queries.selectReminderByFamilyAndId(familyId.value, "maintenance-reminder:$it").executeAsOneOrNull() != null ||
                    queries.selectReminderByFamilyAndId(familyId.value, "planned-maintenance-reminder:$it").executeAsOneOrNull() != null
            }
        }
        val generatedReminderIds = buildMap {
            maintenanceIds.forEach { (oldId, newId) ->
                if (oldId != newId) {
                    put("maintenance-reminder:$oldId", "maintenance-reminder:$newId")
                    put("planned-maintenance-reminder:$oldId", "planned-maintenance-reminder:$newId")
                }
            }
        }
        val reminderIds = reminders.associate { row ->
            val generated = generatedReminderIds[row.id]
            row.id to if (generated != null) {
                generated
            } else {
                collisionSafeId("reminder", row.id, familyId, digest) {
                    it in remoteIds.reminderIds || queries.selectReminderByFamilyAndId(familyId.value, it).executeAsOneOrNull() != null
                }
            }
        }
        vehicles.forEach { row ->
            queries.moveVehicleToFamily(vehicleIds.getValue(row.id), familyId.value, LOCAL_FAMILY_ID.value, row.id)
        }
        maintenance.forEach { row ->
            queries.moveMaintenanceRecordToFamily(
                maintenanceIds.getValue(row.id), familyId.value, vehicleIds[row.vehicleId] ?: row.vehicleId,
                LOCAL_FAMILY_ID.value, row.id,
            )
        }
        reminders.forEach { row ->
            val newId = reminderIds.getValue(row.id)
            val newVehicleId = vehicleIds[row.vehicleId] ?: row.vehicleId
            queries.moveReminderToFamily(newId, familyId.value, newVehicleId, LOCAL_FAMILY_ID.value, row.id)
            moveNotificationOutbox(LOCAL_FAMILY_ID, row.id, familyId, newId, newVehicleId)
            queries.moveNotificationRevision(familyId.value, newId, LOCAL_FAMILY_ID.value, row.id)
        }
    }

    private fun remapExcluded(
        digest: String,
        remoteIds: RemoteTargetIds,
    ) {
        val queries = database.carburaDatabaseQueries
        val vehicles = queries.selectUnresolvedLocalVehicles().executeAsList()
        val maintenance = queries.selectUnresolvedLocalMaintenanceRecords().executeAsList()
        val reminders = queries.selectUnresolvedLocalReminders().executeAsList()
        val localVehicleIds = vehicles.mapTo(mutableSetOf()) { it.id }
        val localMaintenanceIds = maintenance.mapTo(mutableSetOf()) { it.id }
        val localReminderIds = reminders.mapTo(mutableSetOf()) { it.id }
        val vehicleIds = vehicles.associate { row ->
            row.id to excludedCollisionSafeId("vehicle", row.id, digest, remoteIds.vehicleIds, localVehicleIds)
        }
        val maintenanceIds = maintenance.associate { row ->
            row.id to excludedCollisionSafeId("maintenance", row.id, digest, remoteIds.maintenanceRecordIds, localMaintenanceIds)
        }
        val generatedReminderIds = buildMap {
            maintenanceIds.forEach { (oldId, newId) ->
                if (oldId != newId) {
                    put("maintenance-reminder:$oldId", "maintenance-reminder:$newId")
                    put("planned-maintenance-reminder:$oldId", "planned-maintenance-reminder:$newId")
                }
            }
        }
        val reminderIds = reminders.associate { row ->
            val preferredId = generatedReminderIds[row.id] ?: row.id
            row.id to excludedCollisionSafeId("reminder", preferredId, digest, remoteIds.reminderIds, localReminderIds - row.id)
        }
        vehicles.forEach { row ->
            queries.moveVehicleToFamily(vehicleIds.getValue(row.id), LOCAL_FAMILY_ID.value, LOCAL_FAMILY_ID.value, row.id)
        }
        maintenance.forEach { row ->
            queries.moveMaintenanceRecordToFamily(
                maintenanceIds.getValue(row.id),
                LOCAL_FAMILY_ID.value,
                vehicleIds[row.vehicleId] ?: row.vehicleId,
                LOCAL_FAMILY_ID.value,
                row.id,
            )
        }
        reminders.forEach { row ->
            val newId = reminderIds.getValue(row.id)
            queries.moveReminderToFamily(
                newId,
                LOCAL_FAMILY_ID.value,
                vehicleIds[row.vehicleId] ?: row.vehicleId,
                LOCAL_FAMILY_ID.value,
                row.id,
            )
            moveNotificationOutbox(LOCAL_FAMILY_ID, row.id, LOCAL_FAMILY_ID, newId, vehicleIds[row.vehicleId] ?: row.vehicleId)
            queries.moveNotificationRevision(LOCAL_FAMILY_ID.value, newId, LOCAL_FAMILY_ID.value, row.id)
        }
    }

    private fun moveNotificationOutbox(
        oldFamilyId: FamilyId,
        oldReminderId: String,
        newFamilyId: FamilyId,
        newReminderId: String,
        newVehicleId: String,
    ) {
        val queries = database.carburaDatabaseQueries
        val outbox = queries.selectDesiredNotificationById(oldFamilyId.value, oldReminderId).executeAsOneOrNull()
        val payload =
            outbox?.payload?.let(notificationPayloadCodec::decode)?.let { existing ->
                notificationPayloadCodec.encode(
                    existing.copy(
                        reminder =
                            existing.reminder.copy(
                                id = ReminderId(newReminderId),
                                familyId = newFamilyId,
                                vehicleId = VehicleId(newVehicleId),
                            ),
                    ),
                )
            }
        queries.moveNotificationOutbox(
            newFamilyId.value,
            newReminderId,
            payload,
            oldFamilyId.value,
            oldReminderId,
        )
    }

    private fun excludedCollisionSafeId(
        kind: String,
        preferredId: String,
        digest: String,
        remoteIds: Set<String>,
        localIds: Set<String>,
    ): String {
        if (preferredId !in remoteIds && preferredId !in localIds) return preferredId
        if (preferredId in localIds && preferredId !in remoteIds) return preferredId
        val prefix = "legacy-${stableSha256("$kind|excluded|$digest|$preferredId").take(16)}"
        var candidate = "$prefix:$preferredId"
        var suffix = 1
        while (candidate in remoteIds || candidate in localIds) candidate = "$prefix:${suffix++}:$preferredId"
        return candidate
    }

    private suspend fun remoteTargetIds(familyId: FamilyId): RemoteTargetIds {
        val source = remote ?: return RemoteTargetIds()
        return RemoteTargetIds(
            vehicleIds = source.getVehicles(familyId).mapTo(mutableSetOf()) { it.id },
            maintenanceRecordIds = source.getMaintenanceRecords(familyId).mapTo(mutableSetOf()) { it.id },
            reminderIds = source.getReminders(familyId).mapTo(mutableSetOf()) { it.id },
        )
    }

    private fun collisionSafeId(
        kind: String,
        originalId: String,
        familyId: FamilyId,
        digest: String,
        collides: (String) -> Boolean,
    ): String {
        val prefix = "legacy-${stableSha256("$kind|${familyId.value}|$digest|$originalId").take(32)}"
        var candidate = "$prefix:$originalId"
        var suffix = 1
        while (collides(candidate)) candidate = "$prefix:${suffix++}:$originalId"
        return candidate
    }

    private fun recordDecision(
        userId: UserId,
        familyId: FamilyId,
        digest: String,
        decision: LocalDataDecision,
    ) {
        database.carburaDatabaseQueries.insertLocalDataDecision(userId.value, familyId.value, digest, decision.name)
    }
}

private data class RemoteTargetIds(
    val vehicleIds: Set<String> = emptySet(),
    val maintenanceRecordIds: Set<String> = emptySet(),
    val reminderIds: Set<String> = emptySet(),
)

private fun stableRow(
    kind: String,
    id: String,
    familyId: String,
    deletedAt: Long?,
    updatedAt: Long,
    pendingSync: Long,
): String = listOf(kind, id, familyId, deletedAt?.toString() ?: "null", updatedAt.toString(), pendingSync.toString()).joinToString("|") { value ->
    "${value.length}:$value"
}
