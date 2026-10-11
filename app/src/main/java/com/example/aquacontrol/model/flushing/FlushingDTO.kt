package com.example.aquacontrol.model.flushing

data class FlushingDTO(
    val fechaHora: String,
    val lineaId: Int,
    val observacion: String,
    val temperaturaAntes: Double,
    val temperaturaDespues: Double,
    val duracionSegundos: Int?,
    val fotoRuta: String? = null
)