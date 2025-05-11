package com.gabbasov.meterscan.scan.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecognitionProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,
    stabilityScore: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "ProgressAnimation"
    )

    val indicatorColor = when {
        stabilityScore > 0.9f -> Color(0xFF4CAF50) // Зеленый - высокая стабильность
        stabilityScore > 0.7f -> Color(0xFF8BC34A) // Светло-зеленый
        stabilityScore > 0.5f -> Color(0xFFFFC107) // Желтый
        stabilityScore > 0.3f -> Color(0xFFFF9800) // Оранжевый
        else -> Color(0xFFCCCCCC) // Серый - накопление
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x80000000))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = indicatorColor,
                trackColor = Color(0x33FFFFFF)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (stabilityScore > 0.3f) {
                    "Стабильность: ${(stabilityScore * 100).toInt()}%"
                } else {
                    "Накопление данных..."
                },
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
