package com.example.aquacontrol.repository.flushing

import com.example.aquacontrol.data.flushing.local.FlushingEntity
import com.example.aquacontrol.data.flushing.local.FlushingLocalDataSource
import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO

class FlushingRepositoryImpl(
    private val localDataSource: FlushingLocalDataSource
) : FlushingRepository {

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

        val idGenerado = localDataSource.guardarFlushing(entity)

        return entity.copy(
            id = idGenerado.toInt()
        ).toEventoFlushing()
    }

    override suspend fun obtenerFlushingPorLinea(
        lineaId: Int
    ): List<EventoFlushing> {
        require(lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        return localDataSource.obtenerFlushingPorLinea(lineaId)
            .map { entity -> entity.toEventoFlushing() }
    }

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
}