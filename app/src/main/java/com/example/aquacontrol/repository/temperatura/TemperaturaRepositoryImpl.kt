package com.example.aquacontrol.repository.temperatura

import android.util.Log
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.notificaciones.NotificadorAlertas
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TemperaturaRepositoryImpl(
    private val localDataSource: TemperaturaLocalDataSource,
    private val notificadorAlertas: NotificadorAlertas,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : TemperaturaRepository {

    override suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ): MedicionTemperatura {
        val ubicacion = obtenerUbicacion(lineaId)

        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }

        return bloqueoRegistro.withLock {
            val anterior = localDataSource
                .observarUltimaMedicionPorLinea(lineaId)
                .first()
                ?.toModelo()

            val entity = MedicionTemperaturaEntity(
                lineaId = lineaId,
                temperatura = temperatura,
                fechaHora = System.currentTimeMillis(),
                origen = origen.name
            )

            val idGenerado = localDataSource.guardarMedicion(entity)

            val medicionGuardada = entity.copy(
                id = idGenerado
            ).toModelo()

            try {
                notificadorAlertas.notificarCambio(
                    medicion = medicionGuardada,
                    estadoAnterior = anterior?.estado,
                    ubicacion = ubicacion
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Un fallo al notificar no invalida la medición guardada.
                Log.e(
                    "TemperaturaRepository",
                    "La medición se guardó, pero no se pudo emitir el aviso.",
                    e
                )
            }

            medicionGuardada
        }
    }

    override fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)

        return localDataSource.observarHistorialPorLinea(lineaId)
            .map { mediciones ->
                mediciones.map { it.toModelo() }
            }
    }

    override fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)

        return localDataSource.observarPaginaHistorial(
            lineaId = lineaId,
            filtro = filtro,
            limite = limite,
            desplazamiento = desplazamiento
        ).map { mediciones ->
            mediciones.map { it.toModelo() }
        }
    }

    override fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int> {
        obtenerUbicacion(lineaId)

        return localDataSource.observarCantidadHistorial(
            lineaId = lineaId,
            filtro = filtro
        )
    }

    override fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperatura?> {
        obtenerUbicacion(lineaId)

        return localDataSource.observarUltimaMedicionPorLinea(lineaId)
            .map { medicion ->
                medicion?.toModelo()
            }
    }

    override fun observarUltimasMediciones():
            Flow<List<MedicionTemperatura>> {
        return localDataSource.observarUltimasMediciones()
            .map { mediciones ->
                mediciones.map { it.toModelo() }
            }
    }

    private fun obtenerUbicacion(lineaId: Int): String {
        require(lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        for (granja in bebederoRepository.obtenerGranjas()) {
            for (galpon in bebederoRepository.obtenerGalpones(granja.id)) {
                val linea = bebederoRepository
                    .obtenerLineas(galpon.id)
                    .firstOrNull { it.id == lineaId }

                if (linea != null) {
                    return "${granja.nombre} · ${galpon.nombre} · ${linea.nombre}"
                }
            }
        }

        throw IllegalArgumentException(
            "La línea seleccionada no existe."
        )
    }

    private fun MedicionTemperaturaEntity.toModelo():
            MedicionTemperatura {
        return MedicionTemperatura(
            id = id,
            lineaId = lineaId,
            temperatura = temperatura,
            fechaHora = fechaHora,
            origen = OrigenMedicion.valueOf(origen)
        )
    }

    companion object {
        // Evita comparar y guardar simultáneamente desde varias instancias.
        private val bloqueoRegistro = Mutex()
    }
}