package com.example.aquacontrol.viewmodel.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.linea.LineaMonitoreada
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DetalleLineaViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DetalleLineaUiState>(
            DetalleLineaUiState.Loading
        )

    val uiState: StateFlow<DetalleLineaUiState> =
        _uiState.asStateFlow()

    private var observacionActual: Job? = null

    fun cargarResumen(lineaId: Int) {
        cargarDatos(
            lineaId = lineaId,
            soloUltimaMedicion = true
        )
    }

    fun cargarHistorial(lineaId: Int) {
        cargarDatos(
            lineaId = lineaId,
            soloUltimaMedicion = false
        )
    }

    private fun cargarDatos(
        lineaId: Int,
        soloUltimaMedicion: Boolean
    ) {
        observacionActual?.cancel()

        if (lineaId <= 0) {
            _uiState.value = DetalleLineaUiState.Error(
                mensaje = "No se pudo identificar la línea seleccionada."
            )
            return
        }

        _uiState.value = DetalleLineaUiState.Loading

        observacionActual = viewModelScope.launch {
            try {
                val ubicacion = buscarUbicacion(lineaId)

                if (ubicacion == null) {
                    _uiState.value = DetalleLineaUiState.Error(
                        mensaje = "La línea seleccionada no existe."
                    )
                    return@launch
                }

                val mediciones = if (soloUltimaMedicion) {
                    temperaturaRepository
                        .observarUltimaMedicionPorLinea(lineaId)
                        .map { ultimaMedicion ->
                            listOfNotNull(ultimaMedicion)
                        }
                } else {
                    // Las alertas necesitan encontrar la medición
                    // original, aunque existan registros posteriores.
                    temperaturaRepository
                        .observarHistorialPorLinea(lineaId)
                }

                mediciones.collect { historial ->
                    val linea = LineaMonitoreada(
                        id = lineaId,
                        nombre = ubicacion.nombreLinea,
                        galponId = ubicacion.galponId,
                        ultimaMedicion = historial.firstOrNull()
                    )

                    _uiState.value = DetalleLineaUiState.Success(
                        nombreGranja = ubicacion.nombreGranja,
                        nombreGalpon = ubicacion.nombreGalpon,
                        linea = linea,
                        historial = historial
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DetalleLineaUiState.Error(
                    mensaje = "No se pudieron cargar los datos de la línea. Intenta nuevamente."
                )
            }
        }
    }

    private fun buscarUbicacion(lineaId: Int): UbicacionLinea? {
        for (granja in bebederoRepository.obtenerGranjas()) {
            for (galpon in bebederoRepository.obtenerGalpones(granja.id)) {
                val linea = bebederoRepository.obtenerLineas(galpon.id)
                    .firstOrNull { it.id == lineaId }

                if (linea != null) {
                    return UbicacionLinea(
                        nombreGranja = granja.nombre,
                        nombreGalpon = galpon.nombre,
                        galponId = galpon.id,
                        nombreLinea = linea.nombre
                    )
                }
            }
        }

        return null
    }

    private data class UbicacionLinea(
        val nombreGranja: String,
        val nombreGalpon: String,
        val galponId: Int,
        val nombreLinea: String
    )
}