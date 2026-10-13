package com.example.aquacontrol.viewmodel.galpones

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

class GalponViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<GalponUiState>(GalponUiState.Loading)

    val uiState: StateFlow<GalponUiState> =
        _uiState.asStateFlow()

    private var observacionActual: Job? = null
    private var granjaActual: Int? = null

    fun cargarGalpones(granjaId: Int) {
        if (
            granjaActual == granjaId &&
            observacionActual?.isActive == true &&
            _uiState.value is GalponUiState.Success
        ) {
            return
        }

        observacionActual?.cancel()
        granjaActual = granjaId
        _uiState.value = GalponUiState.Loading

        observacionActual = viewModelScope.launch {
            try {
                val galpones = bebederoRepository.obtenerGalpones(granjaId)

                if (galpones.isEmpty()) {
                    _uiState.value = GalponUiState.Empty
                    return@launch
                }

                val idsLineasPorGalpon = galpones.associate { galpon ->
                    galpon.id to bebederoRepository
                        .obtenerLineas(galpon.id)
                        .map { linea -> linea.id }
                        .toSet()
                }

                temperaturaRepository.observarUltimasMediciones()
                    .collect { mediciones ->
                        val medicionesPorLinea = mediciones.associateBy {
                            it.lineaId
                        }

                        val resumenes = idsLineasPorGalpon.mapValues {
                                (_, idsLineas) ->

                            var normales = 0
                            var advertencias = 0
                            var criticas = 0
                            var sinMediciones = 0

                            for (lineaId in idsLineas) {
                                when (medicionesPorLinea[lineaId]?.estado) {
                                    EstadoLinea.NORMAL -> normales++
                                    EstadoLinea.ADVERTENCIA -> advertencias++
                                    EstadoLinea.CRITICO -> criticas++
                                    null -> sinMediciones++
                                }
                            }

                            ResumenEstadoGalpon(
                                lineasNormales = normales,
                                lineasEnAdvertencia = advertencias,
                                lineasCriticas = criticas,
                                lineasSinMediciones = sinMediciones
                            )
                        }

                        _uiState.value = GalponUiState.Success(
                            galpones = galpones,
                            resumenesPorGalpon = resumenes
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = GalponUiState.Error(
                    mensaje = "No se pudieron cargar los galpones y sus estados. Intenta nuevamente."
                )
            }
        }
    }
}