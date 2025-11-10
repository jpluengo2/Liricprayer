package com.example.liricprayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.liricprayer.data.Song
import com.example.liricprayer.repository.SongRepository
import kotlinx.coroutines.launch

// Heredamos de AndroidViewModel para tener acceso a 'Application'
class SongListViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia del Repositorio
    private val songRepository: SongRepository = SongRepository(application)

    // LiveData privado y mutable (solo para este ViewModel)
    private val _songs = MutableLiveData<List<Song>>()
    // LiveData público e inmutable (para que la UI lo observe)
    val songs: LiveData<List<Song>> = _songs

    // LiveData para estados de Carga y Error (útil para la UI)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Inicia la carga de canciones.
     */
    fun loadSongs() {
        //viewModelScope se encarga de cancelar esto si la app se cierra
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = songRepository.getSongs() // Magia aquí
                _songs.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al cargar canciones: ${e.message}"
            }
        }
    }
}