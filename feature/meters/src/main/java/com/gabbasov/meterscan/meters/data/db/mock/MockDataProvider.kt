package com.gabbasov.meterscan.meters.data.db.mock

import com.gabbasov.meterscan.meters.data.db.MeterEntity
import com.gabbasov.meterscan.meters.data.db.MeterScanDatabase
import com.gabbasov.meterscan.meters.data.db.ReadingEntity
import com.gabbasov.meterscan.meters.domain.MeterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Класс для заполнения базы данных тестовыми данными
 */
class MockDataProvider(private val database: MeterScanDatabase) {

    /**
     * Проверяет, пуста ли БД, и если да - заполняет тестовыми данными
     */
    suspend fun populateMockDataIfNeeded() = withContext(Dispatchers.IO) {
        val meterCount = database.meterDao().getMeterCount()
        if (meterCount == 0) {
            populateMockData()
        }
    }

    /**
     * Заполняет базу данных тестовыми данными
     */
    private suspend fun populateMockData() = withContext(Dispatchers.IO) {
        val izhevskAddresses = listOf(
            "г. Ижевск, ул. Пушкинская, д. 278, кв. 15",
            "г. Ижевск, ул. Удмуртская, д. 304, кв. 78",
            "г. Ижевск, ул. Ленина, д. 45, кв. 3",
            "г. Ижевск, ул. Молодежная, д. 111, кв. 144",
            "г. Ижевск, ул. Советская, д. 22, кв. 21"
        )

        val meters = listOf(
            createMockMeter(MeterType.ELECTRICITY, "54762198", izhevskAddresses[0], "Иванов Иван Иванович"),
            createMockMeter(MeterType.WATER, "78452196", izhevskAddresses[1], "Петров Петр Петрович"),
            createMockMeter(MeterType.GAS, "12453698", izhevskAddresses[2], "Сидорова Мария Ивановна"),
            createMockMeter(MeterType.ELECTRICITY, "89541237", izhevskAddresses[3], "Кузнецов Алексей Викторович"),
            createMockMeter(MeterType.WATER, "74125638", izhevskAddresses[4], "Иванов Иван Иванович")
        )

        meters.forEach { meter ->
            database.meterDao().insertMeter(meter)
            val readings = createMockReadingsForMeter(meter.id, meter.type)
            database.readingDao().insertReadings(readings)
        }
    }

    /**
     * Создает тестовый счетчик
     */
    private fun createMockMeter(
        type: MeterType,
        number: String,
        address: String,
        owner: String
    ): MeterEntity {
        val now = LocalDate.now()
        return MeterEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            number = number,
            address = address,
            owner = owner,
            installationDate = now.minusMonths(Random.nextLong(6, 36)),
            nextCheckDate = now.plusMonths(Random.nextLong(12, 48)),
            notes = if (Random.nextBoolean()) getMockNote(type) else null
        )
    }

    /**
     * Создает тестовые показания для счетчика
     */
    private fun createMockReadingsForMeter(meterId: String, type: MeterType): List<ReadingEntity> {
        val startDate = LocalDate.now().minusYears(5)
        val readings = mutableListOf<ReadingEntity>()

        // Начальное значение
        var currentValue = when (type) {
            MeterType.ELECTRICITY -> Random.nextDouble(1000.0, 10000.0)
            MeterType.WATER -> Random.nextDouble(50.0, 500.0)
            MeterType.GAS -> Random.nextDouble(100.0, 1000.0)
        }

        // Создаем показания за 5 лет (60 месяцев)
        for (i in 0..60) {
            val date = startDate.plusMonths(i.toLong())
            val month = date.monthValue

            // Увеличиваем значение в зависимости от типа счетчика
            currentValue += when (type) {
                MeterType.ELECTRICITY -> Random.nextDouble(100.0, 300.0) // 100-300 кВтч в месяц
                MeterType.WATER -> Random.nextDouble(0.0, 30.0) // 0-30 м³ в месяц
                MeterType.GAS -> {
                    // Сезонность для газа: больше зимой (октябрь-март), меньше летом (апрель-сентябрь)
                    if (month in 10..12 || month in 1..3) {
                        Random.nextDouble(300.0, 1200.0) // Зимой высокое потребление
                    } else {
                        Random.nextDouble(0.0, 300.0) // Летом низкое потребление
                    }
                }
            }

            // Округляем значения
            val roundedValue = when (type) {
                MeterType.ELECTRICITY -> (currentValue * 10).roundToInt() / 10.0
                MeterType.WATER -> (currentValue * 100).roundToInt() / 100.0
                MeterType.GAS -> (currentValue * 10).roundToInt() / 10.0
            }

            readings.add(
                ReadingEntity(
                    id = UUID.randomUUID().toString(),
                    meterId = meterId,
                    date = date,
                    value = roundedValue
                )
            )
        }

        return readings
    }

    /**
     * Возвращает случайное примечание для счетчика
     */
    private fun getMockNote(type: MeterType): String {
        val commonNotes = listOf(
            "Требуется замена в ближайшие 2 года",
            "Установлен после капитального ремонта",
            "Прибор учета опломбирован",
            "Необходима проверка показаний",
            "Возможны неточности в измерениях"
        )

        val typeSpecificNotes = when (type) {
            MeterType.ELECTRICITY -> listOf(
                "Двухтарифный счетчик",
                "Повышенное энергопотребление",
                "Проблемы со стабильностью напряжения"
            )
            MeterType.WATER -> listOf(
                "Счетчик холодной воды",
                "Счетчик горячей воды",
                "Установлен фильтр грубой очистки"
            )
            MeterType.GAS -> listOf(
                "Требуется регулярная проверка на утечки",
                "Проведена проверка на герметичность",
                "Установлен распределительный счетчик"
            )
        }

        return if (Random.nextBoolean()) commonNotes.random() else typeSpecificNotes.random()
    }
}