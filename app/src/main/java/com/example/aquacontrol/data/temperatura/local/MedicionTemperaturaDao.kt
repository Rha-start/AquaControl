// Consultas Room de temperaturas: historial, filtros, paginación y últimas lecturas.
package com.example.aquacontrol.data.temperatura.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Filtro SQL compartido; los rangos deben coincidir con model/estado/EstadoLinea.kt.
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
    // Inserta la entidad en Room y devuelve el identificador generado.
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
    // Emite el historial de la línea y sus cambios posteriores.
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
    // Emite solo la página solicitada, respetando filtros de estado y fecha.
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
    // Emite el total filtrado para calcular los límites de paginación.
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
    // Emite la lectura más reciente de la línea o null si aún no hay datos.
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
    // Emite una última lectura por línea para actualizar resúmenes y alertas.
    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>>

    // Busca el registro exacto para editarlo; la línea evita operar sobre otro historial.
    // Conecta con TemperaturaLocalDataSource.kt en este mismo paquete.
    @Query(
        "SELECT * FROM mediciones_temperatura " +
                "WHERE id = :medicionId AND lineaId = :lineaId LIMIT 1"
    )
    suspend fun obtenerMedicion(
        lineaId: Int,
        medicionId: Long
    ): MedicionTemperaturaEntity?

    // Corrige solo el valor: conserva ID, fecha, línea y origen de la medición.
    // Devuelve 1 si actualizó el registro o 0 si ya no existe en esta línea.
    @Query(
        "UPDATE mediciones_temperatura SET temperatura = :temperatura " +
                "WHERE id = :medicionId AND lineaId = :lineaId"
    )
    suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double
    ): Int

    // Elimina únicamente el registro identificado; devuelve la cantidad de filas eliminadas.
    // El permiso de Supervisor y la confirmación se controlan antes de invocar esta operación.
    @Query(
        "DELETE FROM mediciones_temperatura " +
                "WHERE id = :medicionId AND lineaId = :lineaId"
    )
    suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long
    ): Int

    // Consulta solo el intervalo del gráfico; evita cargar todo el historial.
    // El ViewModel renovará los límites para mantener una ventana móvil de 24 horas.
    @Query(
        """
        SELECT * FROM mediciones_temperatura
        WHERE lineaId = :lineaId
          AND fechaHora >= :desdeInclusive
          AND fechaHora <= :hastaInclusive
        ORDER BY fechaHora ASC, id ASC
        """
    )
    fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperaturaEntity>>
}
