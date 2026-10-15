package com.example.aquacontrol.viewmodel.temperatura

import com.example.aquacontrol.model.temperatura.MedicionTemperatura

// Estado que publicará DetalleTemperaturaViewModel para consultar,
// editar o eliminar una medición concreta.
data class DetalleTemperaturaUiState(
    val cargando: Boolean = true,

    // Registro persistido y texto editable: escribir no modifica Room.
    val medicion: MedicionTemperatura? = null,
    val editando: Boolean = false,
    val temperatura: String = "",

    // Separa los errores de carga, validación y operaciones.
    val errorCarga: String? = null,
    val errorTemperatura: String? = null,
    val errorOperacion: String? = null,

    // Permite bloquear los botones mientras se procesa una operación.
    val guardando: Boolean = false,
    val eliminando: Boolean = false,

    // Comunica a la pantalla el resultado de la acción.
    val eliminado: Boolean = false,
    val mensaje: String? = null
) {
    // Evita iniciar acciones durante la carga, guardado o eliminación.
    val ocupado: Boolean
        get() = cargando || guardando || eliminando

    // Los permisos de cada rol se comprobarán además en el ViewModel.
    val puedeInteractuar: Boolean
        get() = medicion != null && !ocupado && !eliminado
}