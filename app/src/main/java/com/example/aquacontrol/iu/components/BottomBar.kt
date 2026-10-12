package com.example.aquacontrol.iu.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.perfil.PerfilUsuario

@Composable
fun BottomBar(
    navController: NavController,
    perfil: PerfilUsuario? = null
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
        Routes.REGISTRAR_TEMPERATURA_PARAM,
        Routes.FLUSHING_PARAM,
        Routes.DETALLE_FLUSHING_PARAM
    )

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == rutaInicio,
            onClick = {
                if (currentRoute != rutaInicio) {
                    navController.navigate(rutaInicio) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio"
                )
            },
            label = {
                Text("Inicio")
            }
        )

        NavigationBarItem(
            selected = enGranjas,
            onClick = {
                if (currentRoute != Routes.GRANJAS) {
                    navController.navigate(Routes.GRANJAS) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Granjas"
                )
            },
            label = {
                Text("Granjas")
            }
        )
    }
}