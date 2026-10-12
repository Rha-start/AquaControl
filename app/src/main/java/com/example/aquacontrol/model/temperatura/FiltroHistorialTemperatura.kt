package com.example.aquacontrol.model.temperatura

import com.example.aquacontrol.model.estado.EstadoLinea

data class FiltroHistorialTemperatura(
    // null permite consultar todos los estados.
    val estado: EstadoLinea? = null,

    // Inicio del primer día seleccionado, en milisegundos.
    val desdeInclusive: Long? = null,

    // Inicio del día siguiente al último día seleccionado.
    // Así se incluye completo el último día del filtro.
    val hastaExclusivo: Long? = null
) {
    val tieneFiltros: Boolean
        get() = estado != null ||
                desdeInclusive != null ||
                hastaExclusivo != null

    val rangoValido: Boolean
        get() {
            val desde = desdeInclusive
            val hasta = hastaExclusivo

            return desde == null || hasta == null || desde < hasta
        }
}

