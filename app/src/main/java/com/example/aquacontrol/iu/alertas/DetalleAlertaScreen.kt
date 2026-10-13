package com.example.aquacontrol.iu.alertas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
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
import com.example.aquacontrol.iu.components.AquaTarjetaAccion
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.ui.theme.ContenidoAdvertenciaClaro
import com.example.aquacontrol.ui.theme.ContenidoCriticoClaro
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel

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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "encabezado") {
                AquaEncabezado(
                    titulo = "Detalle de alerta",
                    subtitulo = "Medición que originó el aviso"
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

                    item(key = "ubicacion") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${actual.nombreGranja} · " +
                                        actual.nombreGalpon,
                                color = Muted,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = actual.linea.nombre,
                                color = Navy,
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
                        item(key = "medicion") {
                            val colorEstado = when (medicion.estado) {
                                EstadoLinea.CRITICO -> ContenidoCriticoClaro
                                EstadoLinea.ADVERTENCIA ->
                                    ContenidoAdvertenciaClaro
                                EstadoLinea.NORMAL -> Navy
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = colorEstado.copy(alpha = 0.45f)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surface,
                                    contentColor = Navy
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 5.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(14.dp)
                                ) {
                                    IndicadorEstado(
                                        estado = medicion.estado,
                                        destacado = true
                                    )

                                    Text(
                                        text = "${medicion.temperatura} °C",
                                        color = colorEstado,
                                        style =
                                            MaterialTheme.typography.displaySmall
                                    )

                                    Text(
                                        text = FechasHistorialTemperatura
                                            .formatearFechaHora(
                                                medicion.fechaHora
                                            ),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    val origen = when (medicion.origen) {
                                        OrigenMedicion.MANUAL -> "Manual"
                                        OrigenMedicion.SIMULADA -> "Simulada"
                                    }

                                    Text(
                                        text = "Origen: $origen",
                                        color = Muted,
                                        style =
                                            MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    item(key = "estado_actual") {
                        AquaTarjetaAccion(
                            titulo = "Ver estado actual",
                            descripcion = "Detalle e historiales de la línea",
                            icono = Icons.Outlined.Thermostat,
                            onClick = {
                                navController.navigate(
                                    "${Routes.DETALLE_LINEA}/$lineaId"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                is DetalleLineaUiState.Error -> {
                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                viewModel.cargarHistorial(lineaId)
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
}