package com.example.mybabywallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybabywallet.data.AppDatabase
import com.example.mybabywallet.data.Transaccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).transaccionDao()

    // Datos listos para ser consumidos por la pantalla
    val listaTransacciones = dao.obtenerTodas()
    val totalIngresos = dao.totalIngresos()
    val totalGastos = dao.totalGastos()

    fun agregarTransaccion(titulo: String, montoStr: String, esIngreso: Boolean) {
        if (titulo.isBlank() || montoStr.isBlank()) return // Validación básica

        val monto = montoStr.toDoubleOrNull() ?: 0.0
        if (monto <= 0) return

        val nuevaTransaccion = Transaccion(
            titulo = titulo,
            monto = monto,
            tipo = if (esIngreso) "INGRESO" else "GASTO",
            fecha = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertar(nuevaTransaccion)
        }
    }

    fun eliminarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.borrar(transaccion)
        }
    }
}