package com.example.mybabywallet.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "tabla_transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val monto: Double,
    val tipo: String,
    val fecha: Long,
    val imagenPath: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val usuarioId: Int // <--- NUEVO: La etiqueta del dueño
)