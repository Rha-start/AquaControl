package com.example.aquacontrol.viewmodel.temperatura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

// Conecta el repositorio con el gráfico; solo consulta, no modifica mediciones.
@OptIn(ExperimentalCoroutinesApi::class)
class GraficoTemperaturaViewModel(
    private val repository: TemperaturaRepository
) : ViewModel() {

    // Un nuevo número de intento permite reintentar incluso sobre la misma línea.
    private data class Solicitud(val lineaId: Int, val intento: Long = 0L)

    private val solicitud = MutableStateFlow<Solicitud?>(null)

    // Cambiar de línea cancela la consulta anterior y evita mezclar sus mediciones.
    val uiState: StateFlow<GraficoTemperaturaUiState> = solicitud
        .flatMapLatest { actual ->
            when {
                actual == null -> flowOf(GraficoTemperaturaUiState())
                actual.lineaId <= 0 -> flowOf(
                    GraficoTemperaturaUiState(
                        cargando = false,
                        error = "No se pudo identificar la línea seleccionada."
                    )
                )
                else -> observarVentana(actual.lineaId)
            }
        }
        // Detiene las consultas y el reloj cuando nadie observa esta pantalla.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 0L,
                replayExpirationMillis = 0L
            ),
            initialValue = GraficoTemperaturaUiState()
        )

    // La pantalla solicita la línea una vez al entrar; recomponer no reinicia la carga.
    fun cargar(lineaId: Int) {
        if (solicitud.value?.lineaId != lineaId) {
            solicitud.value = Solicitud(lineaId)
        }
    }

    // Reinicia el flujo después de un error de consulta.
    fun reintentar() {
        val actual = solicitud.value ?: return
        solicitud.value = actual.copy(intento = actual.intento + 1L)
    }

    private fun observarVentana(lineaId: Int) = flow {
        // Renueva el intervalo aunque no se registren datos nuevos.
        while (true) {
            emit(System.currentTimeMillis())
            delay(INTERVALO_ACTUALIZACION_MS)
        }
    }.flatMapLatest { hasta ->
        val desde = hasta - DURACION_VENTANA_MS
        repository.observarMedicionesEnIntervalo(
            lineaId = lineaId,
            desdeInclusive = desde,
            hastaInclusive = hasta
        ).map { mediciones ->
            // Room actualiza las ediciones y eliminaciones dentro del intervalo.
            // Las lecturas posteriores al límite entran en la siguiente renovación.
            GraficoTemperaturaUiState(
                cargando = false,
                desdeInclusive = desde,
                hastaInclusive = hasta,
                mediciones = mediciones
            )
        }
    }.onStart {
        // Se muestra carga al entrar o reintentar, no cada 30 segundos.
        emit(GraficoTemperaturaUiState())
    }.catch { error ->
        if (error is CancellationException) throw error
        emit(
            GraficoTemperaturaUiState(
                cargando = false,
                error = "No se pudo cargar el gráfico. Intenta nuevamente."
            )
        )
    }

    private companion object {
        const val DURACION_VENTANA_MS = 24L * 60L * 60L * 1000L
        const val INTERVALO_ACTUALIZACION_MS = 30_000L
    }
}
