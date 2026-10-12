package com.example.aquacontrol.repository.temperatura

import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import kotlinx.coroutines.flow.Flow

interface TemperaturaRepository {

    suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ): MedicionTemperatura

    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperatura>>

    fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperatura>>

    fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int>

    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperatura?>

    fun observarUltimasMediciones():
            Flow<List<MedicionTemperatura>>
}