// Conecta pantallas, repositorios y ViewModels; aplica los permisos del rol seleccionado.
// Conecta con: data/flushing/local/FlushingLocalDataSource.kt (desde com/example/aquacontrol).
// Conecta con: data/temperatura/local/TemperaturaLocalDataSource.kt (desde com/example/aquacontrol).
// Conecta con: notificaciones/NotificadorAlertas.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.iu.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aquacontrol.data.AppDatabase
import com.example.aquacontrol.data.flushing.FotosFlushing
import com.example.aquacontrol.data.flushing.local.FlushingLocalDataSource
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import com.example.aquacontrol.iu.alertas.AlertasScreen
import com.example.aquacontrol.iu.alertas.DetalleAlertaScreen
import com.example.aquacontrol.iu.components.BottomBar
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen
import com.example.aquacontrol.iu.flushing.FlushingDetalleScreen
import com.example.aquacontrol.iu.flushing.FlushingHomeScreen
import com.example.aquacontrol.iu.flushing.FlushingScreen
import com.example.aquacontrol.iu.flushing.RegistrarFlushingScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.historial.HistorialTemperaturaScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.roles.OperarioScreen
import com.example.aquacontrol.iu.roles.RolSelectionScreen
import com.example.aquacontrol.iu.roles.SupervisorScreen
import com.example.aquacontrol.iu.simulacion.SimulacionScreen
import com.example.aquacontrol.iu.temperatura.DetalleTemperaturaScreen
import com.example.aquacontrol.iu.temperatura.RegistrarTemperaturaScreen
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.viewmodel.alertas.AlertasUiState
import com.example.aquacontrol.notificaciones.NotificadorAlertas
import com.example.aquacontrol.repository.flushing.FlushingRepositoryImpl
import com.example.aquacontrol.repository.temperatura.TemperaturaRepositoryImpl
import com.example.aquacontrol.viewmodel.alertas.AlertasViewModel
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel
import com.example.aquacontrol.viewmodel.galpones.GalponViewModel
import com.example.aquacontrol.viewmodel.historial.HistorialTemperaturaViewModel
import com.example.aquacontrol.viewmodel.lineas.LineaViewModel
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import com.example.aquacontrol.viewmodel.supervisor.SupervisorViewModel
import com.example.aquacontrol.viewmodel.temperatura.GraficoTemperaturaViewModel
import com.example.aquacontrol.viewmodel.temperatura.DetalleTemperaturaViewModel
import com.example.aquacontrol.viewmodel.temperatura.RegistrarTemperaturaViewModel

private const val DETALLE_ALERTA = "detalleAlerta"
private const val DETALLE_ALERTA_PARAM =
    "$DETALLE_ALERTA/{lineaId}/{medicionId}"

