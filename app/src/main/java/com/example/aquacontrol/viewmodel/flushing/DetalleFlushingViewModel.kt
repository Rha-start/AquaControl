package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.repository.flushing.FlushingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Conecta el detalle con FlushingRepository; AppNavHost inyectará la limpieza de fotos.
class DetalleFlushingViewModel(
    private val repository: FlushingRepository,
    private val eliminarFoto: (String) -> Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleFlushingUiState())
    val uiState: StateFlow<DetalleFlushingUiState> = _uiState.asStateFlow()
    private var cargaActual: Job? = null
    private var destinoActual: Pair<Int, Int>? = null

    // Carga el evento exacto sin reiniciar el borrador en cada recomposición.
    fun cargarEvento(lineaId: Int, eventoId: Int, forzar: Boolean = false) {
        if (_uiState.value.guardando || _uiState.value.eliminando) return
        val destino = lineaId to eventoId
        if (!forzar && destinoActual == destino) return
        cargaActual?.cancel()
        destinoActual = destino
        _uiState.value = DetalleFlushingUiState()

        if (lineaId <= 0 || eventoId <= 0) {
            _uiState.value = DetalleFlushingUiState(
                cargando = false,
                errorCarga = "No se pudo identificar el registro de flushing."
            )
            return
        }
        cargaActual = viewModelScope.launch {
            try {
                val evento = repository.obtenerEvento(lineaId, eventoId)
                if (destinoActual != destino) return@launch
                _uiState.value = if (evento == null) {
                    DetalleFlushingUiState(
                        cargando = false,
                        errorCarga = "El registro ya no existe en esta línea."
                    )
                } else {
                    estadoDesdeEvento(evento)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (destinoActual == destino) {
                    _uiState.value = DetalleFlushingUiState(
                        cargando = false,
                        errorCarga = "No se pudo cargar el registro. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    // Prepara el borrador del Operario, conservando fecha y fotografía originales.
    fun iniciarEdicion(perfil: PerfilUsuario?) {
        val actual = _uiState.value
        val evento = actual.evento ?: return
        if (!actual.puedeInteractuar || perfil != PerfilUsuario.OPERARIO) return
        _uiState.value = estadoDesdeEvento(evento).copy(editando = true)
    }

    // Descarta cambios escritos sin modificar Room; también se usa al cambiar de rol.
    fun cancelarEdicion() {
        val actual = _uiState.value
        val evento = actual.evento ?: return
        if (!actual.puedeInteractuar) return
        _uiState.value = estadoDesdeEvento(evento)
    }

    // Las actualizaciones siguientes solo cambian el borrador y limpian el error del campo.
    fun actualizarObservacion(valor: String, perfil: PerfilUsuario?) = editar(perfil) {
        it.copy(observacion = valor, errorObservacion = null)
    }

    fun actualizarTemperaturaAntes(valor: String, perfil: PerfilUsuario?) = editar(perfil) {
        it.copy(temperaturaAntes = valor, errorTemperaturaAntes = null)
    }

    fun actualizarTemperaturaDespues(valor: String, perfil: PerfilUsuario?) = editar(perfil) {
        it.copy(temperaturaDespues = valor, errorTemperaturaDespues = null)
    }

    fun actualizarDuracion(valor: String, perfil: PerfilUsuario?) = editar(perfil) {
        it.copy(duracionSegundos = valor, errorDuracion = null)
    }

    // Centraliza el permiso de edición y bloquea cambios durante una operación.
    private fun editar(
        perfil: PerfilUsuario?,
        cambio: (DetalleFlushingUiState) -> DetalleFlushingUiState
    ) {
        val actual = _uiState.value
        if (!actual.puedeInteractuar || !actual.editando || perfil != PerfilUsuario.OPERARIO) return
        _uiState.value = cambio(actual).copy(errorOperacion = null, mensaje = null)
    }

    // Se invoca después de confirmar Guardar en la pantalla.
    fun guardarCambios(perfil: PerfilUsuario?) {
        val actual = _uiState.value
        val evento = actual.evento ?: return
        if (!actual.puedeInteractuar || !actual.editando) return
        if (perfil != PerfilUsuario.OPERARIO) {
            _uiState.value = actual.copy(errorOperacion = "Solo el operario puede editar flushing.")
            return
        }

        val antes = convertirTemperatura(actual.temperaturaAntes)
        val despues = convertirTemperatura(actual.temperaturaDespues)
        val duracion = actual.duracionSegundos.trim().toIntOrNull()
        val validado = actual.copy(
            errorObservacion = if (actual.observacion.isBlank()) "Ingresa una observación." else null,
            errorTemperaturaAntes = if (antes == null) "Ingresa una temperatura válida." else null,
            errorTemperaturaDespues = if (despues == null) "Ingresa una temperatura válida." else null,
            errorDuracion = if (duracion == null || duracion <= 0) "Ingresa un entero mayor que cero." else null,
            errorOperacion = null,
            mensaje = null
        )
        _uiState.value = validado
        if (validado.errorObservacion != null || antes == null || despues == null ||
            duracion == null || duracion <= 0
        ) return

        // Activa el bloqueo antes de lanzar la corrutina para evitar doble envío.
        _uiState.value = validado.copy(guardando = true)
        viewModelScope.launch {
            try {
                val actualizado = repository.actualizarFlushing(
                    lineaId = evento.lineaId,
                    eventoId = evento.id,
                    observacion = actual.observacion,
                    temperaturaAntes = antes,
                    temperaturaDespues = despues,
                    duracionSegundos = duracion,
                    perfil = perfil
                )
                _uiState.value = estadoDesdeEvento(actualizado).copy(
                    mensaje = "Flushing actualizado correctamente."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                informarError(e.message ?: "Revisa los datos ingresados.")
            } catch (e: IllegalStateException) {
                informarError(e.message ?: "No se pudo actualizar el registro.")
            } catch (e: Exception) {
                informarError("No se pudo guardar el cambio. Inténtalo nuevamente.")
            } finally {
                _uiState.value = _uiState.value.copy(guardando = false)
            }
        }
    }

    // Solo Supervisor: primero elimina en Room y después limpia la evidencia privada.
    fun eliminarEvento(perfil: PerfilUsuario?) {
        val actual = _uiState.value
        val evento = actual.evento ?: return
        if (!actual.puedeInteractuar) return
        if (perfil != PerfilUsuario.SUPERVISOR) {
            informarError("Solo el supervisor puede eliminar flushing.")
            return
        }
        _uiState.value = actual.copy(eliminando = true, errorOperacion = null, mensaje = null)
        viewModelScope.launch {
            try {
                // Una vez iniciada, termina la eliminación y su limpieza aunque se abandone la pantalla.
                // No protege frente al cierre forzado del proceso; Room y el archivo son almacenamientos distintos.
                withContext(NonCancellable) {
                    val eliminado = repository.eliminarFlushing(evento.lineaId, evento.id, perfil)
                    val foto = eliminado.fotoRuta
                    val fotoLimpia = if (foto.isNullOrBlank()) {
                        true
                    } else {
                        // El acceso a archivos se realiza fuera del hilo de la interfaz.
                        withContext(Dispatchers.IO) {
                            try {
                                eliminarFoto(foto)
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }
                    _uiState.value = DetalleFlushingUiState(
                        cargando = false,
                        eliminado = true,
                        mensaje = if (fotoLimpia) {
                            "Flushing eliminado correctamente."
                        } else {
                            "Flushing eliminado. No se pudo borrar su archivo de fotografía."
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                informarError(e.message ?: "No se pudo eliminar el registro.")
            } catch (e: IllegalStateException) {
                informarError(e.message ?: "No se pudo eliminar el registro.")
            } catch (e: Exception) {
                informarError("No se pudo eliminar el registro. Inténtalo nuevamente.")
            } finally {
                _uiState.value = _uiState.value.copy(eliminando = false)
            }
        }
    }

    // Acepta coma o punto decimal y descarta números infinitos o inválidos.
    private fun convertirTemperatura(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    private fun informarError(mensaje: String) {
        _uiState.value = _uiState.value.copy(errorOperacion = mensaje)
    }

    // Reconstruye los campos del formulario a partir del evento persistido.
    private fun estadoDesdeEvento(evento: EventoFlushing) = DetalleFlushingUiState(
        cargando = false,
        evento = evento,
        observacion = evento.observacion,
        temperaturaAntes = evento.temperaturaAntes.toString(),
        temperaturaDespues = evento.temperaturaDespues.toString(),
        duracionSegundos = evento.duracionSegundos.toString()
    )
}
