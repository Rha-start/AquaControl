// Muestra mediciones persistidas con filtros y controles de paginación.
// Conecta con: viewmodel/historial/HistorialTemperaturaViewModel.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.iu.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.historial.HistorialTemperaturaViewModel
import kotlinx.coroutines.launch

@Composable
// Muestra mediciones persistidas con filtros y controles de paginación.
fun HistorialTemperaturaScreen(
    lineaId: Int,
    viewModel: HistorialTemperaturaViewModel,
    // AppNavHost recibe el ID y abre el detalle dentro de esta misma línea.
    onVerDetalle: (Long) -> Unit
) {
    // Observa el ViewModel; cada cambio de estado actualiza la interfaz.
    val estado by viewModel.uiState.collectAsState()
    val listaState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var mostrarFiltros by rememberSaveable(lineaId) {
        mutableStateOf(false)
    }

    // Solicita el historial de la línea; el ViewModel conserva los filtros vigentes.
    LaunchedEffect(lineaId, viewModel) {
        viewModel.cargarHistorial(lineaId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            state = listaState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "encabezado") {
                AquaEncabezado(
                    titulo = "Historial de temperaturas",
                    subtitulo = estado.ubicacion.takeIf { it.isNotBlank() }
                )
            }

            item(key = "filtros") {
                OutlinedButton(
                    onClick = {
                        viewModel.limpiarErrorFiltros()
                        mostrarFiltros = true
                    },
                    enabled = estado.lineaId != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = if (estado.filtro.tieneFiltros) {
                            "Filtros activos · Modificar"
                        } else {
                            "Filtrar por estado y fecha"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            when {
                estado.cargando -> {
                    item(key = "cargando") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                estado.error != null -> {
                    item(key = "error") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = estado.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Button(
                                onClick = viewModel::reintentar,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                estado.mediciones.isEmpty() -> {
                    item(key = "sin_resultados") {
                        Text(
                            text = if (estado.filtro.tieneFiltros) {
                                "No hay mediciones que coincidan con los filtros."
                            } else {
                                "Esta línea todavía no tiene mediciones."
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    item(key = "cantidad") {
                        Text(
                            text = "Mostrando ${estado.primerResultadoVisible}" +
                                    "–${estado.ultimoResultadoVisible} " +
                                    "de ${estado.totalResultados}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    items(
                        items = estado.mediciones,
                        key = { "medicion_${it.id}" }
                    ) { medicion ->
                        // Abre la medición exacta, conservando el estado del historial en su ViewModel.
                        Card(
                            onClick = { onVerDetalle(medicion.id) },
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${medicion.temperatura} °C",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                IndicadorEstado(
                                    estado = medicion.estado
                                )

                                Text(
                                    text = FechasHistorialTemperatura
                                        .formatearFechaHora(medicion.fechaHora),
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Text(
                                    text = when (medicion.origen) {
                                        OrigenMedicion.MANUAL ->
                                            "Origen: Manual"

                                        OrigenMedicion.SIMULADA ->
                                            "Origen: Simulada"
                                    },
                                    color = Muted,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Ver detalle",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }

                    if (estado.totalPaginas > 1) {
                        item(key = "paginacion") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Página ${estado.paginaActual + 1} " +
                                            "de ${estado.totalPaginas}",
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.paginaAnterior()
                                            scope.launch {
                                                listaState.scrollToItem(0)
                                            }
                                        },
                                        enabled = estado.puedeIrAnterior,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Text("Anterior")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.paginaSiguiente()
                                            scope.launch {
                                                listaState.scrollToItem(0)
                                            }
                                        },
                                        enabled = estado.puedeIrSiguiente,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Text("Siguiente")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFiltros) {
        FiltrosTemperaturaPanel(
            filtroInicial = estado.filtro,
            errorFiltros = estado.errorFiltros,
            onAplicar = { filtro ->
                viewModel.aplicarFiltros(filtro)

                if (viewModel.uiState.value.errorFiltros == null) {
                    mostrarFiltros = false

                    scope.launch {
                        listaState.scrollToItem(0)
                    }
                }
            },
            onLimpiar = {
                viewModel.limpiarFiltros()
                mostrarFiltros = false

                scope.launch {
                    listaState.scrollToItem(0)
                }
            },
            onEditar = viewModel::limpiarErrorFiltros,
            onCerrar = {
                mostrarFiltros = false
                viewModel.limpiarErrorFiltros()
            }
        )
    }
}