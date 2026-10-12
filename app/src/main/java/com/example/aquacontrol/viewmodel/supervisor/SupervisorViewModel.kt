package com.example.aquacontrol.viewmodel.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SupervisorViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SupervisorUiState>(SupervisorUiState.Loading)

    val uiState: StateFlow<SupervisorUiState> =
        _uiState.asStateFlow()

    private var observacionActual: Job? = null

    init {
        cargarResumen()
    }

    fun cargarResumen() {
        observacionActual?.cancel()
        _uiState.value = SupervisorUiState.Loading

        observacionActual = viewModelScope.launch {
            try {
                val idsLineas = obtenerIdsLineas()

                temperaturaRepository.observarUltimasMediciones()
                    .collect { mediciones ->
                        val medicionesPorLinea = mediciones.associateBy {
                            it.lineaId
                        }

                        var normales = 0
                        var advertencias = 0
                        var criticas = 0
                        var sinMediciones = 0

                        for (lineaId in idsLineas) {
                            val medicion = medicionesPorLinea[lineaId]

                            when (medicion?.estado) {
                                EstadoLinea.NORMAL -> normales++
                                EstadoLinea.ADVERTENCIA -> advertencias++
                                EstadoLinea.CRITICO -> criticas++
                                null -> sinMediciones++
                            }
                        }

                        _uiState.value = SupervisorUiState.Success(
                            lineasNormales = normales,
                            lineasEnAdvertencia = advertencias,
                            lineasCriticas = criticas,
                            lineasSinMediciones = sinMediciones
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = SupervisorUiState.Error(
                    mensaje = "No se pudo cargar el resumen. Intenta nuevamente."
                )
            }
        }
    }

    private fun obtenerIdsLineas(): Set<Int> {
        val idsLineas = mutableSetOf<Int>()

        for (granja in bebederoRepository.obtenerGranjas()) {
            for (galpon in bebederoRepository.obtenerGalpones(granja.id)) {
                for (linea in bebederoRepository.obtenerLineas(galpon.id)) {
                    idsLineas.add(linea.id)
                }
            }
        }

        return idsLineas
    }
}