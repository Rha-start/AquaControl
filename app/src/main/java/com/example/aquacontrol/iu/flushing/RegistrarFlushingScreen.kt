// Recoge los campos y la fotografía; delega validación y guardado al ViewModel.
// Conecta con: viewmodel/flushing/RegistrarFlushingViewModel.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.iu.flushing

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.FondoNormalClaro
import com.example.aquacontrol.ui.theme.ContenidoNormalClaro
import com.example.aquacontrol.data.flushing.FotosFlushing
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel

@Composable
// Recoge los campos y la fotografía; delega validación y guardado al ViewModel.
fun RegistrarFlushingScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: RegistrarFlushingViewModel
) {
    // Observa el ViewModel; cada cambio de estado actualiza la interfaz.
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current.applicationContext

    // Conserva la captura pendiente y los campos siguientes si Android recrea la pantalla.
    var fotoPendiente by rememberSaveable(lineaId) {
        mutableStateOf<String?>(null)
    }

    var fotoConservada by rememberSaveable(lineaId) {
        mutableStateOf(estado.fotoRuta)
    }

    var observacionConservada by rememberSaveable(lineaId) {
        mutableStateOf(estado.observacion)
    }

    var antesConservada by rememberSaveable(lineaId) {
        mutableStateOf(estado.temperaturaAntes)
    }

    var despuesConservada by rememberSaveable(lineaId) {
        mutableStateOf(estado.temperaturaDespues)
    }

    var duracionConservada by rememberSaveable(lineaId) {
        mutableStateOf(estado.duracionSegundos)
    }

    // Restaura los campos conservados en el ViewModel de la línea seleccionada.
    LaunchedEffect(lineaId, viewModel) {
        if (estado.lineaId != lineaId) {
            viewModel.seleccionarLinea(lineaId)
            viewModel.actualizarObservacion(observacionConservada)
            viewModel.actualizarTemperaturaAntes(antesConservada)
            viewModel.actualizarTemperaturaDespues(despuesConservada)
            viewModel.actualizarDuracion(duracionConservada)

            // Recupera la foto si Android recreó la pantalla al usar la cámara.
            if (estado.fotoRuta == null) {
                viewModel.actualizarFoto(fotoConservada)
            }
        }
    }

    // Recibe el resultado de la cámara y conserva solo una evidencia válida.
    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { capturada ->
        val nombre = fotoPendiente
        fotoPendiente = null

        if (nombre != null) {
            if (capturada && FotosFlushing.esFotoValida(context, nombre)) {
                val anterior = fotoConservada

                fotoConservada = nombre
                viewModel.actualizarFoto(nombre)

                if (anterior != null && anterior != nombre) {
                    FotosFlushing.eliminarFotoNoGuardada(
                        context,
                        anterior
                    )
                }
            } else {
                FotosFlushing.eliminarFotoNoGuardada(context, nombre)

                viewModel.informarErrorFoto(
                    if (capturada) {
                        "La cámara no generó una foto válida. Inténtalo nuevamente."
                    } else {
                        null
                    }
                )
            }
        } else if (capturada) {
            viewModel.informarErrorFoto(
                "No se pudo recuperar la captura. Toma la foto nuevamente."
            )
        }
    }

    // La confirmación pertenece a esta línea y se conserva al recrear la pantalla.
    var confirmarGuardado by rememberSaveable(lineaId) { mutableStateOf(false) }

    val registrado = estado.eventoGuardado != null
    val capturando = fotoPendiente != null
    // Bloquea la edición durante captura o guardado y después de completar el registro.
    val editable = !estado.guardando && !registrado && !capturando

    // Cancelar o cerrar el diálogo conserva los campos y la foto para seguir editando.
    if (confirmarGuardado && editable && estado.lineaId == lineaId) {
        AlertDialog(
            onDismissRequest = { confirmarGuardado = false },
            title = { Text("Confirmar guardado") },
            text = {
                Text("¿Confirmas que deseas guardar los datos de flushing?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (confirmarGuardado && editable && estado.lineaId == lineaId) {
                            // Cierra primero y vuelve a comprobar la evidencia antes de guardar.
                            confirmarGuardado = false
                            val foto = estado.fotoRuta
                            if (
                                foto != null &&
                                !FotosFlushing.esFotoValida(context, foto)
                            ) {
                                viewModel.informarErrorFoto(
                                    "La foto ya no está disponible. " +
                                            "Toma una nueva antes de guardar."
                                )
                            } else {
                                // El ViewModel valida los campos y delega la persistencia al repositorio.
                                viewModel.registrarFlushing()
                            }
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
                titulo = "Registrar flushing"
            )

            estado.errorLinea?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoFlushing(
                        valor = estado.observacion,
                        alCambiar = {
                            observacionConservada = it
                            viewModel.actualizarObservacion(it)
                        },
                        etiqueta = "Observación",
                        error = estado.errorObservacion,
                        habilitado = editable,
                        unaLinea = false
                    )

                    CampoFlushing(
                        valor = estado.temperaturaAntes,
                        alCambiar = {
                            antesConservada = it
                            viewModel.actualizarTemperaturaAntes(it)
                        },
                        etiqueta = "Temperatura antes (°C)",
                        error = estado.errorTemperaturaAntes,
                        habilitado = editable,
                        teclado = KeyboardType.Decimal
                    )

                    CampoFlushing(
                        valor = estado.temperaturaDespues,
                        alCambiar = {
                            despuesConservada = it
                            viewModel.actualizarTemperaturaDespues(it)
                        },
                        etiqueta = "Temperatura después (°C)",
                        error = estado.errorTemperaturaDespues,
                        habilitado = editable,
                        teclado = KeyboardType.Decimal
                    )

                    CampoFlushing(
                        valor = estado.duracionSegundos,
                        alCambiar = {
                            duracionConservada = it
                            viewModel.actualizarDuracion(it)
                        },
                        etiqueta = "Duración en segundos",
                        error = estado.errorDuracion,
                        habilitado = editable,
                        teclado = KeyboardType.Number
                    )
                }
            }

            Text(
                text = "Foto de evidencia (opcional)",
                style = MaterialTheme.typography.titleMedium
            )

            estado.fotoRuta?.let { nombre ->
                FotoFlushing(fotoRuta = nombre)
            }

            OutlinedButton(
                onClick = {
                    var nombreCreado: String? = null

                    try {
                        viewModel.informarErrorFoto(null)

                        val archivo = FotosFlushing.crearArchivo(context)
                        nombreCreado = archivo.name

                        val uri = FotosFlushing.obtenerUri(
                            context,
                            archivo.name
                        )

                        fotoPendiente = archivo.name
                        camara.launch(uri)
                    } catch (e: Exception) {
                        fotoPendiente = null

                        FotosFlushing.eliminarFotoNoGuardada(
                            context,
                            nombreCreado
                        )

                        viewModel.informarErrorFoto(
                            "No se pudo abrir la cámara. " +
                                    "Comprueba que esté disponible e inténtalo nuevamente."
                        )
                    }
                },
                enabled = editable,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = when {
                        capturando -> "Capturando foto…"
                        estado.fotoRuta != null -> "Repetir foto"
                        else -> "Tomar foto"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            estado.errorFoto?.let { mensaje ->
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

            Button(
                // Abre la confirmación sin guardar todavía el registro.
                onClick = { confirmarGuardado = true },
                enabled = editable && estado.lineaId == lineaId,
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
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(8.dp))
                    Text("Guardando…")
                } else {
                    Text(
                        text = if (registrado) {
                            "Registro guardado"
                        } else {
                            "Guardar flushing"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            estado.errorGuardado?.let { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null
                        )

                        Text(
                            text = mensaje,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (registrado) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = FondoNormalClaro,
                        contentColor = ContenidoNormalClaro
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Text(
                            text = "Flushing guardado correctamente.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

        }
    }
}

@Composable
// Reutiliza un campo del formulario con etiqueta, teclado y error.
private fun CampoFlushing(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    habilitado: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        isError = error != null,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado
        ),
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Campo con error"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}