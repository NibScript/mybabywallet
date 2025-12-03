package com.example.mybabywallet.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransaccionDao {
    @Query("SELECT * FROM tabla_transacciones ORDER BY fecha DESC")
    fun obtenerTodas(): LiveData<List<Transaccion>>

    @Insert
    suspend fun insertar(transaccion: Transaccion)

    @Delete
    suspend fun borrar(transaccion: Transaccion)

    // CORRECCIÓN AQUÍ: Usamos COALESCE para que devuelva 0.0 en vez de NULL si está vacía
    @Query("SELECT COALESCE(SUM(monto), 0) FROM tabla_transacciones WHERE tipo = 'INGRESO'")
    fun totalIngresos(): LiveData<Double>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM tabla_transacciones WHERE tipo = 'GASTO'")
    fun totalGastos(): LiveData<Double>
}