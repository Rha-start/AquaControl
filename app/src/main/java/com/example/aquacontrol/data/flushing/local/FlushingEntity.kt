package com.example.aquacontrol.data.flushing.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flushing")
data class FlushingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val lineaId: Int,
    val fechaHora: String,
    val observacion: String,
    val temperaturaAntes: Double,
    val temperaturaDespues: Double,
    val duracionSegundos: Int,
    val fotoRuta: String? = null
)