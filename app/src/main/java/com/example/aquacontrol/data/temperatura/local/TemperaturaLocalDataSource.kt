// Traduce el filtro del dominio a parámetros del DAO de temperaturas.
package com.example.aquacontrol.data.temperatura.local

import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import kotlinx.coroutines.flow.Flow

class TemperaturaLocalDataSource(
    private val dao: MedicionTemperaturaDao
) {

    // Delega al DAO la inserción de la medición y devuelve su ID.
    suspend fun guardarMedicion(
        medicion: MedicionTemperaturaEntity
    ): Long {
        return dao.insertarMedicion(medicion)
    }

    // Emite el historial de la línea y sus cambios posteriores.
    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarHistorialPorLinea(lineaId)
    }

    // Emite solo la página solicitada, respetando filtros de estado y fecha.
    fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperaturaEntity>> {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }

        require(filtro.rangoValido) {
            "La fecha inicial no puede ser posterior a la fecha final."
        }

        require(limite in 1..100) {
            "El tamaño de página debe estar entre 1 y 100."
        }

        require(desplazamiento >= 0) {
            "La posición de la página no puede ser negativa."
        }

        return dao.observarPaginaHistorial(
            lineaId = lineaId,
            estado = filtro.estado?.name,
            desdeInclusive = filtro.desdeInclusive,
            hastaExclusivo = filtro.hastaExclusivo,
            limite = limite,
            desplazamiento = desplazamiento
        )
    }

    // Emite el total filtrado para calcular los límites de paginación.
    fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int> {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }

        require(filtro.rangoValido) {
            "La fecha inicial no puede ser posterior a la fecha final."
        }

        return dao.observarCantidadHistorial(
            lineaId = lineaId,
            estado = filtro.estado?.name,
            desdeInclusive = filtro.desdeInclusive,
            hastaExclusivo = filtro.hastaExclusivo
        )
    }

    // Emite la lectura más reciente de la línea o null si aún no hay datos.
    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperaturaEntity?> {
        return dao.observarUltimaMedicionPorLinea(lineaId)
    }

    // Emite una última lectura por línea para actualizar resúmenes y alertas.
    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarUltimasMediciones()
    }

    // Busca una medición concreta mediante MedicionTemperaturaDao, dentro de su línea.
    suspend fun obtenerMedicion(
        lineaId: Int,
        medicionId: Long
    ): MedicionTemperaturaEntity? {
        validarIdentificadores(lineaId, medicionId)
        return dao.obtenerMedicion(lineaId, medicionId)
    }

    // Entrega la corrección al DAO; devuelve las filas afectadas para detectar registros inexistentes.
    suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double
    ): Int {
        validarIdentificadores(lineaId, medicionId)
        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }
        return dao.actualizarTemperatura(lineaId, medicionId, temperatura)
    }

    // Delega la eliminación de una sola medición; el repositorio recibe la cantidad eliminada.
    suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long
    ): Int {
        validarIdentificadores(lineaId, medicionId)
        return dao.eliminarMedicion(lineaId, medicionId)
    }

    // Valida el intervalo del gráfico y conecta con MedicionTemperaturaDao.
    // Room vuelve a emitir los datos cuando se registran, editan o eliminan mediciones.
    fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperaturaEntity>> {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }
        require(desdeInclusive <= hastaInclusive) {
            "La fecha inicial no puede ser posterior a la fecha final."
        }
        return dao.observarMedicionesEnIntervalo(
            lineaId = lineaId,
            desdeInclusive = desdeInclusive,
            hastaInclusive = hastaInclusive
        )
    }

    // Evita enviar identificadores inválidos a las consultas de edición y eliminación.
    private fun validarIdentificadores(lineaId: Int, medicionId: Long) {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }
        require(medicionId > 0L) {
            "Selecciona una medición válida."
        }
    }
}
