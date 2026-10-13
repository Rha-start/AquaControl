package com.example.aquacontrol.iu.galpones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Warehouse
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.Blue
import com.example.aquacontrol.ui.theme.ContenidoAdvertenciaClaro
import com.example.aquacontrol.ui.theme.ContenidoCriticoClaro
import com.example.aquacontrol.ui.theme.ContenidoNormalClaro
import com.example.aquacontrol.ui.theme.ContenidoSinMedicionesClaro
import com.example.aquacontrol.ui.theme.FondoAdvertenciaClaro
import com.example.aquacontrol.ui.theme.FondoCriticoClaro
import com.example.aquacontrol.ui.theme.FondoNormalClaro
import com.example.aquacontrol.ui.theme.FondoSinMedicionesClaro
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.Red
import com.example.aquacontrol.ui.theme.Sky
import com.example.aquacontrol.viewmodel.galpones.EstadoResumenGalpon
import com.example.aquacontrol.viewmodel.galpones.GalponUiState
import com.example.aquacontrol.viewmodel.galpones.GalponViewModel

@Composable
fun GalponScreen(
    navController: NavController,
    granjaId: Int,
    viewModel: GalponViewModel
) {
    LaunchedEffect(granjaId, viewModel) {
        viewModel.cargarGalpones(granjaId)
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
                AquaEncabezado(titulo = "Galpones")
            }

            when (val actual = estado) {
                GalponUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is GalponUiState.Success -> {
                    items(
                        items = actual.galpones,
                        key = { galpon -> galpon.id }
                    ) { galpon ->
                        TarjetaGalpon(
                            nombre = galpon.nombre,
                            estado = actual.resumenesPorGalpon[galpon.id]?.estado,
                            onClick = {
                                navController.navigate(
                                    "${Routes.LINEAS}/${galpon.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                GalponUiState.Empty -> {
                    item {
                        Text(
                            text = "Esta granja no tiene galpones registrados.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is GalponUiState.Error -> {
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
                                    viewModel.cargarGalpones(granjaId)
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

@Composable
private fun TarjetaGalpon(
    nombre: String,
    estado: EstadoResumenGalpon?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp),
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
            defaultElevation = 5.dp,
            pressedElevation = 1.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Sky,
                contentColor = Blue,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warehouse,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium
                )

                EtiquetaEstadoGalpon(estado)
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Red,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EtiquetaEstadoGalpon(
    estado: EstadoResumenGalpon?
) {
    val texto = when (estado) {
        EstadoResumenGalpon.ESTABLE -> "Estable"
        EstadoResumenGalpon.ADVERTENCIA -> "Advertencia"
        EstadoResumenGalpon.CRITICO -> "Crítico"
        EstadoResumenGalpon.DATOS_INCOMPLETOS -> "Datos incompletos"
        EstadoResumenGalpon.SIN_MEDICIONES -> "Sin mediciones"
        EstadoResumenGalpon.SIN_LINEAS -> "Sin líneas"
        null -> "Estado no disponible"
    }

    val fondo = when (estado) {
        EstadoResumenGalpon.ESTABLE -> FondoNormalClaro
        EstadoResumenGalpon.ADVERTENCIA -> FondoAdvertenciaClaro
        EstadoResumenGalpon.CRITICO -> FondoCriticoClaro
        else -> FondoSinMedicionesClaro
    }

    val contenido = when (estado) {
        EstadoResumenGalpon.ESTABLE -> ContenidoNormalClaro
        EstadoResumenGalpon.ADVERTENCIA -> ContenidoAdvertenciaClaro
        EstadoResumenGalpon.CRITICO -> ContenidoCriticoClaro
        else -> ContenidoSinMedicionesClaro
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = fondo,
        contentColor = contenido,
        border = BorderStroke(
            width = 1.dp,
            color = contenido.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
        )
    }
}