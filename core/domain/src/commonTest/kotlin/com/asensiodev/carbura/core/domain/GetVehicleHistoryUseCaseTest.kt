package com.asensiodev.carbura.core.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetVehicleHistoryUseCaseTest {
    @Test
    fun historyIsReturnedNewestFirst() = runTest {
        val repository = FakeMaintenanceRecordRepository()
        val older = testMaintenanceRecord(id = "older", performedOn = "2025-01-10")
        val newer = testMaintenanceRecord(id = "newer", performedOn = "2026-02-15")
        repository.savedRecords += listOf(older, newer)
        val useCase = GetVehicleHistoryUseCase(repository)

        val history = useCase(testVehicleId)

        assertEquals(listOf(newer, older), history)
    }
}
