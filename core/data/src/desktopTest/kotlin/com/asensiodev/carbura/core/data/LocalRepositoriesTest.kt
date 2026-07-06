package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalRepositoriesTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")

    @Test
    fun vehicleRepositoryReadsSavedVehiclesFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val vehicleRepository = LocalVehicleRepository(firstDatabase)
        vehicleRepository.saveVehicle(
            Vehicle(
                id = vehicleId,
                familyId = familyId,
                name = "Coche familiar",
                type = VehicleType.Car,
                currentOdometerKm = 12000,
            ),
        )

        val recreatedRepository = LocalVehicleRepository(recreatedDatabase)

        val vehicles = recreatedRepository.observeVehicles(familyId)
        assertEquals(1, vehicles.size)
        assertEquals("Coche familiar", vehicles.single().name)
        assertEquals(12000, vehicles.single().currentOdometerKm)
    }

    @Test
    fun maintenanceRepositoryReadsHistoryOrderedByDateFromRecreatedDatabase() = runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
        val repository = LocalMaintenanceRecordRepository(firstDatabase)
        repository.saveMaintenanceRecord(record("old", "2026-01-01"))
        repository.saveMaintenanceRecord(record("new", "2026-07-04"))

        val recreatedRepository = LocalMaintenanceRecordRepository(recreatedDatabase)

        val history = recreatedRepository.getVehicleHistory(vehicleId)
        assertEquals(listOf("new", "old"), history.map { it.id.value })
    }

    private fun runTestWithRecreatedDatabase(
        block: suspend (CarburaDatabase, CarburaDatabase) -> Unit,
    ) = kotlinx.coroutines.test.runTest {
        val databaseFile = File.createTempFile("carbura-test", ".db").also { it.deleteOnExit() }
        val firstDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        CarburaDatabase.Schema.create(firstDriver)
        val recreatedDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")

        block(CarburaDatabase(firstDriver), CarburaDatabase(recreatedDriver))
    }

    private fun record(id: String, performedOn: String): MaintenanceRecord = MaintenanceRecord(
        id = MaintenanceRecordId(id),
        familyId = familyId,
        vehicleId = vehicleId,
        maintenanceTypeId = MaintenanceTypeId("type-$id"),
        maintenanceTypeCode = MaintenanceTypeCode.Custom,
        performedOn = CalendarDate(performedOn),
        odometerKm = 1,
    )
}
