package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.repository.flushing.FlushingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarFlushingViewModel(
    private val repository: FlushingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarFlushingUiState())

    val uiState: StateFlow<RegistrarFlushingUiState> =
        _uiState.asStateFlow()

    fun seleccionarLinea(lineaId: Int) {
        editar {
            it.copy(
                lineaId = lineaId,
                errorLinea = null
            )
        }
    }

    fun actualizarObservacion(valor: String) {
        editar {
            it.copy(
                observacion = valor,
                errorObservacion = null
            )
        }
    }

    fun actualizarTemperaturaAntes(valor: String) {
        editar {
            it.copy(
                temperaturaAntes = valor,
                errorTemperaturaAntes = null
            )
        }
    }

    fun actualizarTemperaturaDespues(valor: String) {
        editar {
            it.copy(
                temperaturaDespues = valor,
                errorTemperaturaDespues = null
            )
        }
    }

    fun actualizarDuracion(valor: String) {
        editar {
            it.copy(
                duracionSegundos = valor,
                errorDuracion = null
            )
        }
    }

    fun actualizarFoto(ruta: String?) {
        editar {
            it.copy(
                fotoRuta = ruta?.takeIf { valor -> valor.isNotBlank() },
                errorFoto = null
            )
        }
    }

    fun informarErrorFoto(mensaje: String?) {
        editar {
            it.copy(errorFoto = mensaje)
        }
    }

    private fun editar(
        cambio: (RegistrarFlushingUiState) -> RegistrarFlushingUiState
    ) {
        val actual = _uiState.value

        if (actual.guardando || actual.eventoGuardado != null) return

        _uiState.value = cambio(actual).copy(
            errorGuardado = null
        )
    }

    fun registrarFlushing() {
        val actual = _uiState.value

        if (actual.guardando || actual.eventoGuardado != null) return

        val lineaId = actual.lineaId
        val temperaturaAntes = convertirTemperatura(actual.temperaturaAntes)
        val temperaturaDespues = convertirTemperatura(actual.temperaturaDespues)
        val duracion = actual.duracionSegundos.trim().toIntOrNull()

        val errorLinea = if (lineaId == null || lineaId <= 0) {
            "Selecciona una línea."
        } else {
            null
        }

        val errorObservacion = if (actual.observacion.isBlank()) {
            "Ingresa una observación."
        } else {
            null
        }

        val errorAntes = validarTemperatura(
            actual.temperaturaAntes,
            temperaturaAntes
        )

        val errorDespues = validarTemperatura(
            actual.temperaturaDespues,
            temperaturaDespues
        )

        val errorDuracion = when {
            actual.duracionSegundos.isBlank() ->
                "Ingresa la duración en segundos."

            duracion == null || duracion <= 0 ->
                "Ingresa un número entero mayor que cero."

            else -> null
        }

        val estadoValidado = actual.copy(
            errorLinea = errorLinea,
            errorObservacion = errorObservacion,
            errorTemperaturaAntes = errorAntes,
            errorTemperaturaDespues = errorDespues,
            errorDuracion = errorDuracion,
            errorGuardado = null,
            eventoGuardado = null
        )

        _uiState.value = estadoValidado

        val hayErrores = listOf(
            errorLinea,
            errorObservacion,
            errorAntes,
            errorDespues,
            errorDuracion
        ).any { it != null }

        if (hayErrores) return

        if (
            lineaId == null ||
            temperaturaAntes == null ||
            temperaturaDespues == null ||
            duracion == null
        ) {
            return
        }

        val dto = FlushingDTO(
            fechaHora = obtenerFechaActual(),
            lineaId = lineaId,
            observacion = actual.observacion.trim(),
            temperaturaAntes = temperaturaAntes,
            temperaturaDespues = temperaturaDespues,
            duracionSegundos = duracion,
            fotoRuta = actual.fotoRuta
        )

        _uiState.value = estadoValidado.copy(guardando = true)

        viewModelScope.launch {
            try {
                val evento = repository.registrarFlushing(dto)

                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardado = null,
                    eventoGuardado = evento
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardado =
                        "No se pudo guardar el flushing. Inténtalo nuevamente.",
                    eventoGuardado = null
                )
            } finally {
                if (_uiState.value.guardando) {
                    _uiState.value = _uiState.value.copy(
                        guardando = false
                    )
                }
            }
        }
    }

    private fun convertirTemperatura(texto: String): Double? {
        return texto.trim()
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() }
    }

    private fun validarTemperatura(
        texto: String,
        temperatura: Double?
    ): String? {
        return when {
            texto.isBlank() -> "Ingresa la temperatura."
            temperatura == null -> "Ingresa una temperatura numérica válida."
            else -> null
        }
    }

    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )
        return formato.format(Date())
    }
}