package com.example.miappdenotas.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas_table")
data class Nota(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val titulo: String,
    val contenido: String,
    val fecha: Long = System.currentTimeMillis(),
    val favorita: Boolean = false,
    val eliminada: Boolean = false,
    val fechaModificacion: Long = System.currentTimeMillis(),
    val userId: String = ""

    )