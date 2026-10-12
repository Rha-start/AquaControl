package com.example.aquacontrol.viewmodel.historial

import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura

data class HistorialTemperaturaUiState(
    val lineaId: Int? = null,
    val ubicacion: String = "",
    val filtro: FiltroHistorialTemperatura =
        FiltroHistorialTemperatura(),
    val mediciones: List<MedicionTemperatura> = emptyList(),
    val paginaActual: Int = 0,
    val totalResultados: Int = 0,
    val cargando: Boolean = false,
    val error: String? = null,
    val errorFiltros: String? = null
) {
    val totalPaginas: Int
        get() = if (totalResultados <= 0) {
            0
        } else {
            ((totalResultados - 1) / TAMANIO_PAGINA) + 1
        }

    val puedeIrAnterior: Boolean
        get() = !cargando &&
                error == null &&
                paginaActual > 0

    val puedeIrSiguiente: Boolean
        get() = !cargando &&
                error == null &&
                paginaActual + 1 < totalPaginas

    val primerResultadoVisible: Int
        get() = if (mediciones.isEmpty()) {
            0
        } else {
            paginaActual * TAMANIO_PAGINA + 1
        }

    val ultimoResultadoVisible: Int
        get() = if (mediciones.isEmpty()) {
            0
        } else {
            paginaActual * TAMANIO_PAGINA + mediciones.size
        }

    companion object {
        const val TAMANIO_PAGINA = 30
    }
}

