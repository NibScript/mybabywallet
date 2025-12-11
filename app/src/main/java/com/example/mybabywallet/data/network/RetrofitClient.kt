package com.example.mybabywallet.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL_EXTERNA = "https://mindicador.cl/api/"

    // Emulador "10.0.2.2"
    private const val BASE_URL_SPRING = "http://172.20.10.10:8080/api/"

    val apiExterna: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_EXTERNA)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val apiSpring: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_SPRING)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}