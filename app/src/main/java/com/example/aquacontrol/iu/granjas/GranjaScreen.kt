package com.example.aquacontrol.iu.granjas

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.aquacontrol.R
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.viewmodel.granjas.GranjaUiState
import com.example.aquacontrol.viewmodel.granjas.GranjaViewModel

@Composable
fun GranjaScreen(
    navController: NavController,
    viewModel: GranjaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item(key = "encabezado") {
                AquaEncabezado(
                    titulo = "Granjas"
                )
            }

            when (val actual = estado) {
                GranjaUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is GranjaUiState.Success -> {
                    items(
                        items = actual.granjas,
                        key = { granja -> granja.id }
                    ) { granja ->
                        TarjetaGranja(
                            nombre = granja.nombre,
                            imagen = imagenParaGranja(granja.id),
                            onClick = {
                                navController.navigate(
                                    "${Routes.GALPONES}/${granja.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                GranjaUiState.Empty -> {
                    item {
                        Text(
                            text = "No hay granjas disponibles.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is GranjaUiState.Error -> {
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
                                    viewModel.cargarGranjas()
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

// Fotografías ilustrativas asociadas al identificador,
// para mantenerlas aunque cambie el orden de las granjas.
@DrawableRes
private fun imagenParaGranja(granjaId: Int): Int {
    return when (granjaId) {
        2 -> R.drawable.fondo_granja_bebederos
        else -> R.drawable.fondo_bienvenida
    }
}

@Composable
private fun TarjetaGranja(
    nombre: String,
    @DrawableRes imagen: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.35f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Navy,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 1.dp,
            focusedElevation = 7.dp,
            hoveredElevation = 7.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            AsyncImage(
                model = imagen,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Navy.copy(alpha = 0.08f),
                            0.35f to Navy.copy(alpha = 0.35f),
                            0.65f to Navy.copy(alpha = 0.80f),
                            1f to Navy.copy(alpha = 0.97f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = nombre,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver galpones",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}