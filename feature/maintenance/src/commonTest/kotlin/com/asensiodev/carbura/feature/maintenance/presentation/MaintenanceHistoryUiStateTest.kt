package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.VehicleId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaintenanceHistoryUiStateTest {
    @Test
    fun loadingStateIsNotEmptyState() {
        assertFalse(MaintenanceHistoryUiState(performedOn = TODAY).isEmpty)
    }

    @Test
    fun emptyStateRequiresNoRecordsAndNotLoading() {
        assertTrue(MaintenanceHistoryUiState(performedOn = TODAY, loadState = MaintenanceLoadState.Content).isEmpty)
    }

    @Test
    fun blankSearchPreservesCompleteSourceOrder() {
        val records =
            listOf(record("new", MaintenanceTypeCode.OilChange, "2026-07-01"), record("old", MaintenanceTypeCode.Itv, "2025-01-10"))

        assertEquals(records, state(records, "   ").visibleRecords)
    }

    @Test
    fun searchMatchesSupportedFieldsCaseInsensitively() {
        val oil = record("oil", MaintenanceTypeCode.OilChange, "2026-07-01", workshop = "Central Garage")
        val custom =
            record(
                "custom",
                MaintenanceTypeCode.Custom,
                "2025-01-10",
                label = "eBike ECU Check",
                notes = "Firmware updated",
                nextDueDate = "2028-02-20",
            )
        val records = listOf(oil, custom)

        assertEquals(listOf(oil), state(records, "OIL CHANGE").visibleRecords)
        assertEquals(listOf(oil), state(records, "central").visibleRecords)
        assertEquals(listOf(custom), state(records, "ecu").visibleRecords)
        assertEquals(listOf(custom), state(records, "firmware").visibleRecords)
        assertEquals(listOf(custom), state(records, "2025-01").visibleRecords)
        assertEquals(listOf(custom), state(records, "2028-02").visibleRecords)
    }

    @Test
    fun noMatchIsDistinctFromSourceEmpty() {
        val filtered = state(listOf(record("record", MaintenanceTypeCode.Repair, "2026-07-01")), "insurance")

        assertFalse(filtered.isEmpty)
        assertTrue(filtered.hasNoMatchingRecords)
        assertEquals(emptyList(), filtered.visibleRecords)
    }

    private fun state(
        records: List<MaintenanceRecord>,
        query: String,
    ) = MaintenanceHistoryUiState(
        performedOn = TODAY,
        records = records,
        searchQuery = query,
        loadState = MaintenanceLoadState.Content,
    )

    private fun record(
        id: String,
        type: MaintenanceTypeCode,
        performedOn: String,
        label: String? = null,
        workshop: String? = null,
        notes: String? = null,
        nextDueDate: String? = null,
    ) = MaintenanceRecord(
        id = MaintenanceRecordId(id),
        familyId = FamilyId("family"),
        vehicleId = VehicleId("vehicle"),
        maintenanceTypeId = MaintenanceTypeId("type-${type.name}"),
        maintenanceTypeCode = type,
        maintenanceTypeLabel = label,
        performedOn = CalendarDate(performedOn),
        workshop = workshop,
        notes = notes,
        nextDueDate = nextDueDate?.let(::CalendarDate),
    )

    private companion object {
        const val TODAY = "2026-07-17"
    }
}
