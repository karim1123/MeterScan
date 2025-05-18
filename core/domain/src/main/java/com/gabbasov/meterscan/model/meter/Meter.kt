package com.gabbasov.meterscan.model.meter

import java.time.LocalDate

enum class MeterType {
    ELECTRICITY, WATER, GAS
}

data class MeterReading(
    val date: LocalDate,
    val value: Double
)

data class Meter(
    val id: String,
    val type: MeterType,
    val number: String,
    val address: Address,
    val owner: String,
    val readings: List<MeterReading> = emptyList(),
    val installationDate: LocalDate,
    val nextCheckDate: LocalDate,
    val notes: String? = null,
    val state: MeterState = MeterState.REQUIRED
)

data class Address(
    val street: String,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun getFullAddress(): String = street
}