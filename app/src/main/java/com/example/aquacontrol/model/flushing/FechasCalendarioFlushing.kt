package com.example.aquacontrol.model.flushing

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FechasCalendarioFlushing {

    // Estas claves representan días de calendario, no instantes.
    // UTC evita desplazamientos al navegar entre días y meses.
    private val zonaCalendario: TimeZone
        get() = TimeZone.getTimeZone("UTC")

    private fun calendario(fecha: Long): Calendar {
        return Calendar.getInstance(zonaCalendario).apply {
            timeInMillis = fecha
        }
    }

    fun hoy(): Long {
        val fechaLocal = Calendar.getInstance()

        return Calendar.getInstance(zonaCalendario).apply {
            clear()
            set(
                fechaLocal.get(Calendar.YEAR),
                fechaLocal.get(Calendar.MONTH),
                fechaLocal.get(Calendar.DAY_OF_MONTH)
            )
        }.timeInMillis
    }

    fun diaDelRegistro(fechaHora: String): Long? {
        val texto = fechaHora.trim()
        val formato = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.ROOT
        ).apply {
            timeZone = zonaCalendario
            isLenient = false
        }

        val posicion = ParsePosition(0)
        val fecha = formato.parse(texto, posicion) ?: return null

        if (posicion.index != texto.length) {
            return null
        }

        return calendario(fecha.time).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun inicioMes(fecha: Long): Long {
        return calendario(fecha).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun moverMes(mes: Long, cantidad: Int): Long {
        return calendario(inicioMes(mes)).apply {
            add(Calendar.MONTH, cantidad)
        }.timeInMillis
    }

    fun diasDelMes(mes: Long): Int {
        return calendario(mes)
            .getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun espaciosAntesDelPrimerDia(mes: Long): Int {
        val primerDia = calendario(inicioMes(mes))
            .get(Calendar.DAY_OF_WEEK)

        // La primera columna del calendario corresponde al lunes.
        return (primerDia + 5) % 7
    }

    fun fechaDelDia(mes: Long, numeroDia: Int): Long {
        require(numeroDia in 1..diasDelMes(mes)) {
            "El día debe pertenecer al mes seleccionado."
        }

        return calendario(inicioMes(mes)).apply {
            set(Calendar.DAY_OF_MONTH, numeroDia)
        }.timeInMillis
    }

    fun tituloMes(mes: Long): String {
        return formatear(mes, "MMMM yyyy")
    }

    fun tituloDia(dia: Long): String {
        return formatear(dia, "dd/MM/yyyy")
    }

    private fun formatear(fecha: Long, patron: String): String {
        return SimpleDateFormat(
            patron,
            Locale("es", "CL")
        ).apply {
            timeZone = zonaCalendario
        }.format(Date(fecha))
    }
}