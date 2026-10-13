package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.Navy
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AquaEncabezado(
                titulo = "Registrar flushing",
                subtitulo = "Selecciona la ubicación"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = Navy.copy(alpha = 0.08f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Navy
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
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
                        titulo = "Línea",
                        seleccion = lineaSeleccionada?.nombre,
                        opciones = estado.lineas.map { it.id to it.nombre },
                        habilitado = estado.galponId != null &&
                                estado.lineas.isNotEmpty(),
                        onSeleccionar = viewModel::seleccionarLinea
                    )
                }
            }

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
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            estado.error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (estado.granjas.isEmpty()) {
                    OutlinedButton(
                        onClick = viewModel::cargarGranjas,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
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
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp,
                    disabledElevation = 0.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Continuar al registro",
                    style = MaterialTheme.typography.labelLarge
                )
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedButton(
            onClick = { expandido = true },
            enabled = habilitado,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = seleccion ?: "Seleccionar",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
    }

    if (expandido && habilitado) {
        AlertDialog(
            onDismissRequest = { expandido = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = titulo,
                    color = Navy,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = opciones,
                        key = { it.first }
                    ) { (id, nombre) ->
                        OutlinedButton(
                            onClick = {
                                expandido = false
                                onSeleccionar(id)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        ) {
                            Text(
                                text = nombre,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { expandido = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}