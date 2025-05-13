package com.gabbasov.meterscan.repository

interface ScanSettingsRepository {
    /**
     * Получение размера буфера накопления для стабилизации распознавания.
     * Больший размер дает более стабильное распознавание, но требует больше времени.
     */
    suspend fun getBufferSize(): Int
    suspend fun setBufferSize(size: Int)

    /**
     * Получение порога базовой уверенности распознавания.
     * Минимальная уверенность, необходимая для учета обнаруженной цифры.
     * Ниже этого порога обнаружения игнорируются.
     */
    suspend fun getConfidenceThreshold(): Float
    suspend fun setConfidenceThreshold(threshold: Float)

    /**
     * Получение порога высокой уверенности распознавания.
     * Если уверенность выше этого порога, цифра учитывается даже
     * при нарушении других условий (например, неправильное расположение).
     */
    suspend fun getHighConfidenceThreshold(): Float
    suspend fun setHighConfidenceThreshold(threshold: Float)

    // Количество цифр в счетчике
    suspend fun getDefaultDigitCount(): Int
    suspend fun setDefaultDigitCount(count: Int)
}
