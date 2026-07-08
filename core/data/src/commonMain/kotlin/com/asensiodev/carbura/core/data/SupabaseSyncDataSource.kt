package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.FamilyId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

internal class SupabaseSyncDataSource(
    private val client: SupabaseClient,
) : RemoteSyncDataSource {
    override suspend fun upsertVehicles(vehicles: List<SyncVehicle>) {
        if (vehicles.isEmpty()) return
        client.from("vehicles").upsert(vehicles.map { it.toRemoteDto() })
    }

    override suspend fun upsertMaintenanceRecords(records: List<SyncMaintenanceRecord>) {
        if (records.isEmpty()) return
        client.from("maintenance_records").upsert(records.map { it.toRemoteDto() })
    }

    override suspend fun upsertReminders(reminders: List<SyncReminder>) {
        if (reminders.isEmpty()) return
        client.from("reminders").upsert(reminders.map { it.toRemoteDto() })
    }

    override suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle> =
        client.from("vehicles")
            .select {
                filter { eq("family_id", familyId.value) }
                order("updated_at", Order.ASCENDING)
            }
            .decodeList<RemoteVehicleDto>()
            .map { it.toSyncVehicle() }

    override suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord> =
        client.from("maintenance_records")
            .select {
                filter { eq("family_id", familyId.value) }
                order("updated_at", Order.ASCENDING)
            }
            .decodeList<RemoteMaintenanceRecordDto>()
            .map { it.toSyncMaintenanceRecord() }

    override suspend fun getReminders(familyId: FamilyId): List<SyncReminder> =
        client.from("reminders")
            .select {
                filter { eq("family_id", familyId.value) }
                order("updated_at", Order.ASCENDING)
            }
            .decodeList<RemoteReminderDto>()
            .map { it.toSyncReminder() }
}
