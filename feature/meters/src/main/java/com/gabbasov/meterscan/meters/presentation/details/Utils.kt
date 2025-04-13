package com.gabbasov.meterscan.meters.presentation.details

import android.util.Log
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
        .toSortedMap() // Естественный порядок месяцев

    // Convert readings to consumption (difference between consecutive months)
    val sortedMonths = monthlyReadings.keys.toList()

    // Проверка наличия данных за декабрь предыдущего года
    val decemberLastYear = readings
        .filter { it.date.year == readings.first().date.year - 1 && it.date.month == Month.DECEMBER }
        .maxByOrNull { it.date }

    // Обработка января (если есть)
    if (sortedMonths.contains(Month.JANUARY)) {
        val januaryReading = monthlyReadings[Month.JANUARY]!!

        if (decemberLastYear != null) {
            // Если есть данные за декабрь прошлого года, расчитываем разницу
            val consumption = januaryReading.value - decemberLastYear.value
            if (consumption > 0) {
                monthlyConsumption[Month.JANUARY] = consumption
            }
        } else {
            // Если нет данных за декабрь, используем среднее потребление для других месяцев
            // или можно установить в качестве потребления само значение
            monthlyConsumption[Month.JANUARY] = januaryReading.value / 12.0 // примерное деление на количество месяцев
        }
    }

    for (i in 1 until sortedMonths.size) {
        val currentMonth = sortedMonths[i]
        val previousMonth = sortedMonths[i-1]

        val currentReading = monthlyReadings[currentMonth]!!
        val previousReading = monthlyReadings[previousMonth]!!

        val consumption = currentReading.value - previousReading.value
        if (consumption > 0) {
            monthlyConsumption[currentMonth] = consumption
        }
    }

    Log.d("test312", "monthlyConsumption: $monthlyConsumption")

    return monthlyConsumption
}

fun calculateMonthlyConsumption(readings: List<MeterReading>): Map<LocalDate, Double> {
    // Сортируем показания по дате
    val sortedReadings = readings.sortedBy { it.date }
    val consumption = mutableMapOf<LocalDate, Double>()

    // Проверим, есть ли у нас декабрь предыдущего года для расчета января
    val decemberLastYear = sortedReadings
        .firstOrNull()
        ?.let { firstReading ->
            readings.filter { it.date.year == firstReading.date.year - 1 && it.date.month == Month.DECEMBER }
                .maxByOrNull { it.date }
        }

    // Обрабатываем первое показание (вероятно январь)
    if (sortedReadings.isNotEmpty()) {
        val firstReading = sortedReadings.first()

        if (firstReading.date.month == Month.JANUARY) {
            if (decemberLastYear != null) {
                // Если у нас есть декабрь прошлого года, можем вычислить потребление для января
                val diff = firstReading.value - decemberLastYear.value
                if (diff > 0) {
                    consumption[firstReading.date] = diff
                }
            } else {
                // Если нет декабря прошлого года, используем среднее потребление
                // или установим примерное значение
                val avgConsumption = if (sortedReadings.size > 2) {
                    // Среднее потребление из других месяцев
                    var total = 0.0
                    var count = 0
                    for (i in 1 until sortedReadings.size) {
                        val diff = sortedReadings[i].value - sortedReadings[i-1].value
                        if (diff > 0) {
                            total += diff
                            count++
                        }
                    }
                    if (count > 0) total / count else firstReading.value / 12.0
                } else {
                    // Если нет данных для расчета среднего, используем простую оценку
                    firstReading.value / 12.0 // Примерное значение
                }

                consumption[firstReading.date] = avgConsumption
            }
        }
    }

    // Рассчитываем потребление для остальных месяцев
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
