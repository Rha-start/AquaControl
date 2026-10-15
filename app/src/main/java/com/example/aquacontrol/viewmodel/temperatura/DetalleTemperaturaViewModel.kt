package com.example.aquacontrol.viewmodel.temperatura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Conecta el detalle con repository/temperatura/TemperaturaRepository.kt.
// El rol recibido corresponde al perfil seleccionado; no representa autenticación.
class DetalleTemperaturaViewModel(
    private val repository: TemperaturaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleTemperaturaUiState())
    val uiState: StateFlow<DetalleTemperaturaUiState> = _uiState.asStateFlow()
    private var cargaActual: Job? = null
    private var destinoActual: Pair<Int, Long>? = null

    // Carga por línea e ID; reintentar fuerza la consulta sin duplicar cargas normales.
    fun cargarMedicion(lineaId: Int, medicionId: Long, forzar: Boolean = false) {
        if (_uiState.value.guardando || _uiState.value.eliminando) return
        val destino = lineaId to medicionId
        if (!forzar && destinoActual == destino) return

        cargaActual?.cancel()
        destinoActual = destino
        _uiState.value = DetalleTemperaturaUiState()

        if (lineaId <= 0 || medicionId <= 0L) {
            _uiState.value = DetalleTemperaturaUiState(
                cargando = false,
                errorCarga = "No se pudo identificar la medición."
            )
            return
        }

        cargaActual = viewModelScope.launch {
            try {
                val medicion = repository.obtenerMedicion(lineaId, medicionId)
                // Descarta una respuesta antigua si la pantalla cambió de registro.
                if (destinoActual != destino) return@launch
                _uiState.value = if (medicion == null) {
                    DetalleTemperaturaUiState(
                        cargando = false,
                        errorCarga = "La medición ya no existe en esta línea."
                    )
                } else {
                    DetalleTemperaturaUiState(
                        cargando = false,
                        medicion = medicion,
                        temperatura = medicion.temperatura.toString()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (destinoActual == destino) {
                    _uiState.value = DetalleTemperaturaUiState(
                        cargando = false,
                        errorCarga = "No se pudo cargar la medición. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    // Abre la edición únicamente para Operario y recupera el valor persistido.
    fun iniciarEdicion(perfil: PerfilUsuario?) {
        val actual = _uiState.value
        val medicion = actual.medicion ?: return
        if (!actual.puedeInteractuar || perfil != PerfilUsuario.OPERARIO) return
        _uiState.value = actual.copy(
            editando = true,
            temperatura = medicion.temperatura.toString(),
            errorTemperatura = null,
            errorOperacion = null,
            mensaje = null
        )
    }

    // Cambia solo el texto del formulario; Room se modifica después de confirmar Guardar.
    fun actualizarTemperatura(valor: String, perfil: PerfilUsuario?) {
        val actual = _uiState.value
        if (!actual.puedeInteractuar || !actual.editando ||
            perfil != PerfilUsuario.OPERARIO
        ) return
        _uiState.value = actual.copy(
            temperatura = valor,
            errorTemperatura = null,
            errorOperacion = null,
            mensaje = null
        )
    }

    // Descarta el borrador sin tocar el registro guardado; también sirve al cambiar de rol.
    fun cancelarEdicion() {
        val actual = _uiState.value
        if (!actual.puedeInteractuar) return
        _uiState.value = actual.copy(
            editando = false,
            temperatura = actual.medicion?.temperatura?.toString().orEmpty(),
            errorTemperatura = null,
            errorOperacion = null,
            mensaje = null
        )
    }

    // La pantalla llama a este método solo al aceptar el diálogo de confirmación.
    fun guardarCambios(perfil: PerfilUsuario?) {
        val actual = _uiState.value
        val medicion = actual.medicion ?: return
        if (!actual.puedeInteractuar || !actual.editando) return
        if (perfil != PerfilUsuario.OPERARIO) {
            _uiState.value = actual.copy(errorOperacion = "Solo el operario puede editar temperaturas.")
            return
        }

        // Acepta coma o punto y rechaza valores vacíos, infinitos o no numéricos.
        val temperatura = actual.temperatura.trim().replace(',', '.')
            .toDoubleOrNull()?.takeIf { it.isFinite() }
        if (temperatura == null) {
            _uiState.value = actual.copy(
                errorTemperatura = "Ingresa una temperatura numérica válida."
            )
            return
        }

        // Bloquea inmediatamente los botones para evitar un segundo envío.
        _uiState.value = actual.copy(guardando = true, errorTemperatura = null, errorOperacion = null)
        viewModelScope.launch {
            try {
                val actualizada = repository.actualizarTemperatura(
                    lineaId = medicion.lineaId,
                    medicionId = medicion.id,
                    temperatura = temperatura,
                    perfil = perfil
                )
                _uiState.value = _uiState.value.copy(
                    medicion = actualizada,
                    temperatura = actualizada.temperatura.toString(),
                    editando = false,
                    mensaje = "Temperatura actualizada correctamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(errorOperacion = e.message ?: "Revisa los datos.")
            } catch (e: IllegalStateException) {
                _uiState.value = _uiState.value.copy(errorOperacion = e.message ?: "No se pudo actualizar.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorOperacion = "No se pudo guardar el cambio. Inténtalo nuevamente.")
            } finally {
                _uiState.value = _uiState.value.copy(guardando = false)
            }
        }
    }

    // La pantalla solicita confirmación antes; el repositorio vuelve a comprobar Supervisor.
    fun eliminarMedicion(perfil: PerfilUsuario?) {
        val actual = _uiState.value
        val medicion = actual.medicion ?: return
        if (!actual.puedeInteractuar) return
        if (perfil != PerfilUsuario.SUPERVISOR) {
            _uiState.value = actual.copy(errorOperacion = "Solo el supervisor puede eliminar temperaturas.")
            return
        }

        _uiState.value = actual.copy(eliminando = true, errorOperacion = null, mensaje = null)
        viewModelScope.launch {
            try {
                repository.eliminarMedicion(medicion.lineaId, medicion.id, perfil)
                _uiState.value = _uiState.value.copy(
                    eliminado = true,
                    editando = false,
                    medicion = null,
                    temperatura = "",
                    mensaje = "Medición eliminada correctamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(errorOperacion = e.message ?: "No se pudo eliminar.")
            } catch (e: IllegalStateException) {
                _uiState.value = _uiState.value.copy(errorOperacion = e.message ?: "No se pudo eliminar.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorOperacion = "No se pudo eliminar la medición. Inténtalo nuevamente.")
            } finally {
                _uiState.value = _uiState.value.copy(eliminando = false)
            }
        }
    }
}
