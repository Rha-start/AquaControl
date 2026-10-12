package com.example.aquacontrol.iu.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel

@Composable
fun DetalleLineaScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: DetalleLineaViewModel
) {
    LaunchedEffect(lineaId, viewModel) {
        viewModel.cargarResumen(lineaId)
    }

    val estado by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val actual = estado) {
                DetalleLineaUiState.Loading -> {
                    item {
                        Text(
                            text = "Detalle de línea",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    item {
                        CircularProgressIndicator()
                    }
                }

                is DetalleLineaUiState.Success -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = actual.linea.nombre,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Text(
                                text = "${actual.nombreGranja} · " +
                                        actual.nombreGalpon,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Última medición",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                val medicion = actual.linea.ultimaMedicion

                                if (medicion == null) {
                                    IndicadorEstado(estado = null)

                                    Text(
                                        text = "Esta línea todavía no tiene mediciones.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else {
                                    Text(
                                        text = "${medicion.temperatura} °C",
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    IndicadorEstado(
                                        estado = medicion.estado
                                    )

                                    Text(
                                        text = FechasHistorialTemperatura
                                            .formatearFechaHora(
                                                medicion.fechaHora
                                            ),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                navController.navigate(
                                    "${Routes.REGISTRAR_TEMPERATURA}/" +
                                            actual.linea.id
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "Registrar temperatura",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                navController.navigate(
                                    "${Routes.HISTORIAL_TEMPERATURAS}/" +
                                            actual.linea.id
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "Historial de temperaturas",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                navController.navigate(
                                    "${Routes.FLUSHING}/${actual.linea.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "Historial de flushing",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                is DetalleLineaUiState.Error -> {
                    item {
                        Text(
                            text = "Detalle de línea",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.cargarResumen(lineaId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}