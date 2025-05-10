package com.gabbasov.meterscan.meters.data.db

import androidx.room.TypeConverter
import com.gabbasov.meterscan.model.meter.MeterType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, formatter) }
    }
}

class MeterTypeConverter {
    @TypeConverter
    fun fromMeterType(type: MeterType): String {
        return type.name
    }

    @TypeConverter
    fun toMeterType(value: String): MeterType {
        return MeterType.valueOf(value)
    }
}