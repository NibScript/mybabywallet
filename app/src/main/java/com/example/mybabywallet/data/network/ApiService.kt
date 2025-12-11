package com.example.mybabywallet.data.network

import androidx.annotation.Keep
import com.example.mybabywallet.data.Transaccion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Modelos para el Dólar
@Keep
data class RespuestaIndicador(val serie: List<IndicadorDia>)
@Keep data class IndicadorDia(val fecha: String, val valor: Double)

interface ApiService {
    // API
    @GET("dolar")
    suspend fun obtenerDolar(): RespuestaIndicador

    @GET("uf")
    suspend fun obtenerUf(): RespuestaIndicador

    // MICROSERVICIO
    @POST("transacciones/sincronizar")
    suspend fun sincronizarDatos(@Body transacciones: List<Transaccion>): Response<Void>
}