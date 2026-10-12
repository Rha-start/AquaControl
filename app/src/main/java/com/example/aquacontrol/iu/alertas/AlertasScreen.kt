package com.example.aquacontrol.iu.alertas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.viewmodel.alertas.AlertaLinea
import com.example.aquacontrol.viewmodel.alertas.AlertasUiState
import com.example.aquacontrol.viewmodel.alertas.AlertasViewModel

@Composable
fun AlertasScreen(
    navController: NavController,
    viewModel: AlertasViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Alertas",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            when (val actual = estado) {
                AlertasUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is AlertasUiState.Success -> {
                    val criticas = actual.alertas.count {
                        it.medicion.estado == EstadoLinea.CRITICO
                    }

                    val advertencias = actual.alertas.count {
                        it.medicion.estado == EstadoLinea.ADVERTENCIA
                    }

                    val lineasConMediciones =
                        actual.totalLineas - actual.lineasSinMediciones

                    if (actual.alertas.isNotEmpty()) {
                        item {
                            Text(
                                text = "$criticas críticas · $advertencias advertencias",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        item {
                            val mensaje = when {
                                actual.totalLineas == 0 ->
                                    "No hay líneas registradas."

                                lineasConMediciones == 0 ->
                                    "Sin mediciones disponibles."

                                else ->
                                    "Sin alertas en las líneas con mediciones."
                            }

                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (
                        actual.lineasSinMediciones > 0 &&
                        lineasConMediciones > 0
                    ) {
                        item {
                            Text(
                                text = "Líneas sin mediciones: ${actual.lineasSinMediciones}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    items(
                        items = actual.alertas,
                        key = { it.medicion.lineaId }
                    ) { alerta ->
                        TarjetaAlerta(
                            alerta = alerta,
                            onVerDetalle = {
                                navController.navigate(
                                    "${Routes.DETALLE_LINEA}/${alerta.medicion.lineaId}"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                is AlertasUiState.Error -> {
                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    item {
                        Button(
                            onClick = viewModel::cargarAlertas,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun TarjetaAlerta(
    alerta: AlertaLinea,
    onVerDetalle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IndicadorEstado(
                estado = alerta.medicion.estado,
                destacado = true
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${alerta.nombreGranja} · ${alerta.nombreGalpon}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = alerta.nombreLinea,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "${alerta.medicion.temperatura} °C",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedButton(
                onClick = onVerDetalle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Ver detalle",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}