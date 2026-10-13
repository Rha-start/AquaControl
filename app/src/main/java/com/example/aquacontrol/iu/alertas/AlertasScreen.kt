package com.example.aquacontrol.iu.alertas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.ui.theme.ContenidoAdvertenciaClaro
import com.example.aquacontrol.ui.theme.ContenidoCriticoClaro
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.ui.theme.Navy
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "encabezado") {
                AquaEncabezado(
                    titulo = "Alertas",
                    subtitulo = "Estado actual de las líneas"
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
                                text = "$criticas críticas · " +
                                        "$advertencias advertencias",
                                color = Navy,
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

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp
                            ) {
                                Text(
                                    text = mensaje,
                                    color = Navy,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    }

                    if (
                        actual.lineasSinMediciones > 0 &&
                        lineasConMediciones > 0
                    ) {
                        item {
                            Text(
                                text = "Líneas sin mediciones: " +
                                        actual.lineasSinMediciones,
                                color = Muted,
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
                                    "${Routes.DETALLE_LINEA}/" +
                                            alerta.medicion.lineaId
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
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = viewModel::cargarAlertas,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
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
    val esCritica = alerta.medicion.estado == EstadoLinea.CRITICO

    val colorEstado = if (esCritica) {
        ContenidoCriticoClaro
    } else {
        ContenidoAdvertenciaClaro
    }

    Card(
        onClick = onVerDetalle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (esCritica) 2.dp else 1.dp,
            color = colorEstado.copy(alpha = 0.55f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Navy
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp,
            pressedElevation = 1.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IndicadorEstado(
                estado = alerta.medicion.estado,
                destacado = true
            )

            Text(
                text = "${alerta.nombreGranja} · ${alerta.nombreGalpon}",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alerta.nombreLinea,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colorEstado,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "${alerta.medicion.temperatura} °C",
                color = colorEstado,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}