// Ofrece Inicio, Granjas y Alertas; muestra los contadores de advertencias y críticos.
package com.example.aquacontrol.iu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.ui.theme.AmarilloAdvertencia
import com.example.aquacontrol.ui.theme.ContenidoAdvertenciaClaro
import com.example.aquacontrol.ui.theme.ContenidoCriticoClaro
import com.example.aquacontrol.ui.theme.Red

@Composable
// Ofrece Inicio, Granjas y Alertas; muestra los contadores de advertencias y críticos.
fun BottomBar(
    navController: NavController,
    perfil: PerfilUsuario? = null,
    cantidadCriticas: Int = 0,
    cantidadAdvertencias: Int = 0
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val rutaInicio = when (perfil) {
        PerfilUsuario.OPERARIO -> Routes.OPERARIO_HOME
        PerfilUsuario.SUPERVISOR -> Routes.SUPERVISOR_HOME
        null -> Routes.ROLE_SELECTION
    }

    val enGranjas = currentRoute in setOf(
        Routes.GRANJAS,
        Routes.GALPONES_PARAM,
        Routes.LINEAS_PARAM,
        Routes.DETALLE_LINEA_PARAM,
        Routes.HISTORIAL_TEMPERATURAS_PARAM,
        // El detalle de una medición pertenece al recorrido de Granjas.
        Routes.DETALLE_TEMPERATURA_PARAM,
        Routes.REGISTRAR_TEMPERATURA_PARAM,
        Routes.FLUSHING,
        Routes.FLUSHING_PARAM,
        Routes.DETALLE_FLUSHING_PARAM,
        "${Routes.REGISTRAR_FLUSHING}/{lineaId}"
    )

    val enAlertas = currentRoute == Routes.ALERTAS ||
            currentRoute?.startsWith("detalleAlerta/") == true

    val criticas = cantidadCriticas.coerceAtLeast(0)
    val advertencias = cantidadAdvertencias.coerceAtLeast(0)
    val totalAlertas = criticas + advertencias

    val colores = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color.White,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White,
        indicatorColor = Color.White.copy(alpha = 0.18f)
    )

    // Navega desde la barra inferior evitando acumular copias del mismo destino.
    fun abrirDestino(ruta: String) {
        if (currentRoute != ruta) {
            navController.navigate(ruta) {
                popUpTo(
                    navController.graph.findStartDestination().id
                ) {
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    NavigationBar(
        containerColor = Red,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == rutaInicio,
            onClick = { abrirDestino(rutaInicio) },
            colors = colores,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = null
                )
            },
            label = {
                Text("Inicio")
            }
        )

        NavigationBarItem(
            selected = enGranjas,
            onClick = { abrirDestino(Routes.GRANJAS) },
            colors = colores,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null
                )
            },
            label = {
                Text("Granjas")
            }
        )

        NavigationBarItem(
            selected = enAlertas,
            onClick = { abrirDestino(Routes.ALERTAS) },
            colors = colores,
            modifier = Modifier.semantics {
                contentDescription = if (totalAlertas > 0) {
                    "Alertas: $criticas líneas críticas y " +
                            "$advertencias líneas en advertencia"
                } else {
                    "Alertas"
                }
            },
            icon = {
                BadgedBox(
                    badge = {
                        if (totalAlertas > 0) {
                            Surface(
                                shape = CircleShape,
                                color = if (criticas > 0) {
                                    ContenidoCriticoClaro
                                } else {
                                    AmarilloAdvertencia
                                },
                                contentColor = if (criticas > 0) {
                                    Color.White
                                } else {
                                    ContenidoAdvertenciaClaro
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color.White
                                )
                            ) {
                                Text(
                                    text = if (totalAlertas > 99) {
                                        "99+"
                                    } else {
                                        totalAlertas.toString()
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp
                                    ),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null
                    )
                }
            },
            label = {
                Text("Alertas")
            }
        )
    }
}