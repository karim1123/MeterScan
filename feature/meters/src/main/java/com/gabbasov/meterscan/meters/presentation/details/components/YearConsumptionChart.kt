package com.gabbasov.meterscan.meters.presentation.details.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabbasov.meterscan.meters.domain.MeterReading
import com.gabbasov.meterscan.meters.domain.MeterType
import com.gabbasov.meterscan.meters.presentation.details.prepareMonthlyData
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearConsumptionChart(
    modifier: Modifier = Modifier,
    readings: List<MeterReading>,
    meterType: MeterType
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
            mainColor.copy(alpha = 0.7f),
            mainColor
        )
    )

    // Подготавливаем данные по месяцам, включая пустые месяцы
    val monthlyData = prepareMonthlyData(readings).map { (month, consumption) ->
        Bars(
            label = month.getDisplayName(TextStyle.SHORT, Locale("ru")).take(3).uppercase(),
            values = listOf(
                Bars.Data(
                    label = "Потребление",
                    value = consumption,
                    color = chartBrush
                )
            )
        )
    }

    // Отображаем график
    ColumnChart(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        data = monthlyData,
        barProperties = BarProperties(
            thickness = 16.dp,
            spacing = 4.dp,
            cornerRadius = Bars.Data.Radius.Rectangle(
                topRight = 6.dp,
                topLeft = 6.dp
            ),
            style = DrawStyle.Fill
        ),
        labelProperties = LabelProperties(
            textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                color = Color.White,
                fontSize = 12.sp
            ),
            enabled = true,
            rotation = LabelProperties.Rotation(mode = LabelProperties.Rotation.Mode.Force)
        ),
        indicatorProperties = HorizontalIndicatorProperties(enabled = false)
    )
}

@Preview
@Composable
fun YearConsumptionChartPreview() {
    YearConsumptionChart(
        readings = listOf(
            MeterReading(date = java.time.LocalDate.now(), value = 100.0),
            MeterReading(date = java.time.LocalDate.now().minusMonths(1), value = 200.0)
        ),
        meterType = MeterType.ELECTRICITY
    )
}