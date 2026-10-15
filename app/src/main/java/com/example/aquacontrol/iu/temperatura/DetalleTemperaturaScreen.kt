package com.example.aquacontrol.iu.temperatura

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.AquaPanelResumen
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.temperatura.DetalleTemperaturaViewModel

// Conecta con viewmodel/temperatura/DetalleTemperaturaViewModel.kt.
// AppNavHost entrega el perfil actual; esta pantalla no cambia el rol seleccionado.
@Composable
fun DetalleTemperaturaScreen(
    lineaId: Int,
    medicionId: Long,
    perfil: PerfilUsuario?,
    viewModel: DetalleTemperaturaViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    var confirmarGuardado by rememberSaveable(lineaId, medicionId) { mutableStateOf(false) }
    var confirmarEliminacion by rememberSaveable(lineaId, medicionId) { mutableStateOf(false) }
    val esOperario = perfil == PerfilUsuario.OPERARIO
    val esSupervisor = perfil == PerfilUsuario.SUPERVISOR

    // Consulta el registro específico; las recomposiciones no reinician el borrador.
    LaunchedEffect(lineaId, medicionId, viewModel) {
        viewModel.cargarMedicion(lineaId, medicionId)
    }

    // Cierra acciones que ya no corresponden si cambia el rol durante la navegación.
    LaunchedEffect(perfil) {
        if (!esOperario) {
            confirmarGuardado = false
            viewModel.cancelarEdicion()
        }
        if (!esSupervisor) confirmarEliminacion = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AquaEncabezado(titulo = "Detalle de temperatura")

            when {
                estado.cargando -> CircularProgressIndicator()
                estado.eliminado -> {
                    // No muestra controles ni datos de un registro que ya fue eliminado.
                    Text(
                        text = estado.mensaje ?: "Medición eliminada correctamente.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                estado.errorCarga != null -> {
                    Text(
                        text = estado.errorCarga.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(
                        onClick = { viewModel.cargarMedicion(lineaId, medicionId, forzar = true) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) { Text("Reintentar") }
                }
                else -> {
                    estado.medicion?.let { medicion ->
                        AquaPanelResumen(etiqueta = "Medición registrada") {
                            Text(
                                text = "${medicion.temperatura} °C",
                                style = MaterialTheme.typography.displaySmall
                            )
                            IndicadorEstado(estado = medicion.estado)
                            Text(FechasHistorialTemperatura.formatearFechaHora(medicion.fechaHora))
                            Text(
                                text = "Origen: " + when (medicion.origen) {
                                    OrigenMedicion.MANUAL -> "Manual"
                                    OrigenMedicion.SIMULADA -> "Simulada"
                                }
                            )
                        }

                        // El borrador solo está disponible para Operario; no altera Room al escribir.
                        if (esOperario && estado.editando) {
                            OutlinedTextField(
                                value = estado.temperatura,
                                onValueChange = { viewModel.actualizarTemperatura(it, perfil) },
                                label = { Text("Temperatura (°C)") },
                                supportingText = {
                                    Text(estado.errorTemperatura ?: "Puedes usar coma o punto decimal.")
                                },
                                isError = estado.errorTemperatura != null,
                                enabled = estado.puedeInteractuar,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
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
                        } else if (esOperario) {
                            Button(
                                onClick = { viewModel.iniciarEdicion(perfil) },
                                enabled = estado.puedeInteractuar,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                            ) { Text("Editar") }
                        }

                        // El botón destructivo solo se ofrece al Supervisor y requiere confirmación.
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
                        estado.errorOperacion?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                        estado.mensaje?.let {
                            Text(it, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Cancelar este diálogo conserva el borrador; Guardar delega validación y persistencia.
    if (confirmarGuardado && esOperario && estado.editando && estado.puedeInteractuar) {
        AlertDialog(
            onDismissRequest = { confirmarGuardado = false },
            title = { Text("Confirmar cambios") },
            text = { Text("¿Confirmas que deseas guardar los cambios de temperatura?") },
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

    // Solo confirma la eliminación del registro visible, identificado por su ID y línea.
    if (confirmarEliminacion && esSupervisor && estado.puedeInteractuar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminacion = false },
            title = { Text("Eliminar medición") },
            text = {
                Text("¿Confirmas que deseas eliminar esta medición de temperatura? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (confirmarEliminacion) {
                            confirmarEliminacion = false
                            viewModel.eliminarMedicion(perfil)
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
