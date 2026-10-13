package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaPanelResumen
import com.example.aquacontrol.iu.components.AquaTarjetaAccion
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.Blue
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel

@Composable
fun OperarioScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
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
                        text = "PANEL OPERARIO",
                        color = Blue,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = "Control en terreno",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Temperaturas y flushing de tus líneas de agua.",
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                AquaPanelResumen(
                    etiqueta = "CONTROL DE LA OPERACIÓN"
                ) {
                    Text(
                        text = "Revisa, registra y da seguimiento",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Consulta las alertas para identificar las líneas que requieren atención.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                AquaTarjetaAccion(
                    titulo = "Ver granjas",
                    descripcion = "Líneas e historiales por instalación",
                    icono = Icons.Outlined.LocationOn,
                    onClick = {
                        navController.navigate(Routes.GRANJAS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            item {
                AquaTarjetaAccion(
                    titulo = "Alertas",
                    descripcion = "Advertencias y estados críticos",
                    icono = Icons.Outlined.Notifications,
                    onClick = {
                        navController.navigate(Routes.ALERTAS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            item {
                AquaTarjetaAccion(
                    titulo = "Registrar flushing",
                    descripcion = "Registro y evidencia fotográfica",
                    icono = Icons.Outlined.Waves,
                    onClick = {
                        navController.navigate(Routes.FLUSHING) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            item {
                AquaTarjetaAccion(
                    titulo = "Simulación de temperaturas",
                    icono = Icons.Outlined.Science,
                    onClick = {
                        navController.navigate(Routes.SIMULACION) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}