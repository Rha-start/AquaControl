package com.example.aquacontrol.iu.roles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.AzulAriztia
import com.example.aquacontrol.ui.theme.GrisClaro
import com.example.aquacontrol.ui.theme.GrisTexto
import com.example.aquacontrol.ui.theme.RojoAriztia

@Composable
fun SupervisorScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
    val perfil = perfilViewModel.perfilSeleccionado.collectAsState().value
    val nombre = perfilViewModel.nombreUsuario.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisClaro)
            .padding(16.dp)
    ) {
        // Encabezado
        Text("Panel Supervisor", color = AzulAriztia)
        Text("Hola, $nombre", color = GrisTexto)

        // Mostrar rol seleccionado
        Text(
            "Rol seleccionado: ${perfil?.name ?: "Sin rol"}",
            color = GrisTexto
        )

        Spacer(Modifier.height(16.dp))

        // Tarjeta de resumen
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen hoy", color = GrisTexto)
                Text("94% de las líneas en rango térmico óptimo.", color = GrisTexto)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botón: Ver Galpones
        Button(
            onClick = { navController.navigate(Routes.GRANJAS) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AzulAriztia)
        ) {
            Text("Ver Galpones", color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        // Botón: Historial
        Button(
            onClick = { /* Navegar a historial cuando exista */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AzulAriztia)
        ) {
            Text("Historial", color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        // Botón: Eventos críticos
        Button(
            onClick = { /* Navegar a eventos críticos cuando exista */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RojoAriztia)
        ) {
            Text("Eventos Críticos", color = Color.White)
        }

        Spacer(Modifier.height(24.dp))

    }
}
