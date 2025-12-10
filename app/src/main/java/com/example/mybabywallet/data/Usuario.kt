package com.example.mybabywallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val username: String, // El correo o nombre de usuario
    val password: String  // En un caso real esto iría encriptado
)