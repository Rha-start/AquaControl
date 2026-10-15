// Muestra la última medición y los historiales; permite registrar solo con permiso del rol.
// Conecta con: viewmodel/detalle/DetalleLineaViewModel.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.iu.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Waves
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.AquaPanelResumen
import com.example.aquacontrol.iu.components.AquaTarjetaAccion
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.temperatura.GraficoTemperatura
import com.example.aquacontrol.viewmodel.temperatura.GraficoTemperaturaViewModel
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel

@Composable
// Muestra la última medición y los historiales; permite registrar solo con permiso del rol.
fun DetalleLineaScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: DetalleLineaViewModel,
    puedeRegistrar: Boolean = false,
    graficoViewModel: GraficoTemperaturaViewModel? = null
) {
    LaunchedEffect(lineaId, viewModel) {
        viewModel.cargarResumen(lineaId)
    }

    // Observa el ViewModel; cada cambio de estado actualiza la interfaz.
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
            when (val actual = estado) {
                DetalleLineaUiState.Loading -> {
                    item {
                        AquaEncabezado(titulo = "Detalle de línea")
                    }

                    item {
                        CircularProgressIndicator()
                    }
                }

                is DetalleLineaUiState.Success -> {
                    item(key = "encabezado") {
                        AquaEncabezado(
                            titulo = actual.linea.nombre,
                            subtitulo = "${actual.nombreGranja} · " +
                                    actual.nombreGalpon
                        )
                    }

                    item(key = "medicion") {
                        AquaPanelResumen(
                            etiqueta = "Última medición"
                        ) {
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
                                    style = MaterialTheme.typography.displaySmall
                                )

                                IndicadorEstado(
                                    estado = medicion.estado
                                )

                                Text(
                                    text = FechasHistorialTemperatura
                                        .formatearFechaHora(medicion.fechaHora),
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

                    // AppNavHost entrega este ViewModel únicamente al perfil Supervisor.
                    if (graficoViewModel != null) {
                        item(key = "grafico_temperatura") {
                            LaunchedEffect(lineaId, graficoViewModel) {
                                graficoViewModel.cargar(lineaId)
                            }
                            // Suspende la observación cuando la pantalla pasa a segundo plano.
                            val estadoGrafico by graficoViewModel.uiState
                                .collectAsStateWithLifecycle()
                            GraficoTemperatura(
                                estado = estadoGrafico,
                                onReintentar = graficoViewModel::reintentar
                            )
                        }
                    }

                    // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                    if (puedeRegistrar) {
                        item(key = "registrar_temperatura") {
                            AquaTarjetaAccion(
                                titulo = "Registrar temperatura",
                                icono = Icons.Outlined.Thermostat,
                                onClick = {
                                    navController.navigate(
                                        "${Routes.REGISTRAR_TEMPERATURA}/" +
                                                actual.linea.id
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        item(key = "registrar_flushing") {
                            AquaTarjetaAccion(
                                titulo = "Registrar flushing",
                                icono = Icons.Outlined.Waves,
                                onClick = {
                                    navController.navigate(
                                        "${Routes.REGISTRAR_FLUSHING}/" +
                                                actual.linea.id
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    item(key = "historial_temperaturas") {
                        AquaTarjetaAccion(
                            titulo = "Historial de temperaturas",
                            icono = Icons.Outlined.History,
                            onClick = {
                                navController.navigate(
                                    "${Routes.HISTORIAL_TEMPERATURAS}/" +
                                            actual.linea.id
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    item(key = "historial_flushing") {
                        AquaTarjetaAccion(
                            titulo = "Historial de flushing",
                            icono = Icons.Outlined.CalendarMonth,
                            onClick = {
                                navController.navigate(
                                    "${Routes.FLUSHING}/${actual.linea.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                is DetalleLineaUiState.Error -> {
                    item {
                        AquaEncabezado(titulo = "Detalle de línea")
                    }

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
                                viewModel.cargarResumen(lineaId)
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