package com.example.liricprayer.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 'object' crea una instancia única (Singleton)
object ApiClient {

    private const val BASE_URL = "https://www.dropbox.com/"

    // Instancia de Retrofit (perezosa, solo se crea una vez)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Le decimos que use Gson para convertir el JSON a nuestras clases
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instancia pública de nuestro servicio de API
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}