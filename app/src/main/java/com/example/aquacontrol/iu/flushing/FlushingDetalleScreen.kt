package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalle de flushing",
                style = MaterialTheme.typography.headlineSmall
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
                            text = "Fecha y hora: ${evento.fechaHora}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Temperatura antes: " +
                                    "${evento.temperaturaAntes} °C",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Temperatura después: " +
                                    "${evento.temperaturaDespues} °C",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Duración: " +
                                    "${evento.duracionSegundos} segundos",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Observación",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = evento.observacion.ifBlank {
                                "Sin observación"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Foto de evidencia",
                            style = MaterialTheme.typography.titleMedium
                        )

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

                    Button(
                        onClick = {
                            viewModel.cargarFlushing(lineaId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al historial")
            }
        }
    }
}