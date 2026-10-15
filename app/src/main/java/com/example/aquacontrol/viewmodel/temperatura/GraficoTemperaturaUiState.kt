package com.example.aquacontrol.viewmodel.temperatura

import com.example.aquacontrol.model.temperatura.MedicionTemperatura

// Estado que publicará GraficoTemperaturaViewModel para dibujar
// las mediciones guardadas durante las últimas 24 horas.
data class GraficoTemperaturaUiState(
    val cargando: Boolean = true,

    // Límites de tiempo del gráfico, expresados en milisegundos.
    val desdeInclusive: Long = 0L,
    val hastaInclusive: Long = 0L,

    // Mediciones ordenadas cronológicamente; una lista vacía no es un error.
    val mediciones: List<MedicionTemperatura> = emptyList(),

    // Mensaje que permite mostrar un fallo de consulta y ofrecer reintento.
    val error: String? = null
)