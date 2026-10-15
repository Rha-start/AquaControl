package com.example.aquacontrol.data.flushing.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Consultas Room utilizadas por FlushingLocalDataSource.
@Dao
interface FlushingDao {

    // Inserta un registro nuevo y devuelve el ID generado.
    @Insert
    suspend fun insertarFlushing(evento: FlushingEntity): Long

    // Recupera el historial de la línea, del registro más reciente al más antiguo.
    @Query(
        "SELECT * FROM flushing " +
                "WHERE lineaId = :lineaId " +
                "ORDER BY fechaHora DESC, id DESC"
    )
    suspend fun obtenerFlushingPorLinea(
        lineaId: Int
    ): List<FlushingEntity>

    // Busca el evento exacto; comprobar también la línea evita mezclar historiales.
    @Query(
        "SELECT * FROM flushing " +
                "WHERE id = :eventoId AND lineaId = :lineaId LIMIT 1"
    )
    suspend fun obtenerEvento(
        lineaId: Int,
        eventoId: Int
    ): FlushingEntity?

    // Corrige los campos del registro conservando fecha, línea y fotografía.
    // Devuelve 1 si actualizó el evento o 0 si ya no existe en esta línea.
    @Query(
        """
        UPDATE flushing
        SET observacion = :observacion,
            temperaturaAntes = :temperaturaAntes,
            temperaturaDespues = :temperaturaDespues,
            duracionSegundos = :duracionSegundos
        WHERE id = :eventoId AND lineaId = :lineaId
        """
    )
    suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int
    ): Int

    // Elimina solo el evento indicado y devuelve la cantidad de filas eliminadas.
    // El permiso de Supervisor y la confirmación se comprobarán antes de invocarlo.
    @Query(
        "DELETE FROM flushing " +
                "WHERE id = :eventoId AND lineaId = :lineaId"
    )
    suspend fun eliminarFlushing(
        lineaId: Int,
        eventoId: Int
    ): Int
}