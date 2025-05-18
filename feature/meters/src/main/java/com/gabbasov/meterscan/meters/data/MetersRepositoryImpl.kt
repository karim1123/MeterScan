package com.gabbasov.meterscan.meters.data

import com.gabbasov.meterscan.database.MeterDao
import com.gabbasov.meterscan.database.MeterEntity
import com.gabbasov.meterscan.database.ReadingDao
import com.gabbasov.meterscan.database.ReadingEntity
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.repository.MetersRepository
import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.meter.MeterReading
import com.gabbasov.meterscan.model.meter.MeterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MetersRepositoryImpl(
    private val meterDao: MeterDao,
    private val readingDao: ReadingDao
) : MetersRepository {

    override fun getAllMeters(): Flow<Resource<List<Meter>>> {
        return meterDao.getAllMeters()
            .map { meterEntities ->
                Resource.Success(
                    meterEntities.map { entity ->
                        entity.toDomain()
                    }
                ) as Resource<List<Meter>>
            }
            .catch { e ->
                emit(Resource.Error(e))
            }
    }

    override fun getMeterById(id: String): Flow<Resource<Meter>> {
        val meterFlow = meterDao.getMeterById(id)
        val readingsFlow = readingDao.getReadingsForMeter(id)

        return combine(meterFlow, readingsFlow) { meterEntity, readingEntities ->
            if (meterEntity == null) {
                Resource.Error(Throwable("Счетчик не найден"))
            } else {
                val meter = meterEntity.toDomain().copy(
                    readings = readingEntities.map { it.toDomain() }
                )
                Resource.Success(meter)
            }
        }.catch { e ->
            emit(Resource.Error(e))
        }
    }

    override suspend fun addMeter(meter: Meter): Resource<Meter> {
        return try {
            val meterEntity = MeterEntity.fromDomain(meter)
            meterDao.insertMeter(meterEntity)

            // Вставляем показания, если они есть
            if (meter.readings.isNotEmpty()) {
                val readingEntities = meter.readings.map { reading ->
                    ReadingEntity.fromDomain(reading, meter.id)
                }
                readingDao.insertReadings(readingEntities)
            }

            Resource.Success(meter)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun updateMeter(meter: Meter): Resource<Meter> {
        return try {
            val meterEntity = MeterEntity.fromDomain(meter)
            meterDao.updateMeter(meterEntity)

            // Обновляем показания: удаляем старые и вставляем новые
            readingDao.deleteReadingsForMeter(meter.id)
            if (meter.readings.isNotEmpty()) {
                val readingEntities = meter.readings.map { reading ->
                    ReadingEntity.fromDomain(reading, meter.id)
                }
                readingDao.insertReadings(readingEntities)
            }

            Resource.Success(meter)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun deleteMeter(id: String): Resource<Unit> {
        return try {
            meterDao.deleteMeterById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun addReading(meterId: String, reading: Double): Resource<Meter> {
        return try {
            val meterResource = getMeterById(meterId).first()

            if (meterResource is Resource.Error) {
                return Resource.Error(Throwable("Счетчик не найден"))
            }

            val meter = (meterResource as Resource.Success).data
            val now = LocalDate.now()
            val currentMonth = now.month
            val currentYear = now.year

            // Удаляем показания за текущий месяц
            val filteredReadings = meter.readings.filter { reading ->
                reading.date.month != currentMonth || reading.date.year != currentYear
            }

            // Создаем новое показание
            val newReading = MeterReading(
                date = now,
                value = reading
            )

            val updatedMeter = meter.copy(
                readings = filteredReadings + newReading,
                state = MeterState.SUBMITTED_TO_SERVER
            )

            updateMeter(updatedMeter)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}