package com.gabbasov.meterscan.meters.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gabbasov.meterscan.model.meter.Address
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterType
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "meters")
@TypeConverters(DateConverter::class, MeterTypeConverter::class, AddressConverter::class)
data class MeterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: MeterType,
    val number: String,
    val address: Address,
    val owner: String,
    val installationDate: LocalDate,
    val nextCheckDate: LocalDate,
    val notes: String?
) {
    fun toDomain(): Meter {
        return Meter(
            id = id,
            type = type,
            number = number,
            address = address,
            owner = owner,
            readings = emptyList(),
            installationDate = installationDate,
            nextCheckDate = nextCheckDate,
            notes = notes
        )
    }

    companion object {
        fun fromDomain(meter: Meter): MeterEntity {
            return MeterEntity(
                id = meter.id,
                type = meter.type,
                number = meter.number,
                address = meter.address,
                owner = meter.owner,
                installationDate = meter.installationDate,
                nextCheckDate = meter.nextCheckDate,
                notes = meter.notes
            )
        }
    }
}