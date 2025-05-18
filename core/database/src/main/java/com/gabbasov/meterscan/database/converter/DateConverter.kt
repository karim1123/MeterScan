package com.gabbasov.meterscan.database.converter

import androidx.room.TypeConverter
import com.gabbasov.meterscan.model.meter.Address
import com.gabbasov.meterscan.model.meter.MeterType
import com.google.gson.Gson
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

class AddressConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAddress(address: Address?): String? {
        return address?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toAddress(addressString: String?): Address? {
        return addressString?.let { gson.fromJson(it, Address::class.java) }
    }
}
