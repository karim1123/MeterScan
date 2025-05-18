package com.gabbasov.meterscan.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gabbasov.meterscan.database.converter.AddressConverter
import com.gabbasov.meterscan.database.converter.DateConverter
import com.gabbasov.meterscan.database.converter.MeterStateConverter
import com.gabbasov.meterscan.database.converter.MeterTypeConverter
import com.gabbasov.meterscan.model.meter.Address
import com.gabbasov.meterscan.model.meter.Meter
import com.gabbasov.meterscan.model.meter.MeterState
import com.gabbasov.meterscan.model.meter.MeterType
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "meters")
@TypeConverters(DateConverter::class, MeterTypeConverter::class, AddressConverter::class, MeterStateConverter::class)
data class MeterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: MeterType,
    val number: String,
    val address: Address,
    val owner: String,
    val installationDate: LocalDate,
    val nextCheckDate: LocalDate,
    val notes: String?,
    val state: MeterState = MeterState.REQUIRED // По умолчанию нужно снимать показания
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
            notes = notes,
            state = state
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
                notes = meter.notes,
                state = meter.state
            )
        }
    }
}