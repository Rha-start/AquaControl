package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.AquaPanelResumen
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingViewModel

// Consulta el evento y conecta las acciones con viewmodel/flushing/DetalleFlushingViewModel.kt.
@Composable
fun FlushingDetalleScreen(
    lineaId: Int,
    eventoId: Int,
    perfil: PerfilUsuario?,
    viewModel: DetalleFlushingViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    val esOperario = perfil == PerfilUsuario.OPERARIO
    val esSupervisor = perfil == PerfilUsuario.SUPERVISOR
    var confirmarGuardado by rememberSaveable(lineaId, eventoId) { mutableStateOf(false) }
    var confirmarEliminacion by rememberSaveable(lineaId, eventoId) { mutableStateOf(false) }

    // El ViewModel conserva el borrador mientras esta entrada de navegación siga abierta.
    LaunchedEffect(lineaId, eventoId, viewModel) {
        viewModel.cargarEvento(lineaId, eventoId)
    }
    LaunchedEffect(perfil) {
        if (!esOperario) {
            confirmarGuardado = false
            viewModel.cancelarEdicion()
        }
        if (!esSupervisor) confirmarEliminacion = false
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AquaEncabezado(titulo = "Detalle de flushing")
            when {
                estado.cargando -> CircularProgressIndicator()
                estado.eliminado -> {
                    // Incluye el aviso de limpieza si el archivo de evidencia no pudo borrarse.
                    Text(
                        estado.mensaje ?: "Flushing eliminado correctamente.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                estado.errorCarga != null -> {
                    Text(estado.errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(
                        onClick = { viewModel.cargarEvento(lineaId, eventoId, forzar = true) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text("Reintentar") }
                }
                else -> estado.evento?.let { evento ->
                    Text(evento.fechaHora, color = Navy, style = MaterialTheme.typography.titleMedium)
                    AquaPanelResumen(etiqueta = "Temperaturas del procedimiento") {
                        Text("Antes", style = MaterialTheme.typography.labelLarge)
                        Text("${evento.temperaturaAntes} °C", style = MaterialTheme.typography.headlineLarge)
                        Text("Después", style = MaterialTheme.typography.labelLarge)
                        Text("${evento.temperaturaDespues} °C", style = MaterialTheme.typography.headlineLarge)
                        Text("Duración: ${evento.duracionSegundos} segundos")
                    }

                    // El formulario modifica un borrador; la fecha y la evidencia se conservan.
                    if (esOperario && estado.editando) {
                        SeccionDetalleFlushing(titulo = "Editar registro") {
                            CampoEdicionFlushing(
                                valor = estado.observacion,
                                etiqueta = "Observación",
                                error = estado.errorObservacion,
                                habilitado = estado.puedeInteractuar,
                                unaLinea = false,
                                alCambiar = { viewModel.actualizarObservacion(it, perfil) }
                            )
                            CampoEdicionFlushing(
                                valor = estado.temperaturaAntes,
                                etiqueta = "Temperatura antes (°C)",
                                error = estado.errorTemperaturaAntes,
                                habilitado = estado.puedeInteractuar,
                                teclado = KeyboardType.Decimal,
                                alCambiar = { viewModel.actualizarTemperaturaAntes(it, perfil) }
                            )
                            CampoEdicionFlushing(
                                valor = estado.temperaturaDespues,
                                etiqueta = "Temperatura después (°C)",
                                error = estado.errorTemperaturaDespues,
                                habilitado = estado.puedeInteractuar,
                                teclado = KeyboardType.Decimal,
                                alCambiar = { viewModel.actualizarTemperaturaDespues(it, perfil) }
                            )
                            CampoEdicionFlushing(
                                valor = estado.duracionSegundos,
                                etiqueta = "Duración en segundos",
                                error = estado.errorDuracion,
                                habilitado = estado.puedeInteractuar,
                                teclado = KeyboardType.Number,
                                alCambiar = { viewModel.actualizarDuracion(it, perfil) }
                            )
                        }
                        Button(
                            onClick = { confirmarGuardado = true },
                            enabled = estado.puedeInteractuar,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) { Text(if (estado.guardando) "Guardando…" else "Guardar cambios") }
                        OutlinedButton(
                            onClick = { viewModel.cancelarEdicion() },
                            enabled = estado.puedeInteractuar,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) { Text("Cancelar edición") }
                    } else {
                        SeccionDetalleFlushing(titulo = "Observación") {
                            Text(evento.observacion.ifBlank { "Sin observación" })
                        }
                        if (esOperario) {
                            Button(
                                onClick = { viewModel.iniciarEdicion(perfil) },
                                enabled = estado.puedeInteractuar,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            ) { Text("Editar") }
                        }
                    }

                    SeccionDetalleFlushing(titulo = "Foto de evidencia") {
                        val foto = evento.fotoRuta
                        if (foto.isNullOrBlank()) {
                            Text("Este registro no tiene foto de evidencia.")
                        } else {
                            FotoFlushing(fotoRuta = foto)
                        }
                    }

                    // Solo Supervisor ve Eliminar; el ViewModel y el repositorio repiten la comprobación.
                    if (esSupervisor) {
                        Button(
                            onClick = { confirmarEliminacion = true },
                            enabled = estado.puedeInteractuar,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) { Text(if (estado.eliminando) "Eliminando…" else "Eliminar") }
                    }
                    estado.errorOperacion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    estado.mensaje?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
    }

    // Cancelar la confirmación mantiene el borrador; solo Guardar ejecuta la operación.
    if (confirmarGuardado && esOperario && estado.editando && estado.puedeInteractuar) {
        AlertDialog(
            onDismissRequest = { confirmarGuardado = false },
            title = { Text("Confirmar cambios") },
            text = { Text("¿Confirmas que deseas guardar los cambios de flushing?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (confirmarGuardado) {
                            confirmarGuardado = false
                            viewModel.guardarCambios(perfil)
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmarGuardado = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Cancelar") }
            }
        )
    }

    // Advierte que se eliminarán tanto el registro como su fotografía asociada.
    if (confirmarEliminacion && esSupervisor && estado.puedeInteractuar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminacion = false },
            title = { Text("Eliminar flushing") },
            text = {
                Text("¿Confirmas que deseas eliminar este registro y su fotografía, si tiene una? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (confirmarEliminacion) {
                            confirmarEliminacion = false
                            viewModel.eliminarEvento(perfil)
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmarEliminacion = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Cancelar") }
            }
        )
    }
}

// Campo reutilizable con el teclado y error correspondientes a cada dato editable.
@Composable
private fun CampoEdicionFlushing(
    valor: String,
    etiqueta: String,
    error: String?,
    habilitado: Boolean,
    alCambiar: (String) -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

// Conserva el estilo de las secciones de observación, formulario y evidencia.
@Composable
private fun SeccionDetalleFlushing(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Navy.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Navy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}
