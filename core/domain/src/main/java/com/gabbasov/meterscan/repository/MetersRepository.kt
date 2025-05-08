package com.gabbasov.meterscan.repository

import com.gabbasov.meterscan.base.Resource
import com.gabbasov.meterscan.model.meter.Meter
import kotlinx.coroutines.flow.Flow

interface MetersRepository {
    fun getAllMeters(): Flow<Resource<List<Meter>>>

    fun getMeterById(id: String): Flow<Resource<Meter>>

    suspend fun addMeter(meter: Meter): Resource<Meter>

    suspend fun updateMeter(meter: Meter): Resource<Meter>

    suspend fun deleteMeter(id: String): Resource<Unit>
}
