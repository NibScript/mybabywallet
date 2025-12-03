package com.example.mybabywallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_transacciones")
data class Transaccion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val monto: Double,
    val tipo: String, // "INGRESO" o "GASTO"
    val fecha: Long,  // Guardamos fecha en milisegundos para ordenar fácil
    val latitud: Double = 0.0,
    val imagenPath: String = ""
)