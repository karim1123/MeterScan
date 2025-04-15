package com.gabbasov.meterscan.meters.presentation.details.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabbasov.meterscan.meters.domain.MeterReading
import com.gabbasov.meterscan.meters.domain.MeterType
import com.gabbasov.meterscan.meters.presentation.details.getMeterUnits
import com.gabbasov.meterscan.meters.presentation.details.prepareMonthlyData
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearConsumptionChart(
    modifier: Modifier = Modifier,
    readings: List<MeterReading>,
    meterType: MeterType,
    onMonthSelected: (Month) -> Unit
) {
    val mainColor = when (meterType) {
        MeterType.ELECTRICITY -> Color(0xFFFFC107) // Желтый для электричества
        MeterType.WATER -> Color(0xFF2196F3) // Синий для воды
        MeterType.GAS -> Color(0xFFFF5722) // Оранжевый для газа
    }

    val chartBrush = Brush.verticalGradient(
        colors = listOf(
            mainColor.copy(alpha = 0.7f),
            mainColor
        )
    )

    val monthMap = mutableMapOf<Int, Month>()

    val monthlyData = prepareMonthlyData(readings).map { (month, reading) ->
        val monthId = month.ordinal + 1 // Используем порядковый номер месяца + 1 как ID
        monthMap[monthId] = month // Сохраняем соответствие ID и месяца

        Bars(
            label = month.getDisplayName(TextStyle.SHORT, Locale("ru")).take(3).uppercase(),
            values = listOf(
                Bars.Data(
                    id = monthId, // Используем месяц как уникальный ID
                    label = "Потребление",
                    value = reading,
                    color = chartBrush
                )
            )
        )
    }

    ColumnChart(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        data = monthlyData,
        onBarClick = { barData ->
            val clickedMonth = monthMap[barData.id]

            if (clickedMonth != null) {
                onMonthSelected(clickedMonth)
            }
        },
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
        indicatorProperties = HorizontalIndicatorProperties(enabled = false),
        popupProperties = PopupProperties(
            enabled = true,
            contentBuilder = { value -> "${value.toInt()} ${getMeterUnits(meterType)}" },
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    )
}

@Composable
@Preview(showBackground = true)
fun YearConsumptionChartPreview() {
    val readings = listOf(
        MeterReading(
            date = java.time.LocalDate.of(2023, 1, 1),
            value = 100.0
        ),
        MeterReading(
            date = java.time.LocalDate.of(2023, 2, 1),
            value = 150.0
        ),
        MeterReading(
            date = java.time.LocalDate.of(2023, 3, 1),
            value = 200.0
        )
    )

    YearConsumptionChart(
        readings = readings,
        meterType = MeterType.ELECTRICITY,
        onMonthSelected = { month ->
            Log.d("YearConsumptionChart", "Selected month: $month")
        }
    )
}
