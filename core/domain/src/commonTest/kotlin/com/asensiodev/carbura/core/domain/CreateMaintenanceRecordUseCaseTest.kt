package com.asensiodev.carbura.core.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateMaintenanceRecordUseCaseTest {
    @Test
    fun validMaintenanceRecordIsSaved() = runTest {
        val repository = FakeMaintenanceRecordRepository()
        val useCase = CreateMaintenanceRecordUseCase(repository)
        val record = testMaintenanceRecord()

        val result = useCase(record)

        assertEquals(DomainResult.Success(record), result)
        assertEquals(listOf(record), repository.savedRecords)
    }

    @Test
    fun negativeMaintenanceOdometerIsRejected() = runTest {
        val repository = FakeMaintenanceRecordRepository()
        val useCase = CreateMaintenanceRecordUseCase(repository)

        val result = useCase(testMaintenanceRecord(odometerKm = -1))

        assertEquals(
            DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceOdometer),
            result,
        )
        assertTrue(repository.savedRecords.isEmpty())
    }

    @Test
    fun negativeMaintenanceCostIsRejected() = runTest {
        val repository = FakeMaintenanceRecordRepository()
        val useCase = CreateMaintenanceRecordUseCase(repository)

        val result = useCase(testMaintenanceRecord(costCents = -1))

        assertEquals(
            DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceCost),
            result,
        )
        assertTrue(repository.savedRecords.isEmpty())
    }
}
