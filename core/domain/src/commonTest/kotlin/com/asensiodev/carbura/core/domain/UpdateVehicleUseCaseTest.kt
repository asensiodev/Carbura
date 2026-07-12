package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleParams
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleResult
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleUseCase
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateVehicleUseCaseTest {
    @Test
    fun validEditPreservesIdentityAndSavesEditableValues() =
        runTest {
            val repository = FakeVehicleRepository()
            val original = testVehicle(name = "Original", odometerKm = 10_000)
            repository.saveVehicle(original)
            val useCase = UpdateVehicleUseCase(repository)

            val result =
                useCase(
                    UpdateVehicleParams(
                        currentVehicle = original,
                        name = "  Familiar  ",
                        type = VehicleType.Motorcycle,
                        licensePlate = " 1234 ABC ",
                        odometerKm = 12_000,
                    ),
                )

            val updated = (result as UpdateVehicleResult.Success).vehicle
            assertEquals(original.id, updated.id)
            assertEquals(original.familyId, updated.familyId)
            assertEquals("Familiar", updated.name)
            assertEquals(VehicleType.Motorcycle, updated.type)
            assertEquals("1234 ABC", updated.licensePlate)
            assertEquals(12_000, updated.currentOdometerKm)
            assertEquals(listOf(updated), repository.savedVehicles)
        }

    @Test
    fun blankNameIsRejectedWithoutSaving() =
        runTest {
            val repository = FakeVehicleRepository()
            val original = testVehicle()
            val useCase = UpdateVehicleUseCase(repository)

            val result = useCase(params(original, name = " "))

            assertEquals(
                UpdateVehicleResult.ValidationError(ValidationFailure.BlankVehicleName),
                result,
            )
            assertTrue(repository.savedVehicles.isEmpty())
        }

    @Test
    fun negativeOdometerIsRejectedWithoutSaving() =
        runTest {
            val repository = FakeVehicleRepository()
            val original = testVehicle()
            val useCase = UpdateVehicleUseCase(repository)

            val result = useCase(params(original, odometerKm = -1))

            assertEquals(
                UpdateVehicleResult.ValidationError(ValidationFailure.NegativeVehicleOdometer),
                result,
            )
            assertTrue(repository.savedVehicles.isEmpty())
        }

    @Test
    fun odometerIncreaseIsSavedWithoutConfirmation() =
        runTest {
            val repository = FakeVehicleRepository()
            val original = testVehicle(odometerKm = 10_000)
            val useCase = UpdateVehicleUseCase(repository)

            val result = useCase(params(original, odometerKm = 10_001))

            assertTrue(result is UpdateVehicleResult.Success)
            assertEquals(10_001, repository.savedVehicles.single().currentOdometerKm)
        }

    @Test
    fun odometerDecreaseRequiresConfirmationWithoutSaving() =
        runTest {
            val repository = FakeVehicleRepository()
            val original = testVehicle(odometerKm = 10_000)
            val useCase = UpdateVehicleUseCase(repository)

            val result = useCase(params(original, odometerKm = 9_000))

            assertEquals(
                UpdateVehicleResult.OdometerDecreaseConfirmationRequired(
                    currentOdometerKm = 10_000,
                    proposedOdometerKm = 9_000,
                ),
                result,
            )
            assertTrue(repository.savedVehicles.isEmpty())
        }

    @Test
    fun confirmedOdometerDecreaseIsSaved() =
        runTest {
            val repository = FakeVehicleRepository()
            val original = testVehicle(odometerKm = 10_000)
            val useCase = UpdateVehicleUseCase(repository)

            val result =
                useCase(
                    params(
                        original,
                        odometerKm = 9_000,
                        allowOdometerDecrease = true,
                    ),
                )

            assertTrue(result is UpdateVehicleResult.Success)
            assertEquals(9_000, repository.savedVehicles.single().currentOdometerKm)
        }

    private fun params(
        vehicle: Vehicle,
        name: String = vehicle.name,
        odometerKm: Int = vehicle.currentOdometerKm,
        allowOdometerDecrease: Boolean = false,
    ) = UpdateVehicleParams(
        currentVehicle = vehicle,
        name = name,
        type = vehicle.type,
        licensePlate = vehicle.licensePlate,
        odometerKm = odometerKm,
        allowOdometerDecrease = allowOdometerDecrease,
    )
}
