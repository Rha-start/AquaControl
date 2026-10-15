// Contrato de registro y observación de mediciones que consumen los ViewModels.
package com.example.aquacontrol.repository.temperatura

import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import kotlinx.coroutines.flow.Flow

interface TemperaturaRepository {

    // Registra una lectura con su línea, temperatura y origen.
    suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ): MedicionTemperatura

    // Emite el historial de la línea y sus cambios posteriores.
    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperatura>>

    // Emite solo la página solicitada, respetando filtros de estado y fecha.
    fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperatura>>

    // Emite el total filtrado para calcular los límites de paginación.
    fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int>

    // Emite la lectura más reciente de la línea o null si aún no hay datos.
    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperatura?>

    // Emite una última lectura por línea para actualizar resúmenes y alertas.
    fun observarUltimasMediciones():
            Flow<List<MedicionTemperatura>>

    // Recupera una medición de la línea; null indica que el registro ya no existe.
    // La implementación conecta con data/temperatura/local/TemperaturaLocalDataSource.kt.
    suspend fun obtenerMedicion(
        lineaId: Int,
        medicionId: Long
    ): MedicionTemperatura?

    // Corrige solo la temperatura y devuelve el registro actualizado, conservando su identidad.
    // La implementación debe comprobar que el perfil seleccionado sea OPERARIO.
    suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double,
        perfil: PerfilUsuario
    ): MedicionTemperatura

    // Elimina la medición indicada tras la confirmación de la pantalla.
    // La implementación debe comprobar que el perfil seleccionado sea SUPERVISOR.
    suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long,
        perfil: PerfilUsuario
    )

    // Entrega al ViewModel las mediciones del intervalo en orden cronológico.
    // TemperaturaRepositoryImpl conecta esta consulta con Room para el gráfico.
    fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperatura>>
}
