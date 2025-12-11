package com.example.mybabywallet.data.network

import androidx.annotation.Keep
import com.example.mybabywallet.data.Transaccion
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Modelos para el Dólar (API Externa) - YA LO TIENES
@Keep
data class RespuestaIndicador(val serie: List<IndicadorDia>)
@Keep data class IndicadorDia(val fecha: String, val valor: Double)

interface ApiService {
    // 1. API Externa (Dólar)
    @GET("dolar")
    suspend fun obtenerDolar(): RespuestaIndicador

    @GET("uf")
    suspend fun obtenerUf(): RespuestaIndicador

    // 2. MICROSERVICIO SPRING BOOT (Nuevo)
    // Asumimos que tu Spring Boot tiene un endpoint: POST /api/transacciones/sincronizar
    @POST("transacciones/sincronizar")
    suspend fun sincronizarDatos(@Body transacciones: List<Transaccion>): Response<Void>
}