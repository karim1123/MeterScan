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
        // Проверяем, есть ли записи в БД
        val meterCount = database.meterDao().getMeterCount()

        // Если счетчиков нет, заполняем БД
        if (meterCount == 0) {
            populateMockData()
        }
    }

    /**
     * Заполняет базу данных тестовыми данными
     */
    private suspend fun populateMockData() = withContext(Dispatchers.IO) {
        // Создаем случайные счетчики разных типов
        val meters = listOf(
            createMockMeter(
                MeterType.ELECTRICITY,
                "54762198",
                "ул. Ленина, д. 42, кв. 15",
                "Иванов Иван Иванович"
            ),
            createMockMeter(
                MeterType.WATER,
                "78452196",
                "ул. Пушкина, д. 10, кв. 78",
                "Петров Петр Петрович"
            ),
            createMockMeter(
                MeterType.GAS,
                "12453698",
                "ул. Гагарина, д. 15, кв. 3",
                "Сидорова Мария Ивановна"
            ),
            createMockMeter(
                MeterType.ELECTRICITY,
                "89541237",
                "пр. Космонавтов, д. 25, кв. 144",
                "Кузнецов Алексей Викторович"
            ),
            createMockMeter(
                MeterType.WATER,
                "74125638",
                "ул. Строителей, д. 7, кв. 21",
                "Иванов Иван Иванович"
            )
        )

        // Сохраняем счетчики в базу данных
        meters.forEach { meter ->
            database.meterDao().insertMeter(meter)

            // Создаем и сохраняем показания для каждого счетчика
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
        val startDate = LocalDate.now().minusMonths(7)
        val readings = mutableListOf<ReadingEntity>()

        // Генерируем начальное значение в зависимости от типа счетчика
        var currentValue = when (type) {
            MeterType.ELECTRICITY -> Random.nextDouble(1000.0, 10000.0)
            MeterType.WATER -> Random.nextDouble(50.0, 500.0)
            MeterType.GAS -> Random.nextDouble(100.0, 1000.0)
        }

        // Создаем показания за последние 6 месяцев, плюс текущий месяц
        for (i in 0..6) {
            val date = startDate.plusMonths(i.toLong())

            // Увеличиваем значение для каждого месяца
            currentValue += when (type) {
                MeterType.ELECTRICITY -> Random.nextDouble(100.0, 300.0)
                MeterType.WATER -> Random.nextDouble(3.0, 10.0)
                MeterType.GAS -> Random.nextDouble(10.0, 50.0)
            }

            // Округляем значения для более реалистичных показаний
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

        return if (Random.nextBoolean()) {
            commonNotes.random()
        } else {
            typeSpecificNotes.random()
        }
    }
}