package com.gabbasov.meterscan.work.presentation.list.pager.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.gabbasov.meterscan.model.meter.MeterType
import com.gabbasov.meterscan.work.R

object MapIconUtils {

    /**
     * Создает Bitmap иконки счетчика с кружком и тинтом для отображения на карте
     */
    fun createMeterIconBitmap(context: Context, iconResId: Int, meterType: MeterType): Bitmap {
        // Определяем цвет в зависимости от типа счетчика
        val tintColor = when (meterType) {
            MeterType.ELECTRICITY -> Color(0xFFFFC107) // Желтый для электричества
            MeterType.WATER -> Color(0xFF2196F3) // Синий для воды
            MeterType.GAS -> Color(0xFFFF5722) // Оранжевый для газа
        }

        // Создаем Drawable из ресурса
        val drawable = AppCompatResources.getDrawable(context, iconResId)
            ?: throw IllegalArgumentException("Resource not found: $iconResId")

        // Устанавливаем размеры
        val size = context.resources.getDimensionPixelSize(R.dimen.map_icon_size)

        // Создаем bitmap для фона (круг)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Рисуем круглый фон
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor.copy(alpha = 0.2f).toArgb()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // Устанавливаем tint для drawable
        DrawableCompat.setTint(drawable, tintColor.toArgb())
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)

        // Устанавливаем bounds для центрирования иконки (60% от размера круга)
        val iconSize = (size * 0.6f).toInt()
        val offset = (size - iconSize) / 2
        drawable.setBounds(offset, offset, offset + iconSize, offset + iconSize)

        // Рисуем иконку
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * Создает Bitmap иконки текущего местоположения пользователя
     */
    fun createLocationMarkerBitmap(context: Context): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_location)
            ?: throw IllegalArgumentException("Resource not found")

        val size = context.resources.getDimensionPixelSize(R.dimen.location_icon_size)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Устанавливаем размеры drawable
        drawable.setBounds(0, 0, size, size)

        // Устанавливаем цвет (синий)
        DrawableCompat.setTint(drawable, Color(0xFF4285F4).toArgb())
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)

        drawable.draw(canvas)

        return bitmap
    }
}
