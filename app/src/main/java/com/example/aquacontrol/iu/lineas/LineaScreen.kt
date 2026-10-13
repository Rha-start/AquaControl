package com.example.aquacontrol.iu.lineas

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.ui.theme.ContenidoCriticoClaro
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.Red
import com.example.aquacontrol.viewmodel.lineas.LineaUiState
import com.example.aquacontrol.viewmodel.lineas.LineaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LineaScreen(
    navController: NavController,
    galponId: Int,
    viewModel: LineaViewModel
) {
    LaunchedEffect(galponId, viewModel) {
        viewModel.cargarLineas(galponId)
    }

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
                    titulo = "Líneas",
                    subtitulo = "Últimas temperaturas"
                )
            }

            when (val actual = estado) {
                LineaUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is LineaUiState.Success -> {
                    items(
                        items = actual.lineas,
                        key = { it.id }
                    ) { linea ->
                        val medicion = linea.ultimaMedicion
                        val esCritica =
                            medicion?.estado == EstadoLinea.CRITICO

                        Card(
                            onClick = {
                                navController.navigate(
                                    "${Routes.DETALLE_LINEA}/${linea.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = if (esCritica) 2.dp else 1.dp,
                                color = if (esCritica) {
                                    ContenidoCriticoClaro.copy(alpha = 0.55f)
                                } else {
                                    Navy.copy(alpha = 0.08f)
                                }
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surface,
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
                                verticalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(12.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = linea.nombre,
                                        style =
                                            MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        imageVector =
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                if (medicion == null) {
                                    IndicadorEstado(estado = null)
                                } else {
                                    Text(
                                        text = "${medicion.temperatura} °C",
                                        color = if (esCritica) {
                                            ContenidoCriticoClaro
                                        } else {
                                            Navy
                                        },
                                        style =
                                            MaterialTheme.typography.headlineLarge
                                    )

                                    IndicadorEstado(
                                        estado = medicion.estado
                                    )

                                    val fecha = remember(medicion.fechaHora) {
                                        formatearFechaLinea(medicion.fechaHora)
                                    }

                                    Text(
                                        text = "Última medición: $fecha",
                                        color = Muted,
                                        style =
                                            MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                LineaUiState.Empty -> {
                    item {
                        Text(
                            text = "Este galpón no tiene líneas registradas.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is LineaUiState.Error -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = actual.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarLineas(galponId)
                                },
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
}

private fun formatearFechaLinea(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formato.format(Date(fechaHora))
}