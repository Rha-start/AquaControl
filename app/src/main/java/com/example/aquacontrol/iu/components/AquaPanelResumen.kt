package com.example.aquacontrol.iu.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.ui.theme.AzulPanelAqua

@Composable
fun AquaPanelResumen(
    etiqueta: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AzulPanelAqua)
    ) {
        Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            val radio = size.height * 0.48f
            val centro = Offset(
                x = size.width * 0.88f,
                y = size.height * 0.65f
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = radio,
                center = centro,
                style = Stroke(width = 18.dp.toPx())
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = radio * 1.4f,
                center = centro,
                style = Stroke(width = 12.dp.toPx())
            )
        }

        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )

                content()
            }
        }
    }
}