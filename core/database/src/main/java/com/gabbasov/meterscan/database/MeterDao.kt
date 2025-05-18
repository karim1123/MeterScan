package com.gabbasov.meterscan.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterDao {
    @Query("SELECT * FROM meters ORDER BY address ASC")
    fun getAllMeters(): Flow<List<MeterEntity>>

    @Query("SELECT * FROM meters WHERE id = :meterId")
    fun getMeterById(meterId: String): Flow<MeterEntity?>

    @Query("SELECT COUNT(*) FROM meters")
    suspend fun getMeterCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeter(meter: MeterEntity): Long

    @Update
    suspend fun updateMeter(meter: MeterEntity)

    @Delete
    suspend fun deleteMeter(meter: MeterEntity)

    @Query("DELETE FROM meters WHERE id = :meterId")
    suspend fun deleteMeterById(meterId: String)
}

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY date ASC")
    fun getReadingsForMeter(meterId: String): Flow<List<ReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<ReadingEntity>)

    @Update
    suspend fun updateReading(reading: ReadingEntity)

    @Delete
    suspend fun deleteReading(reading: ReadingEntity)

    @Query("DELETE FROM readings WHERE meterId = :meterId")
    suspend fun deleteReadingsForMeter(meterId: String)
}
