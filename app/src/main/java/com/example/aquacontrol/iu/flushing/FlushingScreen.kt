package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.AquaTarjetaAccion
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FechasCalendarioFlushing
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel

@Composable
fun FlushingScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: FlushingViewModel,
    puedeRegistrar: Boolean = false
) {
    val estado by viewModel.uiState.collectAsState()
    val backStackEntry = navController.currentBackStackEntry

    var mesVisible by rememberSaveable(lineaId) {
        mutableStateOf(
            FechasCalendarioFlushing.inicioMes(
                FechasCalendarioFlushing.hoy()
            )
        )
    }

    var diaSeleccionado by rememberSaveable(lineaId) {
        mutableStateOf<Long?>(FechasCalendarioFlushing.hoy())
    }

    val eventos = (estado as? FlushingUiState.Success)
        ?.eventos.orEmpty()

    val eventosPorDia = remember(eventos) {
        eventos.groupBy { evento ->
            FechasCalendarioFlushing.diaDelRegistro(evento.fechaHora)
        }
    }

    val diasConEventos = remember(eventosPorDia) {
        eventosPorDia.keys.filterNotNull().toSet()
    }

    val eventosDelDia = eventosPorDia[diaSeleccionado].orEmpty()
    val eventosSinFecha = eventosPorDia[null].orEmpty()

    DisposableEffect(backStackEntry, lineaId, viewModel) {
        val lifecycle = backStackEntry?.lifecycle

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarFlushing(lineaId)
            }
        }

        if (lifecycle != null) {
            lifecycle.addObserver(observer)
        } else {
            viewModel.cargarFlushing(lineaId)
        }

        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

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
                AquaEncabezado(
                    titulo = "Historial de flushing"
                )
            }

            if (puedeRegistrar) {
                item {
                    Button(
                        onClick = {
                            navController.navigate(
                                "${Routes.REGISTRAR_FLUSHING}/$lineaId"
                            ) {
                                launchSingleTop = true
                            }
                        },
                        enabled = lineaId > 0,
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
                            text = "Registrar flushing",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            when (val actual = estado) {
                FlushingUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is FlushingUiState.Error -> {
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
                                    viewModel.cargarFlushing(lineaId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                FlushingUiState.Empty,
                is FlushingUiState.Success -> {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            CalendarioFlushing(
                                modifier = Modifier.padding(12.dp),
                                mesVisible = mesVisible,
                                diaSeleccionado = diaSeleccionado,
                                diasConEventos = diasConEventos,
                                onCambiarMes = { nuevoMes ->
                                    mesVisible = nuevoMes
                                    diaSeleccionado = null
                                },
                                onSeleccionarDia = { dia ->
                                    diaSeleccionado = dia
                                }
                            )
                        }
                    }

                    item {
                        val dia = diaSeleccionado

                        if (dia == null) {
                            Text(
                                text = "Selecciona un día del calendario.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Flushing del " +
                                            FechasCalendarioFlushing
                                                .tituloDia(dia),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                if (eventosDelDia.isEmpty()) {
                                    Text(
                                        text = if (actual is FlushingUiState.Empty) {
                                            "Esta línea todavía no tiene registros de flushing."
                                        } else {
                                            "No hay registros para este día."
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else {
                                    Text(
                                        text = if (eventosDelDia.size == 1) {
                                            "1 registro"
                                        } else {
                                            "${eventosDelDia.size} registros"
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    if (diaSeleccionado != null) {
                        items(
                            items = eventosDelDia,
                            key = { evento -> evento.id }
                        ) { evento ->
                            RegistroFlushingCalendario(
                                evento = evento,
                                onVerDetalle = {
                                    navController.navigate(
                                        "${Routes.DETALLE_FLUSHING}/" +
                                                "${evento.lineaId}/${evento.id}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }

                    // Conserva el acceso a registros antiguos si su fecha
                    // no coincide con el formato utilizado actualmente.
                    if (eventosSinFecha.isNotEmpty()) {
                        item {
                            Text(
                                text = "Registros con fecha no reconocida",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(
                            items = eventosSinFecha,
                            key = { evento -> "sinFecha_${evento.id}" }
                        ) { evento ->
                            RegistroFlushingCalendario(
                                evento = evento,
                                onVerDetalle = {
                                    navController.navigate(
                                        "${Routes.DETALLE_FLUSHING}/" +
                                                "${evento.lineaId}/${evento.id}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistroFlushingCalendario(
    evento: EventoFlushing,
    onVerDetalle: () -> Unit
) {
    val resumen = buildString {
        append("Antes: ${evento.temperaturaAntes} °C")
        append(" · Después: ${evento.temperaturaDespues} °C")
        if (!evento.fotoRuta.isNullOrBlank()) {
            append("\nCon foto de evidencia")
        }
    }

    AquaTarjetaAccion(
        titulo = evento.fechaHora,
        descripcion = resumen,
        icono = Icons.Outlined.Waves,
        onClick = onVerDetalle
    )
}
