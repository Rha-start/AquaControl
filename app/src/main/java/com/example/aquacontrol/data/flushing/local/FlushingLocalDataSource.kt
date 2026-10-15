package com.example.aquacontrol.data.flushing.local

// Conecta FlushingRepositoryImpl con las consultas de FlushingDao.
class FlushingLocalDataSource(
    private val dao: FlushingDao
) {

    // Guarda un evento nuevo y devuelve el identificador generado por Room.
    suspend fun guardarFlushing(entity: FlushingEntity): Long {
        return dao.insertarFlushing(entity)
    }

    // Recupera los eventos que muestra el historial de la línea.
    suspend fun obtenerFlushingPorLinea(
        lineaId: Int
    ): List<FlushingEntity> {
        return dao.obtenerFlushingPorLinea(lineaId)
    }

    // Busca el registro exacto para consultar, editar o preparar su eliminación.
    suspend fun obtenerEvento(
        lineaId: Int,
        eventoId: Int
    ): FlushingEntity? {
        validarIdentificadores(lineaId, eventoId)
        return dao.obtenerEvento(lineaId, eventoId)
    }

    // Valida los datos y delega la corrección al DAO.
    // La consulta conserva fecha, línea y fotografía del evento.
    suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int
    ): Int {
        validarIdentificadores(lineaId, eventoId)

        require(observacion.isNotBlank()) {
            "Ingresa una observación."
        }
        require(
            temperaturaAntes.isFinite() &&
                    temperaturaDespues.isFinite()
        ) {
            "Las temperaturas deben ser números válidos."
        }
        require(duracionSegundos > 0) {
            "La duración debe ser mayor que cero."
        }

        return dao.actualizarFlushing(
            lineaId = lineaId,
            eventoId = eventoId,
            observacion = observacion.trim(),
            temperaturaAntes = temperaturaAntes,
            temperaturaDespues = temperaturaDespues,
            duracionSegundos = duracionSegundos
        )
    }

    // Elimina la fila indicada y devuelve cuántos registros fueron eliminados.
    // La limpieza de la fotografía se gestiona fuera del DAO.
    suspend fun eliminarFlushing(
        lineaId: Int,
        eventoId: Int
    ): Int {
        validarIdentificadores(lineaId, eventoId)
        return dao.eliminarFlushing(lineaId, eventoId)
    }

    // Evita consultar o modificar registros con identificadores inválidos.
    private fun validarIdentificadores(lineaId: Int, eventoId: Int) {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }
        require(eventoId > 0) {
            "Selecciona un registro de flushing válido."
        }
    }
}