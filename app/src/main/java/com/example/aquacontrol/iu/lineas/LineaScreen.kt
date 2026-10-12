package com.example.aquacontrol.iu.lineas

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.temperatura.OrigenMedicion
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Líneas de bebederos",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    text = "Última medición guardada de cada línea. " +
                            "Selecciona una línea para abrir su detalle.",
                    style = MaterialTheme.typography.bodyLarge
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClickLabel = "Ver detalle de ${linea.nombre}"
                                ) {
                                    navController.navigate(
                                        "${Routes.DETALLE_LINEA}/${linea.id}"
                                    ) {
                                        launchSingleTop = true
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = linea.nombre,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                val medicion = linea.ultimaMedicion

                                if (medicion == null) {
                                    IndicadorEstado(estado = null)

                                    Text(
                                        text = "Todavía no se ha guardado una " +
                                                "temperatura para esta línea.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else {
                                    val nombreOrigen = when (medicion.origen) {
                                        OrigenMedicion.MANUAL -> "Manual"
                                        OrigenMedicion.SIMULADA -> "Simulada"
                                    }

                                    Text(
                                        text = "${medicion.temperatura} °C",
                                        style = MaterialTheme.typography.headlineSmall
                                    )

                                    IndicadorEstado(
                                        estado = medicion.estado
                                    )

                                    Text(
                                        text = "Origen: $nombreOrigen",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = "Última medición: ${
                                            formatearFechaLinea(medicion.fechaHora)
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                LineaUiState.Empty -> {
                    item {
                        Text(
                            text = "Este galpón no tiene líneas registradas."
                        )
                    }
                }

                is LineaUiState.Error -> {
                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.cargarLineas(galponId)
                            },
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

private fun formatearFechaLinea(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formato.format(Date(fechaHora))
}