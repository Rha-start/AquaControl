package com.example.aquacontrol.iu.alertas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleAlertaScreen(
    navController: NavController,
    lineaId: Int,
    medicionId: Long,
    viewModel: DetalleLineaViewModel
) {
    LaunchedEffect(lineaId, viewModel) {
        viewModel.cargarHistorial(lineaId)
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
            item {
                Text(
                    text = "Detalle de alerta",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            when (val actual = estado) {
                DetalleLineaUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is DetalleLineaUiState.Success -> {
                    val medicion = actual.historial.firstOrNull {
                        it.id == medicionId && it.lineaId == lineaId
                    }

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${actual.nombreGranja} · ${actual.nombreGalpon}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = actual.linea.nombre,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    if (medicion == null) {
                        item {
                            Text(
                                text = "La medición de esta alerta ya no está disponible.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IndicadorEstado(
                                        estado = medicion.estado,
                                        destacado = true
                                    )

                                    Text(
                                        text = "${medicion.temperatura} °C",
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    Text(
                                        text = "Medición que originó el aviso",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = SimpleDateFormat(
                                            "dd/MM/yyyy HH:mm:ss",
                                            Locale.getDefault()
                                        ).format(Date(medicion.fechaHora)),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    val origen = when (medicion.origen) {
                                        OrigenMedicion.MANUAL -> "Manual"
                                        OrigenMedicion.SIMULADA -> "Simulada"
                                    }

                                    Text(
                                        text = "Origen: $origen",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                navController.navigate(
                                    "${Routes.DETALLE_LINEA}/$lineaId"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver estado actual e historial")
                        }
                    }
                }

                is DetalleLineaUiState.Error -> {
                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.cargarHistorial(lineaId)
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