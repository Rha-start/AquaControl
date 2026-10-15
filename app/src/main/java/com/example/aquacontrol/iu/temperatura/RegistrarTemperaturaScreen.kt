// Recoge la lectura manual y muestra validación, guardado y resultado del ViewModel.
// Conecta con: viewmodel/temperatura/RegistrarTemperaturaViewModel.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.iu.temperatura

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.ui.theme.ContenidoNormalClaro
import com.example.aquacontrol.ui.theme.FondoNormalClaro
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.viewmodel.temperatura.RegistrarTemperaturaViewModel

@Composable
// Recoge la lectura manual y muestra validación, guardado y resultado del ViewModel.
fun RegistrarTemperaturaScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: RegistrarTemperaturaViewModel
) {
    // Observa el ViewModel; cada cambio de estado actualiza la interfaz.
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(lineaId, viewModel) {
        viewModel.seleccionarLinea(lineaId)
    }

    // Conserva la confirmación al recrear la pantalla y la reinicia al cambiar de línea.
    var confirmarGuardado by rememberSaveable(lineaId) { mutableStateOf(false) }

    val registrado = estado.medicionGuardada != null
    val editable = !estado.guardando && !registrado
    val lineaPreparada = lineaId > 0 && estado.lineaId == lineaId

    // Solo el botón Guardar del diálogo llama al ViewModel, que mantiene sus validaciones.
    // Cancelar, tocar fuera o usar Atrás cierra el diálogo sin borrar lo escrito.
    if (confirmarGuardado && editable && lineaPreparada) {
        AlertDialog(
            onDismissRequest = { confirmarGuardado = false },
            title = { Text("Confirmar guardado") },
            text = {
                Text("¿Confirmas que deseas guardar los datos de temperatura?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Cierra primero para que una segunda pulsación no vuelva a enviar el registro.
                        if (confirmarGuardado && editable && lineaPreparada) {
                            confirmarGuardado = false
                            viewModel.registrarTemperatura()
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmarGuardado = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AquaEncabezado(
                titulo = "Registrar temperatura",
                etiqueta = "REGISTRO MANUAL"
            )

            estado.errorLinea?.let { mensaje ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

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
                    OutlinedTextField(
                        value = estado.temperatura,
                        onValueChange = viewModel::actualizarTemperatura,
                        label = { Text("Temperatura (°C)") },
                        enabled = editable && lineaPreparada,
                        singleLine = true,
                        isError = estado.errorTemperatura != null,
                        textStyle = MaterialTheme.typography.titleLarge,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Thermostat,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            Text(
                                text = estado.errorTemperatura
                                    ?: "Puedes usar coma o punto decimal."
                            )
                        },
                        trailingIcon = {
                            if (estado.errorTemperatura != null) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Campo con error"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        // Solicita confirmación; todavía no valida ni escribe en Room.
                        onClick = { confirmarGuardado = true },
                        enabled = editable && lineaPreparada,
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
                        if (estado.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )

                            Spacer(Modifier.width(8.dp))
                            Text("Guardando…")
                        } else {
                            Text(
                                text = if (registrado) {
                                    "Temperatura guardada"
                                } else {
                                    "Guardar temperatura"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            estado.errorGuardado?.let { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor =
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null
                        )

                        Text(
                            text = mensaje,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            estado.medicionGuardada?.let { medicion ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FondoNormalClaro,
                        contentColor = ContenidoNormalClaro
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Text(
                            text = "Registro guardado: ${medicion.temperatura} °C.",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}