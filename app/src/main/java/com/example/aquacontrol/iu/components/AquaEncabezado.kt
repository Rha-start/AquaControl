package com.example.aquacontrol.iu.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.Red

@Composable
fun AquaEncabezado(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    etiqueta: String? = null
) {
    var presentado by rememberSaveable(titulo) {
        mutableStateOf(false)
    }

    val entrada = remember(titulo) {
        Animatable(if (presentado) 1f else 0f)
    }

    LaunchedEffect(titulo) {
        if (!presentado) {
            presentado = true
            entrada.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 650,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Column(
        modifier = modifier.graphicsLayer {
            alpha = entrada.value
            translationY = 12.dp.toPx() * (1f - entrada.value)
        },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!etiqueta.isNullOrBlank()) {
            Text(
                text = etiqueta,
                color = Red,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = titulo,
            color = Navy,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    color = Red,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        if (!subtitulo.isNullOrBlank()) {
            Text(
                text = subtitulo,
                color = Muted,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}