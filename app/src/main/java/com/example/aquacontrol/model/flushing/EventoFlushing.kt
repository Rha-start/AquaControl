package com.example.aquacontrol.model.flushing

data class EventoFlushing(
    val id: Int,
    val lineaId: Int,
    val fechaHora: String,
    val temperaturaAntes: Double,
    val temperaturaDespues: Double,
    val duracionSegundos: Int,
    val observacion: String = "",
    val fotoRuta: String? = null
)