package com.example.aquacontrol.viewmodel.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HistorialTemperaturaViewModel(
    private val repository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HistorialTemperaturaUiState()
    )

    val uiState: StateFlow<HistorialTemperaturaUiState> =
        _uiState.asStateFlow()

    private var observacionActual: Job? = null

    fun cargarHistorial(lineaId: Int) {
        // Conserva filtros y página al regresar desde un detalle.
        if (_uiState.value.lineaId == lineaId) return

        observacionActual?.cancel()

        if (lineaId <= 0) {
            _uiState.value = HistorialTemperaturaUiState(
                error = "No se pudo identificar la línea."
            )
            return
        }

        _uiState.value = HistorialTemperaturaUiState(
            lineaId = lineaId
        )

        observarHistorial()
    }

    fun aplicarFiltros(filtro: FiltroHistorialTemperatura) {
        if (!filtro.rangoValido) {
            _uiState.value = _uiState.value.copy(
                errorFiltros =
                    "La fecha inicial no puede ser posterior a la fecha final."
            )
            return
        }

        val actual = _uiState.value

        if (actual.filtro == filtro && actual.error == null) {
            _uiState.value = actual.copy(errorFiltros = null)
            return
        }

        _uiState.value = actual.copy(
            filtro = filtro,
            paginaActual = 0,
            errorFiltros = null
        )

        observarHistorial()
    }

    fun limpiarFiltros() {
        aplicarFiltros(FiltroHistorialTemperatura())
    }

    fun limpiarErrorFiltros() {
        _uiState.value = _uiState.value.copy(
            errorFiltros = null
        )
    }

    fun paginaAnterior() {
        val actual = _uiState.value

        if (!actual.puedeIrAnterior) return

        _uiState.value = actual.copy(
            paginaActual = actual.paginaActual - 1
        )

        observarHistorial()
    }

    fun paginaSiguiente() {
        val actual = _uiState.value

        if (!actual.puedeIrSiguiente) return

        _uiState.value = actual.copy(
            paginaActual = actual.paginaActual + 1
        )

        observarHistorial()
    }

    fun reintentar() {
        observarHistorial()
    }

    private fun observarHistorial() {
        val actual = _uiState.value
        val lineaId = actual.lineaId ?: return
        val filtro = actual.filtro

        observacionActual?.cancel()

        _uiState.value = actual.copy(
            cargando = true,
            mediciones = emptyList(),
            error = null
        )

        observacionActual = viewModelScope.launch {
            try {
                val ubicacion = obtenerUbicacion(lineaId)

                _uiState.value = _uiState.value.copy(
                    ubicacion = ubicacion
                )

                repository.observarCantidadHistorial(
                    lineaId = lineaId,
                    filtro = filtro
                )
                    .distinctUntilChanged()
                    .collectLatest { cantidad ->
                        val ultimaPagina = if (cantidad <= 0) {
                            0
                        } else {
                            (cantidad - 1) /
                                    HistorialTemperaturaUiState.TAMANIO_PAGINA
                        }

                        val pagina = _uiState.value.paginaActual
                            .coerceIn(0, ultimaPagina)

                        _uiState.value = _uiState.value.copy(
                            paginaActual = pagina,
                            totalResultados = cantidad,
                            cargando = true,
                            mediciones = emptyList()
                        )

                        repository.observarPaginaHistorial(
                            lineaId = lineaId,
                            filtro = filtro,
                            limite =
                                HistorialTemperaturaUiState.TAMANIO_PAGINA,
                            desplazamiento = pagina *
                                    HistorialTemperaturaUiState.TAMANIO_PAGINA
                        ).collect { mediciones ->
                            _uiState.value = _uiState.value.copy(
                                mediciones = mediciones,
                                cargando = false,
                                error = null
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mediciones = emptyList(),
                    error =
                        "No se pudo cargar el historial. Inténtalo nuevamente."
                )
            }
        }
    }

    private fun obtenerUbicacion(lineaId: Int): String {
        for (granja in bebederoRepository.obtenerGranjas()) {
            for (galpon in bebederoRepository.obtenerGalpones(granja.id)) {
                val linea = bebederoRepository
                    .obtenerLineas(galpon.id)
                    .firstOrNull { it.id == lineaId }

                if (linea != null) {
                    return "${granja.nombre} · " +
                            "${galpon.nombre} · ${linea.nombre}"
                }
            }
        }

        throw IllegalArgumentException(
            "La línea seleccionada no existe."
        )
    }
}

