// Valida el registro y convierte entre formulario, entidad Room y evento consultable.
// Conecta con: data/flushing/local/FlushingLocalDataSource.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.repository.flushing

import com.example.aquacontrol.data.flushing.local.FlushingEntity
import com.example.aquacontrol.data.flushing.local.FlushingLocalDataSource
import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.model.perfil.PerfilUsuario
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FlushingRepositoryImpl(
    private val localDataSource: FlushingLocalDataSource
) : FlushingRepository {

    // Registra los datos del flushing mediante la dependencia de este componente.
    override suspend fun registrarFlushing(
        dto: FlushingDTO
    ): EventoFlushing {
        val duracion = requireNotNull(dto.duracionSegundos) {
            "Debes indicar la duración del flushing."
        }

        require(duracion > 0) {
            "La duración debe ser mayor que cero."
        }

        require(dto.lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        require(dto.observacion.isNotBlank()) {
            "Debes ingresar una observación."
        }

        require(
            dto.temperaturaAntes.isFinite() &&
                    dto.temperaturaDespues.isFinite()
        ) {
            "Las temperaturas deben ser números válidos."
        }

        val entity = FlushingEntity(
            lineaId = dto.lineaId,
            fechaHora = dto.fechaHora,
            observacion = dto.observacion.trim(),
            temperaturaAntes = dto.temperaturaAntes,
            temperaturaDespues = dto.temperaturaDespues,
            duracionSegundos = duracion,
            fotoRuta = dto.fotoRuta?.takeIf { it.isNotBlank() }
        )

        // Comparte el bloqueo con edición y eliminación entre instancias del repositorio.
        return bloqueoFlushing.withLock {
            val idGenerado = localDataSource.guardarFlushing(entity)
            entity.copy(id = idGenerado.toInt()).toEventoFlushing()
        }
    }

    // Recupera los registros de flushing asociados a la línea indicada.
    override suspend fun obtenerFlushingPorLinea(
        lineaId: Int
    ): List<EventoFlushing> {
        require(lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        return localDataSource.obtenerFlushingPorLinea(lineaId)
            .map { entity -> entity.toEventoFlushing() }
    }

    // Consulta un evento exacto a través de data/flushing/local/FlushingLocalDataSource.kt.
    override suspend fun obtenerEvento(
        lineaId: Int,
        eventoId: Int
    ): EventoFlushing? {
        return localDataSource.obtenerEvento(lineaId, eventoId)?.toEventoFlushing()
    }

    // Solo Operario puede corregir los campos; conserva identidad, fecha y evidencia.
    override suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int,
        perfil: PerfilUsuario
    ): EventoFlushing {
        require(perfil == PerfilUsuario.OPERARIO) {
            "Solo el operario puede editar registros de flushing."
        }
        require(observacion.isNotBlank()) {
            "Ingresa una observación."
        }
        require(temperaturaAntes.isFinite() && temperaturaDespues.isFinite()) {
            "Las temperaturas deben ser números válidos."
        }
        require(duracionSegundos > 0) {
            "La duración debe ser mayor que cero."
        }

        return bloqueoFlushing.withLock {
            val existente = requireNotNull(localDataSource.obtenerEvento(lineaId, eventoId)) {
                "El registro ya no existe en esta línea. Actualiza el historial."
            }
            val actualizadas = localDataSource.actualizarFlushing(
                lineaId = lineaId,
                eventoId = eventoId,
                observacion = observacion,
                temperaturaAntes = temperaturaAntes,
                temperaturaDespues = temperaturaDespues,
                duracionSegundos = duracionSegundos
            )
            check(actualizadas == 1) {
                "No se pudo actualizar el registro. Actualiza el historial."
            }
            existente.copy(
                observacion = observacion.trim(),
                temperaturaAntes = temperaturaAntes,
                temperaturaDespues = temperaturaDespues,
                duracionSegundos = duracionSegundos
            ).toEventoFlushing()
        }
    }

    // Solo Supervisor puede eliminar; devuelve los datos para limpiar la evidencia después.
    override suspend fun eliminarFlushing(
        lineaId: Int,
        eventoId: Int,
        perfil: PerfilUsuario
    ): EventoFlushing {
        require(perfil == PerfilUsuario.SUPERVISOR) {
            "Solo el supervisor puede eliminar registros de flushing."
        }
        return bloqueoFlushing.withLock {
            val existente = requireNotNull(localDataSource.obtenerEvento(lineaId, eventoId)) {
                "El registro ya no existe en esta línea. Actualiza el historial."
            }
            val eliminadas = localDataSource.eliminarFlushing(lineaId, eventoId)
            check(eliminadas == 1) {
                "No se pudo eliminar el registro. Actualiza el historial."
            }
            // La foto no se borra antes de confirmar que Room eliminó el evento.
            existente.toEventoFlushing()
        }
    }

    // Convierte la fila Room al modelo que muestran historial y detalle.
    private fun FlushingEntity.toEventoFlushing(): EventoFlushing {
        return EventoFlushing(
            id = id,
            lineaId = lineaId,
            fechaHora = fechaHora,
            temperaturaAntes = temperaturaAntes,
            temperaturaDespues = temperaturaDespues,
            duracionSegundos = duracionSegundos,
            observacion = observacion,
            fotoRuta = fotoRuta
        )
    }

    companion object {
        // Serializa las operaciones de escritura realizadas por este repositorio.
        private val bloqueoFlushing = Mutex()
    }
}
