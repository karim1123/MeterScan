package com.gabbasov.meterscan.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gabbasov.meterscan.model.meter.MeterReading
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "readings",
    foreignKeys = [
        ForeignKey(
            entity = MeterEntity::class,
            parentColumns = ["id"],
            childColumns = ["meterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meterId")]
)
@TypeConverters(DateConverter::class)
data class ReadingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val meterId: String,
    val date: LocalDate,
    val value: Double
) {
    fun toDomain(): MeterReading {
        return MeterReading(
            date = date,
            value = value
        )
    }

    companion object {
        fun fromDomain(reading: MeterReading, meterId: String): ReadingEntity {
            return ReadingEntity(
                meterId = meterId,
                date = reading.date,
                value = reading.value
            )
        }
    }
}