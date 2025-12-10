package com.example.mybabywallet.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransaccionDao {
    // 1. Filtrar lista por usuario
    @Query("SELECT * FROM tabla_transacciones WHERE usuarioId = :userId ORDER BY fecha DESC")
    fun obtenerPorUsuario(userId: Int): LiveData<List<Transaccion>>

    @Insert
    suspend fun insertar(transaccion: Transaccion)

    @Delete
    suspend fun borrar(transaccion: Transaccion)

    // 2. Filtrar sumas por usuario
    @Query("SELECT COALESCE(SUM(monto), 0) FROM tabla_transacciones WHERE tipo = 'INGRESO' AND usuarioId = :userId")
    fun totalIngresos(userId: Int): LiveData<Double>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM tabla_transacciones WHERE tipo = 'GASTO' AND usuarioId = :userId")
    fun totalGastos(userId: Int): LiveData<Double>
}