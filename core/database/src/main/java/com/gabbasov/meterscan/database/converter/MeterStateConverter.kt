package com.gabbasov.meterscan.database.converter

import androidx.room.TypeConverter
import com.gabbasov.meterscan.model.meter.MeterState

class MeterStateConverter {
    @TypeConverter
    fun fromMeterState(state: MeterState): String {
        return state.name
    }

    @TypeConverter
    fun toMeterState(value: String): MeterState {
        return MeterState.valueOf(value)
    }
}
