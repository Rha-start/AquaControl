package com.example.aquacontrol.data.temperatura.local

import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import kotlinx.coroutines.flow.Flow

class TemperaturaLocalDataSource(
    private val dao: MedicionTemperaturaDao
) {

    suspend fun guardarMedicion(
        medicion: MedicionTemperaturaEntity
    ): Long {
        return dao.insertarMedicion(medicion)
    }

    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarHistorialPorLinea(lineaId)
    }

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

    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperaturaEntity?> {
        return dao.observarUltimaMedicionPorLinea(lineaId)
    }

    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarUltimasMediciones()
    }
}