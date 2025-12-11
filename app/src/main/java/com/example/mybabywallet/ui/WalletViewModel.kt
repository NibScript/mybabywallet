package com.example.mybabywallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.mybabywallet.data.AppDatabase
import com.example.mybabywallet.data.Transaccion
import com.example.mybabywallet.data.network.RetrofitClient // Asegúrate de que este import exista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).transaccionDao()

    // --- LÓGICA DE USUARIOS (MULTI-USUARIO) ---
    private val _usuarioId = MutableLiveData<Int>()

    // Las listas se actualizan automáticamente según el usuario logueado
    val listaTransacciones: LiveData<List<Transaccion>> = _usuarioId.switchMap { id ->
        dao.obtenerPorUsuario(id)
    }

    val totalIngresos: LiveData<Double> = _usuarioId.switchMap { id ->
        dao.totalIngresos(id)
    }

    val totalGastos: LiveData<Double> = _usuarioId.switchMap { id ->
        dao.totalGastos(id)
    }

    fun setUsuarioActual(id: Int) {
        _usuarioId.value = id
    }

    // --- CRUD BASE DE DATOS LOCAL (ROOM) ---
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

    // --- API EXTERNA: CONVERSOR DE MONEDA ---
    val resultadoConversion = MutableLiveData<String>()
    val cargandoConversion = MutableLiveData<Boolean>(false)

    fun convertirMoneda(montoPesos: Double, monedaDestino: String) {
        cargandoConversion.postValue(true)
        resultadoConversion.postValue("Calculando...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Llamamos a Retrofit para obtener el valor del día
                val respuesta = if (monedaDestino == "DOLAR") {
                    RetrofitClient.apiExterna.obtenerDolar()
                } else {
                    RetrofitClient.apiExterna.obtenerUf()
                }

                val valorMoneda = respuesta.serie.firstOrNull()?.valor ?: 0.0

                // 2. Calculamos
                if (valorMoneda > 0) {
                    val total = montoPesos / valorMoneda
                    val simbolo = if (monedaDestino == "DOLAR") "USD" else "UF"
                    val textoFinal = "${String.format("%.2f", total)} $simbolo\n(Tasa: $valorMoneda)"

                    resultadoConversion.postValue(textoFinal)
                } else {
                    resultadoConversion.postValue("Error en datos de API")
                }

            } catch (e: Exception) {
                resultadoConversion.postValue("Error de conexión o API")
            } finally {
                cargandoConversion.postValue(false)
            }
        }
    }

    fun limpiarConversor() {
        resultadoConversion.value = ""
    }

    // --- MICROSERVICIO PROPIO: SINCRONIZACIÓN SPRING BOOT ---
    val estadoSincronizacion = MutableLiveData<String>()

    fun sincronizarConNube() {
        val transaccionesLocales = listaTransacciones.value ?: emptyList()

        if (transaccionesLocales.isEmpty()) {
            estadoSincronizacion.value = "No hay datos para enviar"
            return
        }

        estadoSincronizacion.value = "Enviando a Spring Boot..."

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Enviamos la lista al backend
                val respuesta = RetrofitClient.apiSpring.sincronizarDatos(transaccionesLocales)

                if (respuesta.isSuccessful) {
                    estadoSincronizacion.postValue("¡Sincronización Exitosa! ☁️")
                } else {
                    estadoSincronizacion.postValue("Error Servidor: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                // Si el servidor local está apagado, caerá aquí.
                // Esto es suficiente para demostrar el intento de conexión.
                estadoSincronizacion.postValue("Fallo de conexión con Servidor Local (¿Está encendido?)")
            }
        }
    }

    fun limpiarMensajeSync() {
        estadoSincronizacion.value = ""
    }
}