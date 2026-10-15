// Guarda en Room, convierte entidades y solicita el aviso tras persistir la medición.
// Conecta con: data/temperatura/local/TemperaturaLocalDataSource.kt (desde com/example/aquacontrol).
// Conecta con: notificaciones/NotificadorAlertas.kt (desde com/example/aquacontrol).
// Conecta con: repository/bebedero/BebederoRepository.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.repository.temperatura

import android.util.Log
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import com.example.aquacontrol.model.perfil.PerfilUsuario
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

    // Registra una lectura con su línea, temperatura y origen.
    override suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ): MedicionTemperatura {
        val ubicacion = obtenerUbicacion(lineaId)

        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }

        // Serializa lectura anterior, guardado y aviso para evitar comparaciones simultáneas.
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
                // Propaga la cancelación de la corrutina; no la presenta como un fallo de datos.
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

    // Emite el historial de la línea y sus cambios posteriores.
    override fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)

        return localDataSource.observarHistorialPorLinea(lineaId)
            .map { mediciones ->
                mediciones.map { it.toModelo() }
            }
    }

    // Emite solo la página solicitada, respetando filtros de estado y fecha.
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

    // Emite el total filtrado para calcular los límites de paginación.
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

    // Emite la lectura más reciente de la línea o null si aún no hay datos.
    override fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperatura?> {
        obtenerUbicacion(lineaId)

        return localDataSource.observarUltimaMedicionPorLinea(lineaId)
            .map { medicion ->
                medicion?.toModelo()
            }
    }

    // Emite una última lectura por línea para actualizar resúmenes y alertas.
    override fun observarUltimasMediciones():
            Flow<List<MedicionTemperatura>> {
        return localDataSource.observarUltimasMediciones()
            .map { mediciones ->
                mediciones.map { it.toModelo() }
            }
    }

    // Consulta el registro exacto para cargar el detalle o el formulario de edición.
    override suspend fun obtenerMedicion(
        lineaId: Int,
        medicionId: Long
    ): MedicionTemperatura? {
        obtenerUbicacion(lineaId)
        return localDataSource.obtenerMedicion(lineaId, medicionId)?.toModelo()
    }

    // Solo Operario puede corregir temperaturas. No crea otra lectura ni cambia su fecha u origen.
    override suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double,
        perfil: PerfilUsuario
    ): MedicionTemperatura {
        require(perfil == PerfilUsuario.OPERARIO) {
            "Solo el operario puede editar temperaturas."
        }
        obtenerUbicacion(lineaId)
        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }

        // Usa el mismo bloqueo del registro para no intercalar cambios con la simulación.
        return bloqueoRegistro.withLock {
            val existente = requireNotNull(
                localDataSource.obtenerMedicion(lineaId, medicionId)
            ) {
                "La medición ya no existe en esta línea. Actualiza el historial."
            }

            val actualizadas = localDataSource.actualizarTemperatura(
                lineaId = lineaId,
                medicionId = medicionId,
                temperatura = temperatura
            )
            check(actualizadas == 1) {
                "No se pudo actualizar la medición. Actualiza el historial."
            }

            // Retira el aviso original solo si su temperatura realmente cambió.
            // La corrección actualiza los indicadores mediante Room, sin emitir otra alarma.
            if (existente.temperatura != temperatura) {
                notificadorAlertas.retirarAvisoDeMedicion(lineaId, medicionId)
            }

            // Room emite los cambios a historiales y resúmenes; no se registra una lectura nueva.
            existente.copy(temperatura = temperatura).toModelo()
        }
    }

    // Solo Supervisor puede eliminar; el ID de línea impide afectar otro historial.
    override suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long,
        perfil: PerfilUsuario
    ) {
        require(perfil == PerfilUsuario.SUPERVISOR) {
            "Solo el supervisor puede eliminar temperaturas."
        }
        obtenerUbicacion(lineaId)

        bloqueoRegistro.withLock {
            val eliminadas = localDataSource.eliminarMedicion(lineaId, medicionId)
            check(eliminadas == 1) {
                "La medición ya no existe en esta línea. Actualiza el historial."
            }
            // Evita que quede un aviso cuyo destino acaba de eliminarse de Room.
            notificadorAlertas.retirarAvisoDeMedicion(lineaId, medicionId)
        }
        // Los Flow de Room recalculan el historial y la última medición disponible.
    }

    // Valida la línea y convierte las entidades de Room para el gráfico del supervisor.
    // Conserva el orden cronológico recibido de TemperaturaLocalDataSource.
    override fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)
        return localDataSource.observarMedicionesEnIntervalo(
            lineaId = lineaId,
            desdeInclusive = desdeInclusive,
            hastaInclusive = hastaInclusive
        ).map { mediciones ->
            mediciones.map { it.toModelo() }
        }
    }

    // Resuelve el nombre completo de la ubicación o rechaza una línea inexistente.
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

    // Convierte la entidad Room a una medición del dominio, recuperando su origen.
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