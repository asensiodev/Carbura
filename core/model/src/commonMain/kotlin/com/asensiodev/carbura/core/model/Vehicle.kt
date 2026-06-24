package com.asensiodev.carbura.core.model

data class Vehicle(
    val id: VehicleId,
    val familyId: FamilyId,
    val name: String,
    val type: String,
    val plate: String?,
    val currentOdometerKm: Int,
)
