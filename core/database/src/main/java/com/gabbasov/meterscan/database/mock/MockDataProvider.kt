package com.gabbasov.meterscan.database.mock

import com.gabbasov.meterscan.database.MeterEntity
import com.gabbasov.meterscan.database.MeterScanDatabase
import com.gabbasov.meterscan.database.ReadingEntity
import com.gabbasov.meterscan.model.meter.Address
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.model.meter.MeterType
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
        // Адреса в Ижевске с координатами
        val izhevskAddresses = listOf(
            Address(
                street = "г. Ижевск, ул. Пушкинская, д. 278, кв. 15",
                latitude = 56.8498,
                longitude = 53.2045
            ),
            Address(
                street = "г. Ижевск, ул. Удмуртская, д. 304, кв. 78",
                latitude = 56.8567,
                longitude = 53.2112
            ),
            Address(
                street = "г. Ижевск, ул. Ленина, д. 45, кв. 3",
                latitude = 56.8443,
                longitude = 53.2067
            ),
            Address(
                street = "г. Ижевск, ул. Молодежная, д. 111, кв. 144",
                latitude = 56.8612,
                longitude = 53.2234
            ),
            Address(
                street = "г. Ижевск, ул. Советская, д. 22, кв. 21",
                latitude = 56.8489,
                longitude = 53.2098
            )
        )

        // Создаем тестовые счетчики с разными состояниями
        val meters = listOf(
            createMockMeter(MeterType.ELECTRICITY, "54762198", izhevskAddresses[0], "Иванов Иван Иванович", MeterState.REQUIRED),
            createMockMeter(MeterType.WATER, "78452196", izhevskAddresses[1], "Петров Петр Петрович", MeterState.NOT_REQUIRED),
            createMockMeter(MeterType.GAS, "12453698", izhevskAddresses[2], "Сидорова Мария Ивановна", MeterState.REQUIRED),
            createMockMeter(MeterType.ELECTRICITY, "89541237", izhevskAddresses[3], "Кузнецов Алексей Викторович", MeterState.REQUIRED),
            createMockMeter(MeterType.WATER, "74125638", izhevskAddresses[4], "Иванов Иван Иванович", MeterState.REQUIRED)
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
        address: Address,
        owner: String,
        state: MeterState = MeterState.REQUIRED
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
            notes = if (Random.nextBoolean()) getMockNote(type) else null,
            state = state
        )
    }

    /**
     * Создает тестовые показания для счетчика
     */
    private fun createMockReadingsForMeter(meterId: String, type: MeterType): List<ReadingEntity> {
        val startDate = LocalDate.now().minusYears(5)
        val readings = mutableListOf<ReadingEntity>()

        // Начальное значение (округляем до целых)
        var currentValue = when (type) {
            MeterType.ELECTRICITY -> Random.nextDouble(1000.0, 10000.0).roundToInt().toDouble()
            MeterType.WATER -> Random.nextDouble(50.0, 500.0).roundToInt().toDouble()
            MeterType.GAS -> Random.nextDouble(100.0, 1000.0).roundToInt().toDouble()
        }

        // Создаем показания за 5 лет (60 месяцев)
        for (i in 0..60) {
            val date = startDate.plusMonths(i.toLong())
            val month = date.monthValue

            // Увеличиваем значение в зависимости от типа счетчика
            val increment = when (type) {
                MeterType.ELECTRICITY -> Random.nextDouble(100.0, 300.0)
                MeterType.WATER -> Random.nextDouble(0.0, 30.0)
                MeterType.GAS -> {
                    if (month in 10..12 || month in 1..3) {
                        Random.nextDouble(300.0, 1200.0)
                    } else {
                        Random.nextDouble(0.0, 300.0)
                    }
                }
            }

            // Добавляем приращение и округляем до целого числа
            currentValue = (currentValue + increment).roundToInt().toDouble()

            readings.add(
                ReadingEntity(
                    id = UUID.randomUUID().toString(),
                    meterId = meterId,
                    date = date,
                    value = currentValue
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