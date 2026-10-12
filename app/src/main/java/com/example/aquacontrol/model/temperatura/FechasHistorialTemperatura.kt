package com.example.aquacontrol.model.temperatura

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FechasHistorialTemperatura {

    private val zonaSelector: TimeZone
        get() = TimeZone.getTimeZone("UTC")

    // El selector de Material representa los días en UTC.
    // Para consultar Room usamos el inicio del día en la zona del celular.
    fun inicioLocalDesdeSelector(fechaSelector: Long): Long {
        val seleccion = Calendar.getInstance(zonaSelector).apply {
            timeInMillis = fechaSelector
        }

        return Calendar.getInstance().apply {
            clear()
            set(
                seleccion.get(Calendar.YEAR),
                seleccion.get(Calendar.MONTH),
                seleccion.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
        }.timeInMillis
    }

    fun finLocalExclusivoDesdeSelector(fechaSelector: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = inicioLocalDesdeSelector(fechaSelector)

            // Avanza un día de calendario, no una cantidad fija de horas.
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
    }

    fun selectorDesdeInicioLocal(inicioLocal: Long): Long {
        val fechaLocal = Calendar.getInstance().apply {
            timeInMillis = inicioLocal
        }

        return selectorDesdeCalendarioLocal(fechaLocal)
    }

    fun selectorDesdeFinLocalExclusivo(finExclusivo: Long): Long {
        val fechaLocal = Calendar.getInstance().apply {
            timeInMillis = finExclusivo

            // Recupera el último día incluido en el filtro.
            add(Calendar.DAY_OF_MONTH, -1)
        }

        return selectorDesdeCalendarioLocal(fechaLocal)
    }

    fun formatearFechaSelector(fechaSelector: Long): String {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).apply {
            timeZone = zonaSelector
        }

        return formato.format(Date(fechaSelector))
    }

    fun formatearFechaHora(fechaHora: Long): String {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        )

        return formato.format(Date(fechaHora))
    }

    private fun selectorDesdeCalendarioLocal(
        fechaLocal: Calendar
    ): Long {
        return Calendar.getInstance(zonaSelector).apply {
            clear()
            set(
                fechaLocal.get(Calendar.YEAR),
                fechaLocal.get(Calendar.MONTH),
                fechaLocal.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
        }.timeInMillis
    }
}