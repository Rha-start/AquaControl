package com.example.aquacontrol.iu.temperatura

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.temperatura.RegistrarTemperaturaViewModel

@Composable
fun RegistrarTemperaturaScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: RegistrarTemperaturaViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(lineaId) {
        viewModel.seleccionarLinea(lineaId)
    }

    val registrado = estado.medicionGuardada != null
    val editable = !estado.guardando && !registrado
    val lineaPreparada = lineaId > 0 && estado.lineaId == lineaId

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Registrar temperatura",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Ingresa la temperatura medida en la línea seleccionada.",
                style = MaterialTheme.typography.bodyLarge
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

            OutlinedTextField(
                value = estado.temperatura,
                onValueChange = viewModel::actualizarTemperatura,
                label = { Text("Temperatura (°C)") },
                enabled = editable && lineaPreparada,
                singleLine = true,
                isError = estado.errorTemperatura != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
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
                onClick = viewModel::registrarTemperatura,
                enabled = editable && lineaPreparada,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
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
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            estado.errorGuardado?.let { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor =
                            MaterialTheme.colorScheme.onErrorContainer
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

            estado.medicionGuardada?.let { medicion ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                        contentColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
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
                            text = "Temperatura de ${medicion.temperatura} °C " +
                                    "guardada correctamente. Puedes consultarla " +
                                    "en el historial de la línea.",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

        }
    }
}