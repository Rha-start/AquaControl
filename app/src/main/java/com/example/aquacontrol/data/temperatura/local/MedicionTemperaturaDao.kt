package com.example.aquacontrol.data.temperatura.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

private const val CONDICIONES_HISTORIAL = """
    WHERE lineaId = :lineaId
      AND (:desdeInclusive IS NULL OR fechaHora >= :desdeInclusive)
      AND (:hastaExclusivo IS NULL OR fechaHora < :hastaExclusivo)
      AND (
          :estado IS NULL
          OR (
              :estado = 'NORMAL'
              AND temperatura >= 18.0
              AND temperatura <= 21.0
          )
          OR (
              :estado = 'ADVERTENCIA'
              AND (
                  (temperatura >= 5.0 AND temperatura < 18.0)
                  OR (temperatura > 21.0 AND temperatura < 30.0)
              )
          )
          OR (
              :estado = 'CRITICO'
              AND (temperatura < 5.0 OR temperatura >= 30.0)
          )
      )
"""

@Dao
interface MedicionTemperaturaDao {

    @Insert
    suspend fun insertarMedicion(
        medicion: MedicionTemperaturaEntity
    ): Long

    @Query(
        """
        SELECT * FROM mediciones_temperatura
        WHERE lineaId = :lineaId
        ORDER BY fechaHora DESC, id DESC
        """
    )
    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperaturaEntity>>

    @Query(
        "SELECT * FROM mediciones_temperatura " +
                CONDICIONES_HISTORIAL +
                """
            ORDER BY fechaHora DESC, id DESC
            LIMIT :limite OFFSET :desplazamiento
            """
    )
    fun observarPaginaHistorial(
        lineaId: Int,
        estado: String?,
        desdeInclusive: Long?,
        hastaExclusivo: Long?,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperaturaEntity>>

    @Query(
        "SELECT COUNT(*) FROM mediciones_temperatura " +
                CONDICIONES_HISTORIAL
    )
    fun observarCantidadHistorial(
        lineaId: Int,
        estado: String?,
        desdeInclusive: Long?,
        hastaExclusivo: Long?
    ): Flow<Int>

    @Query(
        """
        SELECT * FROM mediciones_temperatura
        WHERE lineaId = :lineaId
        ORDER BY fechaHora DESC, id DESC
        LIMIT 1
        """
    )
    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperaturaEntity?>

    @Query(
        """
        SELECT m.*
        FROM mediciones_temperatura AS m
        WHERE m.id = (
            SELECT reciente.id
            FROM mediciones_temperatura AS reciente
            WHERE reciente.lineaId = m.lineaId
            ORDER BY reciente.fechaHora DESC, reciente.id DESC
            LIMIT 1
        )
        ORDER BY m.lineaId ASC
        """
    )
    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>>
}