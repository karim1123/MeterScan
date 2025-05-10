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
    val address: String,
    val owner: String,
    val readings: List<MeterReading> = emptyList(),
    val installationDate: LocalDate,
    val nextCheckDate: LocalDate,
    val notes: String? = null
)
