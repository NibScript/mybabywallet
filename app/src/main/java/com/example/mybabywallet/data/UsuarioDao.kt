package com.example.mybabywallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    // Busca si existe alguien con ese usuario y contraseña
    @Query("SELECT * FROM tabla_usuarios WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun login(user: String, pass: String): Usuario?

    // Para validar que no se repita el usuario al registrarse
    @Query("SELECT * FROM tabla_usuarios WHERE username = :user LIMIT 1")
    suspend fun checkUsuarioExiste(user: String): Usuario?
}