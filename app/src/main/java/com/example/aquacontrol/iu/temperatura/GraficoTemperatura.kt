package com.example.aquacontrol.iu.temperatura

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.viewmodel.temperatura.GraficoTemperaturaUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Dibuja el estado recibido del ViewModel; no consulta ni modifica la base de datos.
@Composable
fun GraficoTemperatura(
    estado: GraficoTemperaturaUiState,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Últimas 24 horas", style = MaterialTheme.typography.titleLarge)
            when {
                estado.cargando -> CircularProgressIndicator()
                estado.error != null -> {
                    Text(estado.error, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(
                        onClick = onReintentar,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) { Text("Reintentar") }
                }
                estado.mediciones.isEmpty() -> {
                    Text("No hay mediciones guardadas en las últimas 24 horas.")
                }
                else -> CurvaTemperatura(estado)
            }
        }
    }
}

@Composable
private fun CurvaTemperatura(estado: GraficoTemperaturaUiState) {
    // Mantiene únicamente puntos válidos del intervalo, en orden cronológico.
    val puntos = remember(estado.mediciones, estado.desdeInclusive, estado.hastaInclusive) {
        estado.mediciones.filter {
            it.temperatura.isFinite() &&
                    it.fechaHora in estado.desdeInclusive..estado.hastaInclusive
        }.sortedWith(compareBy({ it.fechaHora }, { it.id }))
    }
    if (puntos.isEmpty() || estado.hastaInclusive <= estado.desdeInclusive) {
        Text("No hay datos disponibles para dibujar el gráfico.")
        return
    }

    val minimo = puntos.minOf { it.temperatura }
    val maximo = puntos.maxOf { it.temperatura }
    // Incluye el rango normal como referencia incluso si todas las lecturas son iguales.
    val inferior = minOf(minimo, 18.0) - 1.0
    val superior = maxOf(maximo, 21.0) + 1.0
    val amplitud = superior - inferior
    if (!amplitud.isFinite() || amplitud <= 0.0) {
        Text("Los valores registrados exceden la escala disponible.")
        return
    }

    val formatoFecha = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val inicio = formatoFecha.format(Date(estado.desdeInclusive))
    val fin = formatoFecha.format(Date(estado.hastaInclusive))
    val colorLinea = MaterialTheme.colorScheme.primary
    val colorRejilla = MaterialTheme.colorScheme.outlineVariant
    val verde = Color(0xFF26733D)
    val amarillo = Color(0xFF986A00)
    val rojo = Color(0xFFBA2525)

    Text("Temperatura (°C)", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Etiquetas verticales fuera del lienzo para conservar el tamaño de letra del sistema.
        Column(
            modifier = Modifier.height(220.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatearGrados(superior), style = MaterialTheme.typography.bodySmall)
            Text(formatearGrados(inferior + amplitud / 2), style = MaterialTheme.typography.bodySmall)
            Text(formatearGrados(inferior), style = MaterialTheme.typography.bodySmall)
        }
        Canvas(
            modifier = Modifier.weight(1f).height(220.dp).semantics {
                contentDescription = "${puntos.size} mediciones entre $inicio y $fin. " +
                        "Mínima ${formatearGrados(minimo)} grados; " +
                        "máxima ${formatearGrados(maximo)} grados."
            }
        ) {
            val margen = 5.dp.toPx()
            val ancho = (size.width - margen * 2).coerceAtLeast(1f)
            val alto = (size.height - margen * 2).coerceAtLeast(1f)
            fun y(valor: Double): Float =
                margen + ((superior - valor) / amplitud).toFloat() * alto

            // Franja de referencia normal: coincide con EstadoLinea (18 a 21 °C).
            drawRect(
                color = verde.copy(alpha = 0.12f),
                topLeft = Offset(margen, y(21.0)),
                size = androidx.compose.ui.geometry.Size(ancho, y(18.0) - y(21.0))
            )
            for (division in 0..4) {
                val altura = margen + alto * division / 4f
                drawLine(colorRejilla, Offset(margen, altura), Offset(margen + ancho, altura))
            }

            // El eje horizontal representa tiempo real, no posiciones equidistantes.
            val posiciones = puntos.map { medicion ->
                val proporcion = (medicion.fechaHora - estado.desdeInclusive).toDouble() /
                        (estado.hastaInclusive - estado.desdeInclusive).toDouble()
                Offset(margen + proporcion.toFloat() * ancho, y(medicion.temperatura))
            }
            // Une registros consecutivos sin suavizar ni generar mediciones intermedias.
            if (posiciones.size > 1) {
                val trazado = Path().apply {
                    moveTo(posiciones.first().x, posiciones.first().y)
                    posiciones.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(trazado, colorLinea, style = Stroke(width = 2.dp.toPx()))
            }
            posiciones.forEachIndexed { indice, posicion ->
                val color = when (puntos[indice].estado) {
                    EstadoLinea.NORMAL -> verde
                    EstadoLinea.ADVERTENCIA -> amarillo
                    EstadoLinea.CRITICO -> rojo
                }
                drawCircle(color, radius = 3.dp.toPx(), center = posicion)
            }
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(inicio, style = MaterialTheme.typography.bodySmall)
        Text(fin, style = MaterialTheme.typography.bodySmall)
    }
    Text(
        "Mín. ${formatearGrados(minimo)} °C · Máx. ${formatearGrados(maximo)} °C",
        style = MaterialTheme.typography.bodyMedium
    )
    Text(
        if (puntos.size == 1) "Una medición: aún no hay evolución para comparar."
        else "${puntos.size} mediciones guardadas · actualización cada 30 s",
        style = MaterialTheme.typography.bodySmall
    )
    Text(
        "Franja verde: 18–21 °C. Los puntos indican normal, advertencia o crítico. " +
                "Las líneas unen registros; no representan medición continua.",
        style = MaterialTheme.typography.bodySmall
    )
}

// Formato compartido por las etiquetas y la descripción accesible del gráfico.
private fun formatearGrados(valor: Double): String =
    String.format(Locale.getDefault(), "%.1f", valor)
