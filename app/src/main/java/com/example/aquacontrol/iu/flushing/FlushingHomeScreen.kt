package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.viewmodel.flushing.SeleccionFlushingViewModel

@Composable
fun FlushingHomeScreen(
    navController: NavController,
    viewModel: SeleccionFlushingViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    val granjaSeleccionada = estado.granjas
        .firstOrNull { it.id == estado.granjaId }

    val galponSeleccionado = estado.galpones
        .firstOrNull { it.id == estado.galponId }

    val lineaSeleccionada = estado.lineas
        .firstOrNull { it.id == estado.lineaId }

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
                text = "Registrar flushin",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Selecciona la granja, el galpón y la línea donde realizaste el procedimiento.",
                style = MaterialTheme.typography.bodyLarge
            )

            SelectorFlushing(
                titulo = "Granja",
                seleccion = granjaSeleccionada?.nombre,
                opciones = estado.granjas.map { it.id to it.nombre },
                habilitado = estado.granjas.isNotEmpty(),
                onSeleccionar = viewModel::seleccionarGranja
            )

            SelectorFlushing(
                titulo = "Galpón",
                seleccion = galponSeleccionado?.nombre,
                opciones = estado.galpones.map { it.id to it.nombre },
                habilitado = estado.granjaId != null &&
                        estado.galpones.isNotEmpty(),
                onSeleccionar = viewModel::seleccionarGalpon
            )

            SelectorFlushing(
                titulo = "Línea de bebedero",
                seleccion = lineaSeleccionada?.nombre,
                opciones = estado.lineas.map { it.id to it.nombre },
                habilitado = estado.galponId != null &&
                        estado.lineas.isNotEmpty(),
                onSeleccionar = viewModel::seleccionarLinea
            )

            if (estado.error == null) {
                val mensaje = when {
                    estado.granjas.isEmpty() ->
                        "No hay granjas disponibles."

                    estado.granjaId != null && estado.galpones.isEmpty() ->
                        "La granja seleccionada no tiene galpones disponibles."

                    estado.galponId != null && estado.lineas.isEmpty() ->
                        "El galpón seleccionado no tiene líneas disponibles."

                    else -> null
                }

                mensaje?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            estado.error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (estado.granjas.isEmpty()) {
                    OutlinedButton(
                        onClick = viewModel::cargarGranjas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            Button(
                onClick = {
                    val lineaId = estado.lineaId

                    if (estado.puedeContinuar && lineaId != null) {
                        navController.navigate(
                            "${Routes.REGISTRAR_FLUSHING}/$lineaId"
                        ) {
                            launchSingleTop = true
                        }
                    }
                },
                enabled = estado.puedeContinuar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar al registro")
            }

        }
    }
}

@Composable
private fun SelectorFlushing(
    titulo: String,
    seleccion: String?,
    opciones: List<Pair<Int, String>>,
    habilitado: Boolean,
    onSeleccionar: (Int) -> Unit
) {
    var expandido by remember(opciones, habilitado) {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleSmall
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expandido = true },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = seleccion ?: "Seleccionar ${titulo.lowercase()}"
                )
            }

            DropdownMenu(
                expanded = expandido && habilitado,
                onDismissRequest = { expandido = false }
            ) {
                opciones.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            expandido = false
                            onSeleccionar(id)
                        }
                    )
                }
            }
        }
    }
}