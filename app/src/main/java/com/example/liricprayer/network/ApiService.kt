package com.example.liricprayer.network

import com.example.liricprayer.data.Song // Importa nuestro modelo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Apuntamos a la ruta del archivo y pasamos la clave y 'dl=1' como parámetros
    @GET("scl/fi/kkdo6hhmvwg0plalejn1r/Liricprayer.txt")
    suspend fun getSongs(
        @Query("rlkey") rlkey: String = "qno1t32084y9oe6jzvxrd5kqb",
        @Query("dl") dl: String = "1"
    ): Response<List<Song>> // Esperamos recibir una Lista de Canciones
}