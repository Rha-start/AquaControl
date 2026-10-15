package com.example.aquacontrol.viewmodel.flushing

import com.example.aquacontrol.model.flushing.EventoFlushing

// Estado que publicará DetalleFlushingViewModel para consultar,
// editar o eliminar un evento específico.
data class DetalleFlushingUiState(
    val cargando: Boolean = true,

    // Registro persistido; conserva su fecha y fotografía durante la edición.
    val evento: EventoFlushing? = null,
    val editando: Boolean = false,

    // Borrador del formulario: escribir aquí todavía no modifica Room.
    val observacion: String = "",
    val temperaturaAntes: String = "",
    val temperaturaDespues: String = "",
    val duracionSegundos: String = "",

    // Errores individuales para indicar qué campo necesita corrección.
    val errorObservacion: String? = null,
    val errorTemperaturaAntes: String? = null,
    val errorTemperaturaDespues: String? = null,
    val errorDuracion: String? = null,

    // Distingue errores de carga de los ocurridos al guardar o eliminar.
    val errorCarga: String? = null,
    val errorOperacion: String? = null,

    // Bloquea acciones repetidas mientras se procesa una operación.
    val guardando: Boolean = false,
    val eliminando: Boolean = false,

    // Comunica el resultado a la pantalla.
    val eliminado: Boolean = false,
    val mensaje: String? = null
) {
    val ocupado: Boolean
        get() = cargando || guardando || eliminando

    // El ViewModel comprobará además los permisos del perfil seleccionado.
    val puedeInteractuar: Boolean
        get() = evento != null && !ocupado && !eliminado
}