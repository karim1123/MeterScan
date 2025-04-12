package com.gabbasov.meterscan.meters.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.meters.domain.MeterReading
import com.gabbasov.meterscan.meters.domain.MeterType
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import java.time.format.DateTimeFormatter

@Composable
fun ComposeReadingsChart(
    readings: List<MeterReading>,
    meterType: MeterType,
    modifier: Modifier = Modifier
) {
    // Определяем цвет для графика в зависимости от типа счетчика
    val mainColor = when (meterType) {
        MeterType.ELECTRICITY -> Color(0xFFFFC107) // Желтый для электричества
        MeterType.WATER -> Color(0xFF2196F3) // Синий для воды
        MeterType.GAS -> Color(0xFFFF5722) // Оранжевый для газа
    }

    // Создаем градиент для столбцов
    val chartBrush = Brush.verticalGradient(
        colors = listOf(
            mainColor.copy(alpha = 0.8f),
            mainColor
        )
    )

    // Форматируем даты для отображения на оси X
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")

    // Подготавливаем данные для графика
    val chartData = readings.map { reading ->
        Bars(
            label = reading.date.format(dateFormatter),
            values = listOf(
                Bars.Data(
                    label = "Показания",
                    value = reading.value,
                    color = chartBrush
                )
            )
        )
    }

    ColumnChart(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp),
        data = remember { chartData },
        barProperties = BarProperties(
            thickness = 20.dp,
            spacing = 3.dp,
            cornerRadius = Bars.Data.Radius.Rectangle(
                topRight = 6.dp,
                topLeft = 6.dp
            ),
            style = DrawStyle.Fill
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}