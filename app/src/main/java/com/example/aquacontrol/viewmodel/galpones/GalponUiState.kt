package com.example.aquacontrol.viewmodel.galpones

import com.example.aquacontrol.model.galpon.Galpon

sealed class GalponUiState {

    object Loading : GalponUiState()

    data class Success(
        val galpones: List<Galpon>,
        val resumenesPorGalpon: Map<Int, ResumenEstadoGalpon> = emptyMap()
    ) : GalponUiState()

    object Empty : GalponUiState()

    data class Error(
        val mensaje: String
    ) : GalponUiState()
}

enum class EstadoResumenGalpon {
    ESTABLE,
    ADVERTENCIA,
    CRITICO,
    DATOS_INCOMPLETOS,
    SIN_MEDICIONES,
    SIN_LINEAS
}

data class ResumenEstadoGalpon(
    val lineasNormales: Int,
    val lineasEnAdvertencia: Int,
    val lineasCriticas: Int,
    val lineasSinMediciones: Int
) {
    val totalLineas: Int
        get() = lineasNormales +
                lineasEnAdvertencia +
                lineasCriticas +
                lineasSinMediciones

    val estado: EstadoResumenGalpon
        get() = when {
            totalLineas == 0 ->
                EstadoResumenGalpon.SIN_LINEAS

            lineasCriticas > 0 ->
                EstadoResumenGalpon.CRITICO

            lineasEnAdvertencia > 0 ->
                EstadoResumenGalpon.ADVERTENCIA

            lineasSinMediciones == totalLineas ->
                EstadoResumenGalpon.SIN_MEDICIONES

            lineasSinMediciones > 0 ->
                EstadoResumenGalpon.DATOS_INCOMPLETOS

            else ->
                EstadoResumenGalpon.ESTABLE
        }
}