package com.example.aquacontrol.data.flushing

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID

object FotosFlushing {

    private const val CARPETA = "evidencias_flushing"

    fun crearArchivo(context: Context): File {
        val carpeta = File(context.filesDir, CARPETA)

        if (!carpeta.isDirectory && !carpeta.mkdirs()) {
            throw IOException(
                "No se pudo crear la carpeta de evidencias."
            )
        }

        val archivo = File(
            carpeta,
            "flushing_${UUID.randomUUID()}.jpg"
        )

        if (!archivo.createNewFile()) {
            throw IOException(
                "No se pudo preparar el archivo de la foto."
            )
        }

        return archivo
    }

    fun obtenerUri(context: Context, nombreArchivo: String): Uri {
        val archivo = requireNotNull(
            obtenerArchivo(context, nombreArchivo)
        ) {
            "La ubicación de la foto no es válida."
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
    }

    fun obtenerArchivo(
        context: Context,
        nombreArchivo: String?
    ): File? {
        if (nombreArchivo.isNullOrBlank()) return null

        // Solo se aceptan nombres dentro de la carpeta de evidencias.
        if (
            nombreArchivo.contains('/') ||
            nombreArchivo.contains('\\') ||
            nombreArchivo == "." ||
            nombreArchivo == ".."
        ) {
            return null
        }

        return try {
            val carpeta = File(
                context.filesDir,
                CARPETA
            ).canonicalFile

            val archivo = File(
                carpeta,
                nombreArchivo
            ).canonicalFile

            archivo.takeIf {
                it.parentFile == carpeta && it.isFile
            }
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    fun esFotoValida(
        context: Context,
        nombreArchivo: String?
    ): Boolean {
        val archivo = obtenerArchivo(
            context,
            nombreArchivo
        ) ?: return false

        if (archivo.length() == 0L) return false

        return try {
            val opciones = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(
                archivo.absolutePath,
                opciones
            )

            opciones.outWidth > 0 && opciones.outHeight > 0
        } catch (e: Exception) {
            false
        }
    }

    fun eliminarFotoNoGuardada(
        context: Context,
        nombreArchivo: String?
    ): Boolean {
        val archivo = obtenerArchivo(
            context,
            nombreArchivo
        ) ?: return true

        return try {
            archivo.delete()
        } catch (e: SecurityException) {
            false
        }
    }
}
