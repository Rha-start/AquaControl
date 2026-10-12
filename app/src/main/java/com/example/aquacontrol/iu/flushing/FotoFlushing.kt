package com.example.aquacontrol.iu.flushing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.data.flushing.FotosFlushing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private data class EstadoFoto(
    val cargando: Boolean = true,
    val bitmap: Bitmap? = null
)

@Composable
fun FotoFlushing(
    fotoRuta: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext

    val estado by produceState(
        initialValue = EstadoFoto(),
        key1 = fotoRuta,
        key2 = context
    ) {
        value = EstadoFoto()

        val bitmap = withContext(Dispatchers.IO) {
            val archivo = FotosFlushing.obtenerArchivo(
                context,
                fotoRuta
            )

            archivo?.let { cargarFotoReducida(it) }
        }

        value = EstadoFoto(
            cargando = false,
            bitmap = bitmap
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = estado.bitmap

        when {
            estado.cargando -> {
                CircularProgressIndicator()
            }

            bitmap != null -> {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de evidencia del flushing",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize()
                )
            }

            else -> {
                Text(
                    text = "No se pudo cargar la foto de evidencia.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private fun cargarFotoReducida(archivo: File): Bitmap? {
    return try {
        val dimensiones = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(
            archivo.absolutePath,
            dimensiones
        )

        if (
            dimensiones.outWidth <= 0 ||
            dimensiones.outHeight <= 0
        ) {
            return null
        }

        var reduccion = 1

        while (
            dimensiones.outWidth / reduccion > 1200 ||
            dimensiones.outHeight / reduccion > 1200
        ) {
            reduccion *= 2
        }

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = reduccion
        }

        val original = BitmapFactory.decodeFile(
            archivo.absolutePath,
            opciones
        ) ?: return null

        val orientacion = try {
            ExifInterface(archivo.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val matriz = Matrix()

        when (orientacion) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                matriz.setScale(-1f, 1f)

            ExifInterface.ORIENTATION_ROTATE_180 ->
                matriz.setRotate(180f)

            ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                matriz.setScale(1f, -1f)

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matriz.setRotate(90f)
                matriz.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 ->
                matriz.setRotate(90f)

            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matriz.setRotate(-90f)
                matriz.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 ->
                matriz.setRotate(-90f)
        }

        if (matriz.isIdentity) {
            original
        } else {
            val corregida = Bitmap.createBitmap(
                original,
                0,
                0,
                original.width,
                original.height,
                matriz,
                true
            )

            if (corregida !== original) {
                original.recycle()
            }
            corregida
        }
    } catch (e: Exception) {
        null
    }
}
