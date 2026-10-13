package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.AquaPanelResumen
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel

@Composable
fun FlushingDetalleScreen(
    navController: NavController,
    lineaId: Int,
    eventoId: Int,
    viewModel: FlushingViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(lineaId, eventoId, viewModel) {
        viewModel.cargarFlushing(lineaId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AquaEncabezado(
                titulo = "Detalle de flushing"
            )

            when (val actual = estado) {
                FlushingUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is FlushingUiState.Success -> {
                    val evento = actual.eventos.firstOrNull {
                        it.id == eventoId && it.lineaId == lineaId
                    }

                    if (evento == null) {
                        Text(
                            text = "No se encontró el registro seleccionado.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = evento.fechaHora,
                            color = Navy,
                            style = MaterialTheme.typography.titleMedium
                        )

                        AquaPanelResumen(
                            etiqueta = "Temperaturas del procedimiento"
                        ) {
                            Text(
                                text = "Antes",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Text(
                                text = "${evento.temperaturaAntes} °C",
                                style = MaterialTheme.typography.headlineLarge
                            )

                            Text(
                                text = "Después",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Text(
                                text = "${evento.temperaturaDespues} °C",
                                style = MaterialTheme.typography.headlineLarge
                            )

                            Text(
                                text = "Duración: " +
                                        "${evento.duracionSegundos} segundos",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        SeccionDetalleFlushing(
                            titulo = "Observación"
                        ) {
                            Text(
                                text = evento.observacion.ifBlank {
                                    "Sin observación"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        SeccionDetalleFlushing(
                            titulo = "Foto de evidencia"
                        ) {
                            val foto = evento.fotoRuta

                            if (foto.isNullOrBlank()) {
                                Text(
                                    text = "Este registro no tiene foto de evidencia.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            } else {
                                FotoFlushing(fotoRuta = foto)
                            }
                        }
                    }
                }

                FlushingUiState.Empty -> {
                    Text(
                        text = "Esta línea todavía no tiene registros de flushing.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is FlushingUiState.Error -> {
                    Text(
                        text = actual.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.cargarFlushing(lineaId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionDetalleFlushing(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Navy.copy(alpha = 0.08f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Navy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium
            )

            contenido()
        }
    }
}