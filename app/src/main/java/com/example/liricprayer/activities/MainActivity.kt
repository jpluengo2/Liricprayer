package com.example.liricprayer.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView // Asegúrate de importar androidx
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.liricprayer.R
import com.example.liricprayer.adapter.SongAdapter
import com.example.liricprayer.data.Song
import com.example.liricprayer.viewmodel.SongListViewModel
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // 1. Inicializar el ViewModel
    private val viewModel: SongListViewModel by viewModels()

    // 2. Declarar el Adapter (lateinit)
    private lateinit var songAdapter: SongAdapter

    // 3. Lista para guardar las canciones originales
    private var fullSongList: List<Song> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide() // Ocultar barra superior

        // 4. Configurar vistas en el orden correcto
        setupRecyclerView() // Primero inicializa el adapter
        setupSearchView()   // Luego configura el buscador
        observeViewModel()  // Finalmente, observa los datos

        // 5. ¡Pedir los datos!
        viewModel.loadSongs()
    }

    /**
     * Inicializa el RecyclerView y el SongAdapter.
     */
    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_songs)
        // Aquí se inicializa la variable 'lateinit'
        songAdapter = SongAdapter()
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Configura el listener del SearchView para filtrar en tiempo real.
     */
    private fun setupSearchView() {
        val searchView: SearchView = findViewById(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No hacemos nada al pulsar "Enter"
            }

            // Se llama CADA VEZ que el usuario teclea
            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText) // Llama a nuestra función de filtro
                return true
            }
        })
    }

    /**
     * Filtra la 'fullSongList' basándose en la consulta y actualiza el adapter.
     */
    private fun filterSongs(query: String?) {
        // .orEmpty() convierte 'null' en "" (vacío)
        val searchQuery = query.orEmpty().lowercase(Locale.getDefault())

        val filteredList = if (searchQuery.isEmpty()) {
            fullSongList // Si no hay búsqueda, mostrar todo
        } else {
            // Si hay búsqueda, filtrar la lista completa
            fullSongList.filter { song ->
                song.title.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        song.lyrics.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        song.liturgyTypes.any { it.lowercase(Locale.getDefault()).contains(searchQuery) }
            }
        }

        // Entregar la lista (filtrada o completa) al adapter
        songAdapter.submitList(filteredList)
    }

    /**
     * Observa los cambios en el ViewModel (carga, errores, y lista de canciones).
     */
    private fun observeViewModel() {

        // --- ¡¡ESTA ES LA LÓGICA CORREGIDA!! ---
        viewModel.songs.observe(this) { songs ->
            // 1. Guardamos la lista completa
            fullSongList = songs

            // 2. MOSTRAMOS LA LISTA COMPLETA DIRECTAMENTE.
            //    (Ya no filtramos aquí. El filtro solo se activa
            //     si el usuario escribe en el setupSearchView).
            songAdapter.submitList(fullSongList)
        }
        // --- FIN DE LA CORRECIÓN ---

        viewModel.isLoading.observe(this) { isLoading ->
            // (Aquí podemos mostrar un spinner de carga en el futuro)
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}