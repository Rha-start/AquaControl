package com.example.aquacontrol.iu.simulacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SimulacionScreen(
    navController: NavController,
    viewModel: SimulacionViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    var menuLineasAbierto by remember(estado.lineas) {
        mutableStateOf(false)
    }

    var menuEscenariosAbierto by remember {
        mutableStateOf(false)
    }

    val lineaSeleccionada = estado.lineas.firstOrNull {
        it.id == estado.lineaSeleccionadaId
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
                text = "Simulación de temperaturas",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Genera mediciones de demostración. " +
                        "No corresponden a lecturas de sensores reales.",
                style = MaterialTheme.typography.bodyLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (estado.activa) {
                            "Simulación activa"
                        } else {
                            "Simulación detenida"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Mediciones guardadas en esta sesión: " +
                                estado.medicionesGeneradas
                    )

                    val ultimaActualizacion = estado.ultimaActualizacion

                    if (ultimaActualizacion != null) {
                        Text(
                            text = "Último guardado: ${
                                formatearFechaSimulacion(ultimaActualizacion)
                            }"
                        )
                    } else {
                        Text(
                            text = "Esta sesión todavía no tiene mediciones guardadas."
                        )
                    }
                }
            }

            Text(
                text = "Cada línea evoluciona de forma independiente, " +
                        "con cambios graduales y episodios aleatorios. " +
                        "Se guarda una lectura por línea aproximadamente " +
                        "cada cinco segundos.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Cada inicio reinicia la secuencia y el contador. " +
                        "Las mediciones anteriores permanecen en el historial. " +
                        "La simulación se detiene al pasar la app a segundo plano.",
                style = MaterialTheme.typography.bodyMedium
            )

            estado.error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = viewModel::iniciar,
                enabled = !estado.activa,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar simulación")
            }

            OutlinedButton(
                onClick = viewModel::detener,
                enabled = estado.activa,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Detener simulación")
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aplicar escenario a una línea",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Seleccionar las opciones no cambia la " +
                                "simulación hasta pulsar Aplicar escenario."
                    )

                    Text(
                        text = "Línea",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuLineasAbierto = true
                            },
                            enabled = estado.lineas.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lineaSeleccionada == null) {
                                    "Seleccionar línea"
                                } else {
                                    "${lineaSeleccionada.nombreGranja} · " +
                                            "${lineaSeleccionada.nombreGalpon} · " +
                                            lineaSeleccionada.nombreLinea
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = menuLineasAbierto,
                            onDismissRequest = {
                                menuLineasAbierto = false
                            }
                        ) {
                            estado.lineas.forEach { linea ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${linea.nombreGranja} · " +
                                                    "${linea.nombreGalpon} · " +
                                                    linea.nombreLinea
                                        )
                                    },
                                    onClick = {
                                        menuLineasAbierto = false
                                        viewModel.seleccionarLinea(linea.id)
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "Escenario",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuEscenariosAbierto = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nombreEscenarioSimulacion(
                                    estado.escenarioSeleccionado
                                )
                            )
                        }

                        DropdownMenu(
                            expanded = menuEscenariosAbierto,
                            onDismissRequest = {
                                menuEscenariosAbierto = false
                            }
                        ) {
                            EscenarioSimulacion.entries.forEach { escenario ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = nombreEscenarioSimulacion(
                                                escenario
                                            )
                                        )
                                    },
                                    onClick = {
                                        menuEscenariosAbierto = false
                                        viewModel.seleccionarEscenario(escenario)
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = "La temperatura avanzará gradualmente hacia " +
                                "el escenario elegido. Al terminar ese episodio, " +
                                "la línea continuará automáticamente; los episodios " +
                                "críticos pasan primero por recuperación.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!estado.activa) {
                        Text(
                            text = "Inicia la simulación para aplicar un escenario."
                        )
                    } else if (lineaSeleccionada == null) {
                        Text(
                            text = "Selecciona la línea que deseas modificar."
                        )
                    }

                    Button(
                        onClick = viewModel::aplicarEscenario,
                        enabled = estado.puedeAplicarEscenario,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar escenario")
                    }

                    estado.mensajeEscenario?.let { mensaje ->
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Routes.ALERTAS) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver alertas")
            }

        }
    }
}

private fun nombreEscenarioSimulacion(
    escenario: EscenarioSimulacion
): String {
    return when (escenario) {
        EscenarioSimulacion.ESTABILIDAD -> "Estabilidad"
        EscenarioSimulacion.CALENTAMIENTO -> "Calentamiento"
        EscenarioSimulacion.ENFRIAMIENTO -> "Enfriamiento"
        EscenarioSimulacion.CRITICO_CALOR -> "Crítico por calor"
        EscenarioSimulacion.CRITICO_FRIO -> "Crítico por frío"
        EscenarioSimulacion.RECUPERACION -> "Recuperación"
    }
}

private fun formatearFechaSimulacion(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formato.format(Date(fechaHora))
}