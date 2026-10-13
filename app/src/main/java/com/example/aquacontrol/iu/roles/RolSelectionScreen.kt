package com.example.aquacontrol.iu.roles

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.aquacontrol.R
import com.example.aquacontrol.iu.components.AquaTarjetaRol
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.ui.theme.Muted
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.ui.theme.Paper
import com.example.aquacontrol.ui.theme.Red
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import kotlinx.coroutines.launch

@Composable
fun RolSelectionScreen(
    navController: NavController,
    perfilViewModel: PerfilViewModel
) {
    var bienvenidaMostrada by rememberSaveable {
        mutableStateOf(false)
    }

    var fondoPreparado by remember {
        mutableStateOf(false)
    }
    var logoPreparado by remember {
        mutableStateOf(false)
    }

    val paisaje = remember {
        Animatable(if (bienvenidaMostrada) 1f else 0f)
    }
    val logo = remember {
        Animatable(if (bienvenidaMostrada) 1f else 0f)
    }
    val presentacion = remember {
        Animatable(if (bienvenidaMostrada) 1f else 0f)
    }
    val operario = remember {
        Animatable(if (bienvenidaMostrada) 1f else 0f)
    }
    val supervisor = remember {
        Animatable(if (bienvenidaMostrada) 1f else 0f)
    }

    val degrade = remember {
        Brush.verticalGradient(
            0f to Paper.copy(alpha = 0.08f),
            0.38f to Paper.copy(alpha = 0.16f),
            0.65f to Paper.copy(alpha = 0.38f),
            0.84f to Paper.copy(alpha = 0.78f),
            1f to Paper
        )
    }

    LaunchedEffect(fondoPreparado, logoPreparado) {
        if (
            fondoPreparado &&
            logoPreparado &&
            !bienvenidaMostrada
        ) {
            // Permite incorporar las imágenes antes de iniciar la secuencia.
            withFrameNanos { }
            bienvenidaMostrada = true

            launch {
                paisaje.animateTo(
                    1f,
                    tween(
                        durationMillis = 1400,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            launch {
                logo.animateTo(
                    1f,
                    tween(
                        durationMillis = 950,
                        delayMillis = 250,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            launch {
                presentacion.animateTo(
                    1f,
                    tween(
                        durationMillis = 800,
                        delayMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            launch {
                operario.animateTo(
                    1f,
                    tween(
                        durationMillis = 750,
                        delayMillis = 1100,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            launch {
                supervisor.animateTo(
                    1f,
                    tween(
                        durationMillis = 750,
                        delayMillis = 1450,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val alturaPaisaje = (maxHeight * 0.50f)
                .coerceIn(280.dp, 460.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(alturaPaisaje)
                ) {
                    AsyncImage(
                        model = R.drawable.fondo_bienvenida,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            fondoPreparado = true
                        },
                        onError = {
                            fondoPreparado = true
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = paisaje.value
                            }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(degrade)
                    )

                    AsyncImage(
                        model = R.drawable.logo_ariztia,
                        contentDescription = "Ariztía",
                        contentScale = ContentScale.Fit,
                        onSuccess = {
                            logoPreparado = true
                        },
                        onError = {
                            logoPreparado = true
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(220.dp)
                            .graphicsLayer {
                                alpha = logo.value
                                translationY =
                                    (1f - logo.value) * 12.dp.toPx()

                                val escala = 0.96f + logo.value * 0.04f
                                scaleX = escala
                                scaleY = escala
                            }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .graphicsLayer {
                            alpha = presentacion.value
                            translationY =
                                (1f - presentacion.value) * 16.dp.toPx()
                        },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AquaControl",
                        color = Navy,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "Monitoreo de temperatura y registro de flushing.",
                        color = Muted,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Selecciona tu rol",
                        color = Navy,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .graphicsLayer {
                            alpha = operario.value
                            translationY =
                                (1f - operario.value) * 24.dp.toPx()
                        }
                ) {
                    AquaTarjetaRol(
                        titulo = "Operario",
                        descripcion = "Control diario y flushing",
                        icono = Icons.Outlined.Engineering,
                        colorFondo = Navy,
                        onClick = {
                            if (
                                operario.value > 0.8f &&
                                navController.currentDestination?.route ==
                                Routes.ROLE_SELECTION
                            ) {
                                perfilViewModel.seleccionarPerfil(
                                    PerfilUsuario.OPERARIO
                                )
                                navController.navigate(
                                    Routes.OPERARIO_HOME
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .graphicsLayer {
                            alpha = supervisor.value
                            translationY =
                                (1f - supervisor.value) * 24.dp.toPx()
                        }
                ) {
                    AquaTarjetaRol(
                        titulo = "Supervisor",
                        descripcion = "Monitoreo, historial y alertas",
                        icono = Icons.Outlined.Assignment,
                        colorFondo = Red,
                        onClick = {
                            if (
                                supervisor.value > 0.8f &&
                                navController.currentDestination?.route ==
                                Routes.ROLE_SELECTION
                            ) {
                                perfilViewModel.seleccionarPerfil(
                                    PerfilUsuario.SUPERVISOR
                                )
                                navController.navigate(
                                    Routes.SUPERVISOR_HOME
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}