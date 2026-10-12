package com.example.aquacontrol.iu.roles

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.viewmodel.supervisor.SupervisorUiState
import com.example.aquacontrol.viewmodel.supervisor.SupervisorViewModel

@Composable
fun SupervisorScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel,
    viewModel: SupervisorViewModel
) {
    val nombre by perfilViewModel.nombreUsuario.collectAsState()
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Panel supervisor",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (nombre.isNotBlank()) {
                        Text(
                            text = "Hola, $nombre",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
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
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Estado de las líneas",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                if (actual.totalLineas == 0) {
                                    Text(
                                        text = "No hay líneas disponibles.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else {
                                    Text(
                                        text = "Normal: ${actual.lineasNormales}",
                                        style = MaterialTheme.typography.bodyLarge
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
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
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
                Button(
                    onClick = {
                        navController.navigate(Routes.GRANJAS) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Líneas e historiales",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        navController.navigate(Routes.ALERTAS) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "Ver alertas",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}