package com.example.mybabywallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap // <--- IMPORTANTE: Este es el import que soluciona el rojo
import androidx.lifecycle.viewModelScope
import com.example.mybabywallet.data.AppDatabase
import com.example.mybabywallet.data.Transaccion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).transaccionDao()

    // Variable para guardar el ID del usuario actual
    private val _usuarioId = MutableLiveData<Int>()

    // --- CORRECCIÓN AQUÍ ---
    // Usamos .switchMap directamente (forma moderna de Kotlin)
    val listaTransacciones: LiveData<List<Transaccion>> = _usuarioId.switchMap { id ->
        dao.obtenerPorUsuario(id)
    }

    val totalIngresos: LiveData<Double> = _usuarioId.switchMap { id ->
        dao.totalIngresos(id)
    }

    val totalGastos: LiveData<Double> = _usuarioId.switchMap { id ->
        dao.totalGastos(id)
    }
    // -----------------------

    // Función para "Loguear" al usuario en el ViewModel
    fun setUsuarioActual(id: Int) {
        _usuarioId.value = id
    }

    fun agregarTransaccion(titulo: String, montoStr: String, esIngreso: Boolean, imagenPath: String, lat: Double, long: Double) {
        val currentUserId = _usuarioId.value ?: return

        if (titulo.isBlank() || montoStr.isBlank()) return
        val monto = montoStr.toDoubleOrNull() ?: 0.0
        if (monto <= 0) return

        val nuevaTransaccion = Transaccion(
            titulo = titulo,
            monto = monto,
            tipo = if (esIngreso) "INGRESO" else "GASTO",
            fecha = System.currentTimeMillis(),
            imagenPath = imagenPath,
            latitud = lat,
            longitud = long,
            usuarioId = currentUserId
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