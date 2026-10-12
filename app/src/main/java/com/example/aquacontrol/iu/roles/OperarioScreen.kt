package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.AmarilloAdvertencia
import com.example.aquacontrol.ui.theme.AzulAriztia
import com.example.aquacontrol.ui.theme.RojoAriztia
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel

@Composable
fun OperarioScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
    val nombre by perfilViewModel.nombreUsuario.collectAsState()

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
                text = "Panel Operario",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Hola, $nombre",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Consulta las líneas de agua y registra los procedimientos realizados.",
                style = MaterialTheme.typography.bodyLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Control de la operación",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Revisa las temperaturas y consulta las alertas para identificar líneas que requieren atención.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    navController.navigate(Routes.GRANJAS) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulAriztia,
                    contentColor = Color.White
                )
            ) {
                Text("Ver Granjas")
            }

            Button(
                onClick = {
                    navController.navigate(Routes.ALERTAS) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmarilloAdvertencia,
                    contentColor = Color.Black
                )
            ) {
                Text("Alertas")
            }

            Button(
                onClick = {
                    navController.navigate(Routes.FLUSHING) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RojoAriztia,
                    contentColor = Color.White
                )
            ) {
                Text("Registrar flushin")
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Routes.SIMULACION) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simulación de temperaturas")
            }

        }
    }
}