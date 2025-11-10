package com.example.liricprayer.repository

import android.app.Application // Usamos Application para el contexto
import android.content.Context
import android.util.Log
import com.example.liricprayer.data.Song
import com.example.liricprayer.network.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Pedimos 'Application' en el constructor
class SongRepository(private val application: Application) {

    private val api = ApiClient.api
    private val gson = Gson()
    private val CACHE_FILE_NAME = "songs_cache.json"

    /**
     * Función principal: Intenta red, si falla, carga caché.
     */
    suspend fun getSongs(): List<Song> {
        return try {
            // 1. Intentar descargar de Dropbox
            val response = api.getSongs()
            if (response.isSuccessful && response.body() != null) {
                val songsFromNetwork = response.body()!!
                Log.d("SongRepository", "Datos descargados de red.")
                // 2. Guardar en caché
                saveSongsToCache(songsFromNetwork)
                songsFromNetwork
            } else {
                // Error de red (ej. 404)
                Log.w("SongRepository", "Error de red, cargando de caché.")
                loadSongsFromCache()
            }
        } catch (e: Exception) {
            // Sin conexión
            Log.e("SongRepository", "No hay conexión, cargando de caché.", e)
            loadSongsFromCache()
        }
    }

    /**
     * Guarda la lista en un archivo JSON interno.
     */
    private suspend fun saveSongsToCache(songs: List<Song>) {
        withContext(Dispatchers.IO) { // Hilo de fondo para I/O
            try {
                val json = gson.toJson(songs)
                application.openFileOutput(CACHE_FILE_NAME, Context.MODE_PRIVATE).use {
                    it.write(json.toByteArray())
                }
                Log.d("SongRepository", "Canciones guardadas en caché.")
            } catch (e: Exception) {
                Log.e("SongRepository", "Error al guardar en caché.", e)
            }
        }
    }

    /**
     * Carga la lista desde el archivo JSON interno.
     */
    private suspend fun loadSongsFromCache(): List<Song> {
        return withContext(Dispatchers.IO) { // Hilo de fondo para I/O
            try {
                val file = File(application.filesDir, CACHE_FILE_NAME)
                if (!file.exists()) {
                    Log.i("SongRepository", "No existe archivo de caché.")
                    return@withContext emptyList<Song>()
                }

                val json = file.readText()
                val type = object : TypeToken<List<Song>>() {}.type
                val songs: List<Song> = gson.fromJson(json, type)

                Log.d("SongRepository", "Canciones cargadas desde caché.")
                songs
            } catch (e: Exception) {
                Log.e("SongRepository", "Error al cargar de caché.", e)
                emptyList<Song>()
            }
        }
    }
}