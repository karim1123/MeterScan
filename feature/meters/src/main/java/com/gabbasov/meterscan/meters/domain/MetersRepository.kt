package com.gabbasov.meterscan.meters.domain

import com.gabbasov.meterscan.network.Resource
import kotlinx.coroutines.flow.Flow

interface MetersRepository {
    fun getAllMeters(): Flow<Resource<List<Meter>>>

    fun getMeterById(id: String): Flow<Resource<Meter>>

    suspend fun addMeter(meter: Meter): Resource<Meter>

    suspend fun updateMeter(meter: Meter): Resource<Meter>

    suspend fun deleteMeter(id: String): Resource<Unit>
}
