package com.gabbasov.meterscan.scan.presentation.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.gabbasov.meterscan.scan.domain.DigitBox

/**
 * Отображение распознанных цифр поверх видео с камеры
 */
class DigitOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var digitBoxes = listOf<DigitBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var bounds = Rect()

    init {
        initPaints()
    }

    fun clear() {
        digitBoxes = listOf()
        invalidate()
    }

    private fun initPaints() {
        textBackgroundPaint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 50f
        }

        textPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = 50f
        }

        boxPaint.apply {
            color = Color.GREEN//ContextCompat.getColor(context, R.color.digitBoxColor)
            strokeWidth = 8F
            style = Paint.Style.STROKE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Сортируем цифры по x-координате для правильного отображения
        val sortedBoxes = digitBoxes.sortedBy { it.x1 }

        sortedBoxes.forEach { box ->
            val left = box.x1 * width
            val top = box.y1 * height
            val right = box.x2 * width
            val bottom = box.y2 * height

            // Рисуем рамку вокруг цифры
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Рисуем текст с распознанной цифрой
            val drawableText = box.digit

            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            // Прямоугольник для фона текста
            canvas.drawRect(
                left,
                top,
                left + textWidth + TEXT_PADDING,
                top + textHeight + TEXT_PADDING,
                textBackgroundPaint
            )

            // Текст с цифрой
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
        }
    }

    fun setResults(boxes: List<DigitBox>) {
        digitBoxes = boxes
        invalidate()
    }

    companion object {
        private const val TEXT_PADDING = 8
    }
}
