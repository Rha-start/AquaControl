package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.model.flushing.FechasCalendarioFlushing

@Composable
fun CalendarioFlushing(
    mesVisible: Long,
    diaSeleccionado: Long?,
    diasConEventos: Set<Long>,
    onCambiarMes: (Long) -> Unit,
    onSeleccionarDia: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val fechas = FechasCalendarioFlushing
    val inicioMes = fechas.inicioMes(mesVisible)
    val espaciosIniciales = fechas.espaciosAntesDelPrimerDia(inicioMes)
    val cantidadDias = fechas.diasDelMes(inicioMes)
    val cantidadSemanas = (espaciosIniciales + cantidadDias + 6) / 7

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = fechas.tituloMes(inicioMes),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onCambiarMes(fechas.moverMes(inicioMes, -1))
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text("Mes anterior")
            }

            OutlinedButton(
                onClick = {
                    onCambiarMes(fechas.moverMes(inicioMes, 1))
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text("Mes siguiente")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { nombre ->
                Text(
                    text = nombre,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        repeat(cantidadSemanas) { semana ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(7) { columna ->
                    val numeroDia =
                        semana * 7 + columna - espaciosIniciales + 1

                    if (numeroDia !in 1..cantidadDias) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                        )
                    } else {
                        val dia = fechas.fechaDelDia(inicioMes, numeroDia)
                        val tieneEventos = dia in diasConEventos
                        val esSeleccionado = dia == diaSeleccionado

                        val fondo = if (esSeleccionado) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }

                        val colorTexto = if (esSeleccionado) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        val descripcion = buildString {
                            append(fechas.tituloDia(dia))
                            append(
                                if (tieneEventos) {
                                    ", con registros de flushing"
                                } else {
                                    ", sin registros de flushing"
                                }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(fondo)
                                .clickable {
                                    onSeleccionarDia(dia)
                                }
                                .semantics(mergeDescendants = true) {
                                    contentDescription = descripcion
                                    selected = esSeleccionado
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = numeroDia.toString(),
                                color = colorTexto,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (esSeleccionado) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )

                            if (tieneEventos) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = colorTexto,
                                            shape = CircleShape
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.size(6.dp))
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
            )

            Text(
                text = "Día con registros de flushing",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}