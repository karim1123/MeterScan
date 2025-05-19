package com.gabbasov.meterscan.common.ui.components.meters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.common.R
import com.gabbasov.meterscan.model.meter.MeterType

@Composable
fun MeterTypeIcon(
    type: MeterType,
    modifier: Modifier = Modifier,
    size: Float = 40f,
) {
    val (icon, color) = when (type) {
        MeterType.ELECTRICITY -> Pair(
            R.drawable.ic_electricity,
            Color(0xFFFFC107) // Желтый для электричества
        )

        MeterType.WATER -> Pair(
            R.drawable.ic_water,
            Color(0xFF2196F3) // Синий для воды
        )

        MeterType.GAS -> Pair(
            R.drawable.ic_gas,
            Color(0xFFFF5722) // Оранжевый для газа
        )
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = "Тип счетчика: $type",
            tint = color,
            modifier = Modifier
                .size((size * 0.6f).dp)
                .padding(4.dp)
        )
    }
}

@Preview
@Composable
private fun MeterTypeIconPreview() {
    MaterialTheme {
        MeterTypeIcon(type = MeterType.ELECTRICITY)
    }
}