@Composable
// Conecta pantallas, repositorios y ViewModels; aplica los permisos del rol seleccionado.
fun AppNavHost(
    lineaIdNotificacion: Int? = null,
    medicionIdNotificacion: Long? = null,
    onNotificacionAbierta: () -> Unit = {}
) {
    val navController = rememberNavController()
    val perfilViewModel: PerfilViewModel = viewModel()
    val perfil by perfilViewModel.perfilSeleccionado.collectAsState()
    // Fuente única del permiso que se envía a las pantallas y protege las rutas.
    val puedeRegistrar = perfil == PerfilUsuario.OPERARIO
    val appContext = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    // Comparte la base Room de data/AppDatabase.kt entre todos los repositorios.
    val database = remember(appContext) {
        AppDatabase.getInstance(appContext)
    }

    val flushingRepository = remember(database) {
        FlushingRepositoryImpl(
            FlushingLocalDataSource(database.flushingDao())
        )
    }

    val notificadorAlertas = remember(appContext) {
        NotificadorAlertas(appContext).also {
            it.crearCanales()
        }
    }

    val temperaturaRepository = remember(database, notificadorAlertas) {
        TemperaturaRepositoryImpl(
            localDataSource = TemperaturaLocalDataSource(
                database.medicionTemperaturaDao()
            ),
            notificadorAlertas = notificadorAlertas
        )
    }

    val historialFactory = remember(flushingRepository) {
        viewModelFactory {
            initializer {
                FlushingViewModel(flushingRepository)
            }
        }
    }

    // Inyecta FlushingRepository al ViewModel de registro; esta es la fábrica usada actualmente.
    val registroFactory = remember(flushingRepository) {
        viewModelFactory {
            initializer {
                RegistrarFlushingViewModel(flushingRepository)
            }
        }
    }

    // Inyecta el repositorio y la limpieza de evidencia al detalle de flushing.
    val detalleFlushingFactory = remember(flushingRepository, appContext) {
        viewModelFactory {
            initializer {
                DetalleFlushingViewModel(
                    repository = flushingRepository,
                    eliminarFoto = { nombre ->
                        // El ViewModel invoca esta limpieza solo después de eliminar el evento en Room.
                        FotosFlushing.eliminarFotoNoGuardada(appContext, nombre)
                    }
                )
            }
        }
    }

    val registroTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                RegistrarTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    val historialTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                HistorialTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    // Inyecta el repositorio al detalle que permite editar o eliminar según el rol.
    val detalleTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                DetalleTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    // Proporciona el repositorio al gráfico sin crear otra base de datos.
    val graficoTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                GraficoTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    val galponesFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                GalponViewModel(temperaturaRepository)
            }
        }
    }

    val lineasFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                LineaViewModel(temperaturaRepository)
            }
        }
    }

    val detalleFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                DetalleLineaViewModel(temperaturaRepository)
            }
        }
    }

    val alertasFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                AlertasViewModel(temperaturaRepository)
            }
        }
    }

    val alertasViewModel: AlertasViewModel = viewModel(
        factory = alertasFactory
    )
    // Observa el ViewModel; cada cambio de estado actualiza la interfaz.
    val estadoAlertas by alertasViewModel.uiState.collectAsState()
    val alertasActuales = (estadoAlertas as? AlertasUiState.Success)
        ?.alertas.orEmpty()
    val cantidadCriticas = alertasActuales.count {
        it.medicion.estado == EstadoLinea.CRITICO
    }
    val cantidadAdvertencias = alertasActuales.count {
        it.medicion.estado == EstadoLinea.ADVERTENCIA
    }

    val supervisorFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                SupervisorViewModel(temperaturaRepository)
            }
        }
    }

    val simulacionFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                SimulacionViewModel(temperaturaRepository)
            }
        }
    }

    val simulacionViewModel: SimulacionViewModel = viewModel(
        factory = simulacionFactory
    )

    // Detiene la generación de lecturas cuando el perfil deja de ser Operario.
    LaunchedEffect(puedeRegistrar, simulacionViewModel) {
        // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
        if (!puedeRegistrar) {
            simulacionViewModel.detener()
        }
    }

    // Vincula la simulación al ciclo de vida; ON_STOP la detiene al ir a segundo plano.
    DisposableEffect(lifecycleOwner, simulacionViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                simulacionViewModel.detener()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            simulacionViewModel.detener()
        }
    }

    // Reserva el área de la barra inferior y entrega su margen al contenido navegable.
    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController,
                perfil = perfil,
                cantidadCriticas = cantidadCriticas,
                cantidadAdvertencias = cantidadAdvertencias
            )
        }
    ) { innerPadding ->
        // Declara las rutas y convierte sus parámetros a los IDs usados por cada pantalla.
        NavHost(
            navController = navController,
            startDestination = Routes.ROLE_SELECTION,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Roles
            composable(Routes.ROLE_SELECTION) {
                RolSelectionScreen(navController, perfilViewModel)
            }

            composable(Routes.OPERARIO_HOME) {
                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (puedeRegistrar) {
                    OperarioScreen(navController, perfilViewModel)
                } else {
                    Text("Selecciona el rol Operario desde Inicio para acceder.")
                }
            }

            composable(Routes.SUPERVISOR_HOME) { backStack ->
                val supervisorViewModel: SupervisorViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = supervisorFactory
                )

                SupervisorScreen(
                    navController = navController,
                    perfilViewModel = perfilViewModel,
                    viewModel = supervisorViewModel
                )
            }

            // Granjas
            composable(Routes.GRANJAS) {
                GranjaScreen(navController)
            }

            // Galpones
            composable(Routes.GALPONES_PARAM) { backStack ->
                val granjaId = backStack.arguments
                    ?.getString("granjaId")
                    ?.toIntOrNull() ?: 0

                val galponesViewModel: GalponViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = galponesFactory
                )

                GalponScreen(
                    navController = navController,
                    granjaId = granjaId,
                    viewModel = galponesViewModel
                )
            }

            // Líneas
            composable(
                route = Routes.LINEAS_PARAM,
                arguments = listOf(
                    navArgument("galponId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val galponId = backStack.arguments
                    ?.getInt("galponId") ?: 0

                val lineasViewModel: LineaViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = lineasFactory
                )

                LineaScreen(
                    navController = navController,
                    galponId = galponId,
                    viewModel = lineasViewModel
                )
            }

            // Detalle de línea
            composable(
                route = Routes.DETALLE_LINEA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                val detalleViewModel: DetalleLineaViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = detalleFactory
                )

                // Solo Supervisor recibe el gráfico; cada detalle conserva su propio ViewModel.
                val graficoViewModel: GraficoTemperaturaViewModel? =
                    if (perfil == PerfilUsuario.SUPERVISOR) {
                        viewModel<GraficoTemperaturaViewModel>(
                            viewModelStoreOwner = backStack,
                            factory = graficoTemperaturaFactory
                        )
                    } else {
                        null
                    }

                DetalleLineaScreen(
                    navController = navController,
                    lineaId = lineaId,
                    viewModel = detalleViewModel,
                    puedeRegistrar = puedeRegistrar,
                    graficoViewModel = graficoViewModel
                )
            }

            // Historial de temperaturas con filtros y paginación
            composable(
                route = Routes.HISTORIAL_TEMPERATURAS_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                if (lineaId <= 0) {
                    Text("No se pudo identificar la línea.")
                } else {
                    val historialTemperaturaViewModel:
                            HistorialTemperaturaViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = historialTemperaturaFactory
                    )

                    HistorialTemperaturaScreen(
                        lineaId = lineaId,
                        viewModel = historialTemperaturaViewModel,
                        // Conserva el historial en la pila para regresar con sus filtros.
                        onVerDetalle = { medicionId ->
                            navController.navigate(
                                "${Routes.DETALLE_TEMPERATURA}/$lineaId/$medicionId"
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Detalle de una medición: recibe su identidad y el perfil actual.
            composable(
                route = Routes.DETALLE_TEMPERATURA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") { type = NavType.IntType },
                    navArgument("medicionId") { type = NavType.LongType }
                )
            ) { backStack ->
                val lineaId = backStack.arguments?.getInt("lineaId") ?: 0
                val medicionId = backStack.arguments?.getLong("medicionId") ?: 0L

                if (lineaId <= 0 || medicionId <= 0L) {
                    Text("No se pudo identificar la medición.")
                } else {
                    // Cada entrada mantiene su propio estado y borrador de edición.
                    val detalleTemperaturaViewModel: DetalleTemperaturaViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = detalleTemperaturaFactory
                    )
                    DetalleTemperaturaScreen(
                        lineaId = lineaId,
                        medicionId = medicionId,
                        perfil = perfil,
                        viewModel = detalleTemperaturaViewModel
                    )
                }
            }

            // Registro manual de temperatura
            composable(
                route = Routes.REGISTRAR_TEMPERATURA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (!puedeRegistrar) {
                    Text("El registro está disponible solo para el operario.")
                } else if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else {
                    val registroTemperaturaViewModel:
                            RegistrarTemperaturaViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = registroTemperaturaFactory
                    )

                    RegistrarTemperaturaScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = registroTemperaturaViewModel
                    )
                }
            }

            // Medición que originó una notificación
            composable(
                route = DETALLE_ALERTA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    },
                    navArgument("medicionId") {
                        type = NavType.LongType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                val medicionId = backStack.arguments
                    ?.getLong("medicionId") ?: 0L

                if (lineaId <= 0 || medicionId <= 0L) {
                    Text("No se pudo identificar la alerta.")
                } else {
                    val detalleAlertaViewModel: DetalleLineaViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = detalleFactory
                        )

                    DetalleAlertaScreen(
                        navController = navController,
                        lineaId = lineaId,
                        medicionId = medicionId,
                        viewModel = detalleAlertaViewModel
                    )
                }
            }

            // Historial de flushing
            composable(
                route = Routes.FLUSHING_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else {
                    val historialViewModel: FlushingViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = historialFactory
                    )

                    FlushingScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = historialViewModel,
                        puedeRegistrar = puedeRegistrar
                    )
                }
            }

            // Registro de flushing
            composable(
                route = "${Routes.REGISTRAR_FLUSHING}/{lineaId}",
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (!puedeRegistrar) {
                    Text("El registro está disponible solo para el operario.")
                } else if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else {
                    val registroViewModel: RegistrarFlushingViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = registroFactory
                        )

                    RegistrarFlushingScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = registroViewModel
                    )
                }
            }

            // Detalle de un registro de flushing
            composable(
                route = Routes.DETALLE_FLUSHING_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    },
                    navArgument("eventoId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                val eventoId = backStack.arguments
                    ?.getInt("eventoId") ?: 0

                if (lineaId <= 0 || eventoId <= 0) {
                    Text("No se pudo identificar el registro de flushing.")
                } else {
                    // Estado independiente del historial, con edición y eliminación por perfil.
                    val detalleFlushingViewModel: DetalleFlushingViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = detalleFlushingFactory
                        )

                    FlushingDetalleScreen(
                        lineaId = lineaId,
                        eventoId = eventoId,
                        perfil = perfil,
                        viewModel = detalleFlushingViewModel
                    )
                }
            }

            // Alertas actuales
            composable(Routes.ALERTAS) {

                AlertasScreen(
                    navController = navController,
                    viewModel = alertasViewModel
                )
            }

            // Selección para registrar flushing
            composable(Routes.FLUSHING) {
                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (puedeRegistrar) {
                    FlushingHomeScreen(navController)
                } else {
                    Text("El registro está disponible solo para el operario.")
                }
            }

            // Simulación compartida
            composable(Routes.SIMULACION) {
                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (puedeRegistrar) {
                    SimulacionScreen(
                        navController = navController,
                        viewModel = simulacionViewModel
                    )
                } else {
                    Text("La simulación está disponible solo para el operario.")
                }
            }
        }
    }

    // Abre el detalle de la medición recibida desde MainActivity al tocar una notificación.
    LaunchedEffect(
        lineaIdNotificacion,
        medicionIdNotificacion
    ) {
        val lineaId = lineaIdNotificacion
        val medicionId = medicionIdNotificacion

        if (
            lineaId != null &&
            medicionId != null &&
            lineaId > 0 &&
            medicionId > 0L
        ) {
            navController.navigate(
                "$DETALLE_ALERTA/$lineaId/$medicionId"
            ) {
                launchSingleTop = true
            }

            onNotificacionAbierta()
        }
    }
}