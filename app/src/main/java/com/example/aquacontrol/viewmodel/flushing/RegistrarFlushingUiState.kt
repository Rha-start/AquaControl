package com.example.aquacontrol.viewmodel.flushing

import com.example.aquacontrol.model.flushing.EventoFlushing

data class RegistrarFlushingUiState(
    val lineaId: Int? = null,
    val observacion: String = "",
    val temperaturaAntes: String = "",
    val temperaturaDespues: String = "",
    val duracionSegundos: String = "",
    val fotoRuta: String? = null,
    val errorFoto: String? = null,

    val errorLinea: String? = null,
    val errorObservacion: String? = null,
    val errorTemperaturaAntes: String? = null,
    val errorTemperaturaDespues: String? = null,
    val errorDuracion: String? = null,

    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val eventoGuardado: EventoFlushing? = null
)