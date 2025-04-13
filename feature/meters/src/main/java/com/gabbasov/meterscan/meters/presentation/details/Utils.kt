package com.gabbasov.meterscan.meters.presentation.details

import com.gabbasov.meterscan.meters.domain.MeterReading
import com.gabbasov.meterscan.meters.domain.MeterType
import java.time.LocalDate
import java.time.Month


// Вспомогательные функции

fun getMeterTypeText(type: MeterType): String {
    return when (type) {
        MeterType.ELECTRICITY -> "Счетчик электроэнергии"
        MeterType.WATER -> "Счетчик воды"
        MeterType.GAS -> "Счетчик газа"
    }
}

fun getMeterUnits(type: MeterType): String {
    return when (type) {
        MeterType.ELECTRICITY -> "кВт·ч"
        MeterType.WATER -> "м³"
        MeterType.GAS -> "м³"
    }
}

fun calculateYearConsumption(readings: List<MeterReading>): Double {
    if (readings.isEmpty()) return 0.0

    // Group readings by month and take the latest reading for each month
    val monthlyReadings = readings
        .groupBy { it.date.month }
        .mapValues { (_, monthReadings) ->
            monthReadings.maxByOrNull { it.date }
        }
        .values
        .filterNotNull()
        .sortedBy { it.date }

    // Calculate the sum of monthly consumption
    var totalConsumption = 0.0
    for (i in 1 until monthlyReadings.size) {
        val consumption = monthlyReadings[i].value - monthlyReadings[i-1].value
        if (consumption > 0) {  // Only add positive consumption (to handle reset or replacement cases)
            totalConsumption += consumption
        }
    }

    return totalConsumption
}

fun prepareMonthlyData(readings: List<MeterReading>): Map<Month, Double> {
    // Initialize all months with zero consumption
    val monthlyConsumption = Month.entries.associateWith { 0.0 }.toMutableMap()

    // If we have fewer than 2 readings, we can't calculate consumption
    if (readings.size < 2) return monthlyConsumption

    // Group readings by month and take the latest reading for each month
    val monthlyReadings = readings
        .groupBy { it.date.month }
        .mapValues { (_, readings) ->
            readings.maxByOrNull { it.date }
        }
        .filterValues { it != null }
        .mapValues { it.value!! }
        .toSortedMap(compareBy { it.value })

    // Convert readings to consumption (difference between consecutive months)
    val sortedMonths = monthlyReadings.keys.toList()

    for (i in 1 until sortedMonths.size) {
        val currentMonth = sortedMonths[i]
        val previousMonth = sortedMonths[i-1]

        val currentReading = monthlyReadings[currentMonth]!!
        val previousReading = monthlyReadings[previousMonth]!!

        val consumption = currentReading.value - previousReading.value
        if (consumption > 0) {  // Ignore negative values (meter reset or replacement)
            monthlyConsumption[currentMonth] = consumption
        }
    }

    return monthlyConsumption
}

fun calculateMonthlyConsumption(readings: List<MeterReading>): Map<LocalDate, Double> {
    val sortedReadings = readings.sortedBy { it.date }
    val consumption = mutableMapOf<LocalDate, Double>()

    for (i in 1 until sortedReadings.size) {
        val current = sortedReadings[i]
        val previous = sortedReadings[i-1]
        val diff = current.value - previous.value

        if (diff > 0) {
            consumption[current.date] = diff
        }
    }

    return consumption
}