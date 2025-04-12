package com.gabbasov.meterscan.meters.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MeterEntity::class, ReadingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MeterScanDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao
    abstract fun readingDao(): ReadingDao

    companion object {
        const val DATABASE_NAME = "meter_scan_db"
    }
}