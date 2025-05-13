package com.gabbasov.meterscan.scan.data

import android.content.Context
import android.content.SharedPreferences
import com.gabbasov.meterscan.repository.ScanSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanSettingsRepositoryImpl(
    private val context: Context
) : ScanSettingsRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(SCAN_SETTINGS_PREFS, Context.MODE_PRIVATE)

    // Размер буфера - количество кадров для накопления и стабилизации
    override suspend fun getBufferSize(): Int = withContext(Dispatchers.IO) {
        prefs.getInt(KEY_BUFFER_SIZE, DEFAULT_BUFFER_SIZE)
    }

    override suspend fun setBufferSize(size: Int) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(KEY_BUFFER_SIZE, size).apply()
    }

    // Базовый порог уверенности распознавания (0.0-1.0)
    override suspend fun getConfidenceThreshold(): Float = withContext(Dispatchers.IO) {
        prefs.getFloat(KEY_CONFIDENCE_THRESHOLD, DEFAULT_CONFIDENCE_THRESHOLD)
    }

    override suspend fun setConfidenceThreshold(threshold: Float) = withContext(Dispatchers.IO) {
        prefs.edit().putFloat(KEY_CONFIDENCE_THRESHOLD, threshold).apply()
    }

    // Порог высокой уверенности распознавания (0.0-1.0)
    override suspend fun getHighConfidenceThreshold(): Float = withContext(Dispatchers.IO) {
        prefs.getFloat(KEY_HIGH_CONFIDENCE_THRESHOLD, DEFAULT_HIGH_CONFIDENCE_THRESHOLD)
    }

    override suspend fun setHighConfidenceThreshold(threshold: Float) = withContext(Dispatchers.IO) {
        prefs.edit().putFloat(KEY_HIGH_CONFIDENCE_THRESHOLD, threshold).apply()
    }

    override suspend fun getDefaultDigitCount(): Int = withContext(Dispatchers.IO) {
        prefs.getInt(KEY_DEFAULT_DIGIT_COUNT, DEFAULT_DIGIT_COUNT)
    }

    override suspend fun setDefaultDigitCount(count: Int) = withContext(Dispatchers.IO) {
        prefs.edit().putInt(KEY_DEFAULT_DIGIT_COUNT, count).apply()
    }

    companion object {
        private const val SCAN_SETTINGS_PREFS = "scan_settings_preferences"

        // Ключи для настроек
        private const val KEY_BUFFER_SIZE = "buffer_size" // Размер буфера накопления
        private const val KEY_CONFIDENCE_THRESHOLD = "confidence_threshold" // Базовый порог уверенности
        private const val KEY_HIGH_CONFIDENCE_THRESHOLD = "high_confidence_threshold" // Высокий порог
        private const val KEY_DEFAULT_DIGIT_COUNT = "key_default_digit_count" // Высокий порог

        // Значения по умолчанию
        private const val DEFAULT_BUFFER_SIZE = 10 // Накапливаем 30 кадров
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f // 50% уверенность для базового порога
        private const val DEFAULT_HIGH_CONFIDENCE_THRESHOLD = 0.95f // 95% для высокого порога
        private const val DEFAULT_DIGIT_COUNT = 4

        const val STABILITY_THRESHOLD = 0.7f // Порог стабильности (70%)
    }
}