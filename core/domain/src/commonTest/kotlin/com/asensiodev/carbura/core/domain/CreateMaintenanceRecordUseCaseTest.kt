package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateMaintenanceRecordUseCaseTest {
    @Test
    fun validMaintenanceRecordIsSaved() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            val useCase = CreateMaintenanceRecordUseCase(repository)
            val record = testMaintenanceRecord()

            val result = useCase(record)

            assertEquals(DomainResult.Success(record), result)
            assertEquals(listOf(record), repository.savedRecords)
        }

    @Test
    fun negativeMaintenanceOdometerIsRejected() =
        runTest {
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
    fun negativeMaintenanceCostIsRejected() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            val useCase = CreateMaintenanceRecordUseCase(repository)

            val result = useCase(testMaintenanceRecord(costCents = -1))

            assertEquals(
                DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceCost),
                result,
            )
            assertTrue(repository.savedRecords.isEmpty())
        }

    @Test
    fun unsupportedTypeNormalizesNextDueDateBeforePersistence() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            val useCase = CreateMaintenanceRecordUseCase(repository)
            val input = testMaintenanceRecord(code = MaintenanceTypeCode.Repair, nextDueDate = "2027-07-01")

            val result = useCase(input)

            val expected = input.copy(nextDueDate = null)
            assertEquals(DomainResult.Success(expected), result)
            assertEquals(listOf(expected), repository.savedRecords)
        }
}
