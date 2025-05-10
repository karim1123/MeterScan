package com.gabbasov.meterscan.scan.domain

/**
 * Модель данных для обнаруженной цифры на счетчике
 *
 * @property x1 Левая координата ограничивающей рамки (0.0 - 1.0)
 * @property y1 Верхняя координата ограничивающей рамки (0.0 - 1.0)
 * @property x2 Правая координата ограничивающей рамки (0.0 - 1.0)
 * @property y2 Нижняя координата ограничивающей рамки (0.0 - 1.0)
 * @property cx Координата центра по X (0.0 - 1.0)
 * @property cy Координата центра по Y (0.0 - 1.0)
 * @property w Ширина ограничивающей рамки (0.0 - 1.0)
 * @property h Высота ограничивающей рамки (0.0 - 1.0)
 * @property confidence Уверенность распознавания (0.0 - 1.0)
 * @property digit Значение распознанной цифры
 */
data class DigitBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val confidence: Float,
    val digit: String
)
