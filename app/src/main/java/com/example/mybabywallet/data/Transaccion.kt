package com.example.mybabywallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val monto: Double,
    val tipo: String,
    val fecha: Long,
    val imagenPath: String = "", // La foto que ya funciona
    val latitud: Double = 0.0,   // GPS 1
    val longitud: Double = 0.0   // GPS 2 (Nuevo)
)