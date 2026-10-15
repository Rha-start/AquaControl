package com.example.aquacontrol.repository.flushing

import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.model.perfil.PerfilUsuario

// Contrato utilizado por los ViewModels.
// FlushingRepositoryImpl conecta estas operaciones con la persistencia Room.
interface FlushingRepository {

    // Registra un flushing nuevo con los datos del formulario.
    suspend fun registrarFlushing(
        dto: FlushingDTO
    ): EventoFlushing

    // Recupera el historial de una línea.
    suspend fun obtenerFlushingPorLinea(
        lineaId: Int
    ): List<EventoFlushing>

    // Busca un evento específico; null indica que ya no existe en esa línea.
    suspend fun obtenerEvento(
        lineaId: Int,
        eventoId: Int
    ): EventoFlushing?

    // Corrige los campos editables conservando fecha, línea y fotografía.
    // La implementación debe comprobar que el perfil sea OPERARIO.
    suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int,
        perfil: PerfilUsuario
    ): EventoFlushing

    // Elimina el registro únicamente con perfil SUPERVISOR.
    // Devuelve el evento eliminado para gestionar después su fotografía.
    suspend fun eliminarFlushing(
        lineaId: Int,
        eventoId: Int,
        perfil: PerfilUsuario
    ): EventoFlushing
}