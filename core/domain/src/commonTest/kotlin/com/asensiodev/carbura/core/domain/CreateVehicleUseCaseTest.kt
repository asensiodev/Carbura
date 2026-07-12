package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.vehicle.usecase.CreateVehicleUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateVehicleUseCaseTest {
    @Test
    fun validVehicleIsSaved() =
        runTest {
            val repository = FakeVehicleRepository()
            val useCase = CreateVehicleUseCase(repository)
            val vehicle = testVehicle()

            val result = useCase(vehicle)

            assertEquals(DomainResult.Success(vehicle), result)
            assertEquals(listOf(vehicle), repository.savedVehicles)
        }

    @Test
    fun blankVehicleNameIsRejected() =
        runTest {
            val repository = FakeVehicleRepository()
            val useCase = CreateVehicleUseCase(repository)

            val result = useCase(testVehicle(name = "  "))

            assertEquals(
                DomainResult.ValidationError(ValidationFailure.BlankVehicleName),
                result,
            )
            assertTrue(repository.savedVehicles.isEmpty())
        }

    @Test
    fun negativeOdometerIsRejected() =
        runTest {
            val repository = FakeVehicleRepository()
            val useCase = CreateVehicleUseCase(repository)

            val result = useCase(testVehicle(odometerKm = -1))

            assertEquals(
                DomainResult.ValidationError(ValidationFailure.NegativeVehicleOdometer),
                result,
            )
            assertTrue(repository.savedVehicles.isEmpty())
        }
}
