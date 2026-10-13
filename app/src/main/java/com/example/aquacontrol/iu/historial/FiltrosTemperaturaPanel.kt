package com.example.aquacontrol.iu.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.Paper
import com.example.aquacontrol.ui.theme.Sky
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosTemperaturaPanel(
    filtroInicial: FiltroHistorialTemperatura,
    errorFiltros: String?,
    onAplicar: (FiltroHistorialTemperatura) -> Unit,
    onLimpiar: () -> Unit,
    onEditar: () -> Unit,
    onCerrar: () -> Unit
) {
    var estadoSeleccionado by rememberSaveable {
        mutableStateOf(filtroInicial.estado?.name)
    }

    var fechaDesde by rememberSaveable {
        mutableStateOf(
            filtroInicial.desdeInclusive?.let {
                FechasHistorialTemperatura.selectorDesdeInicioLocal(it)
            }
        )
    }

    var fechaHasta by rememberSaveable {
        mutableStateOf(
            filtroInicial.hastaExclusivo?.let {
                FechasHistorialTemperatura.selectorDesdeFinLocalExclusivo(it)
            }
        )
    }

    var calendarioAbierto by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val panelState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = panelState,
        containerColor = Paper,
        contentColor = Navy,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtrar temperaturas",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Estado",
                style = MaterialTheme.typography.titleMedium
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val opciones = listOf(
                    null to "Todos",
                    EstadoLinea.NORMAL.name to "Normal",
                    EstadoLinea.ADVERTENCIA.name to "Advertencia",
                    EstadoLinea.CRITICO.name to "Crítico"
                )

                opciones.forEach { (valor, etiqueta) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (estadoSeleccionado == valor) {
                                    Sky
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .selectable(
                                selected = estadoSeleccionado == valor,
                                role = Role.RadioButton,
                                onClick = {
                                    estadoSeleccionado = valor
                                    onEditar()
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = estadoSeleccionado == valor,
                            onClick = null
                        )

                        Text(
                            text = etiqueta,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            CampoFechaFiltro(
                etiqueta = "Desde",
                fecha = fechaDesde,
                onSeleccionar = {
                    calendarioAbierto = "desde"
                },
                onQuitar = {
                    fechaDesde = null
                    onEditar()
                }
            )

            CampoFechaFiltro(
                etiqueta = "Hasta",
                fecha = fechaHasta,
                onSeleccionar = {
                    calendarioAbierto = "hasta"
                },
                onQuitar = {
                    fechaHasta = null
                    onEditar()
                }
            )

            errorFiltros?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    onAplicar(
                        FiltroHistorialTemperatura(
                            estado = estadoSeleccionado?.let {
                                EstadoLinea.valueOf(it)
                            },
                            desdeInclusive = fechaDesde?.let {
                                FechasHistorialTemperatura
                                    .inicioLocalDesdeSelector(it)
                            },
                            hastaExclusivo = fechaHasta?.let {
                                FechasHistorialTemperatura
                                    .finLocalExclusivoDesdeSelector(it)
                            }
                        )
                    )
                },
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Aplicar filtros",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            OutlinedButton(
                onClick = {
                    estadoSeleccionado = null
                    fechaDesde = null
                    fechaHasta = null
                    onLimpiar()
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Limpiar filtros",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    calendarioAbierto?.let { campo ->
        key(campo) {
            val calendarioState = rememberDatePickerState(
                initialSelectedDateMillis = if (campo == "desde") {
                    fechaDesde
                } else {
                    fechaHasta
                }
            )

            DatePickerDialog(
                onDismissRequest = {
                    calendarioAbierto = null
                },
                confirmButton = {
                    TextButton(
                        enabled = calendarioState.selectedDateMillis != null,
                        onClick = {
                            val seleccion =
                                calendarioState.selectedDateMillis

                            if (seleccion != null) {
                                if (campo == "desde") {
                                    fechaDesde = seleccion
                                } else {
                                    fechaHasta = seleccion
                                }

                                onEditar()
                                calendarioAbierto = null
                            }
                        }
                    ) {
                        Text("Seleccionar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            calendarioAbierto = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = calendarioState)
            }
        }
    }
}

@Composable
private fun CampoFechaFiltro(
    etiqueta: String,
    fecha: Long?,
    onSeleccionar: () -> Unit,
    onQuitar: () -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onSeleccionar,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        ) {
            Text(
                text = "$etiqueta: ${
                    fecha?.let {
                        FechasHistorialTemperatura.formatearFechaSelector(it)
                    } ?: "Sin límite"
                }",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (fecha != null) {
            TextButton(
                onClick = onQuitar
            ) {
                Text("Quitar fecha ${etiqueta.lowercase()}")
            }
        }
    }
}