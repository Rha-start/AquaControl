package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaPanelResumen
import com.example.aquacontrol.iu.components.AquaTarjetaAccion
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.Blue
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.viewmodel.supervisor.SupervisorUiState
import com.example.aquacontrol.viewmodel.supervisor.SupervisorViewModel

@Composable
fun SupervisorScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel,
    viewModel: SupervisorViewModel
) {
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
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PANEL SUPERVISOR",
                        color = Blue,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = "Visión de operación",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Estado de las líneas bajo tu supervisión.",
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            when (val actual = estado) {
                SupervisorUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is SupervisorUiState.Success -> {
                    item {
                        AquaPanelResumen(
                            etiqueta = "ESTADO ACTUAL"
                        ) {
                            if (actual.totalLineas == 0) {
                                Text(
                                    text = "No hay líneas disponibles.",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            } else {
                                Text(
                                    text = "${actual.lineasNormales} de " +
                                            "${actual.totalLineas} líneas " +
                                            "en rango normal",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Text(
                                    text = "Advertencia: " +
                                            actual.lineasEnAdvertencia,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Crítico: ${actual.lineasCriticas}",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Sin mediciones: " +
                                            actual.lineasSinMediciones,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = "Según la última medición de cada línea.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                is SupervisorUiState.Error -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = actual.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarResumen()
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

            item {
                AquaTarjetaAccion(
                    titulo = "Líneas e historiales",
                    descripcion = "Temperaturas y flushing por instalación",
                    icono = Icons.Outlined.Warehouse,
                    onClick = {
                        navController.navigate(Routes.GRANJAS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            item {
                AquaTarjetaAccion(
                    titulo = "Ver alertas",
                    descripcion = "Líneas que requieren atención",
                    icono = Icons.Outlined.Notifications,
                    onClick = {
                        navController.navigate(Routes.ALERTAS) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}