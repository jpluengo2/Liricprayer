package com.example.liricprayer.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.liricprayer.R
import com.example.liricprayer.data.Song
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SongDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        // 1. Configurar la Toolbar (barra superior)
        val toolbar: Toolbar = findViewById(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Activa el botón "Atrás"

        // 2. Encontrar las Vistas
        val lyricsText: TextView = findViewById(R.id.lyrics_text)
        val fabPlay: FloatingActionButton = findViewById(R.id.fab_play)
        val liturgyText: TextView = findViewById(R.id.detail_liturgy_text)
        // 3. Recibir el objeto 'Song' que enviamos
        val song = getSongFromIntent()

        if (song == null) {
            // Si algo falla, cerramos la pantalla
            Toast.makeText(this, "Error al cargar la canción", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 4. Rellenar los datos
        supportActionBar?.title = song.title // Pone el título en la barra
        lyricsText.text = song.lyrics // Pone la letra completa

        // Unimos la lista de liturgias con una coma (como en la lista)
        liturgyText.text = song.liturgyTypes.joinToString(", ")

        // 5. Lógica del botón Play
        if (song.audioUrl.isNotBlank()) {
            // Si la URL NO está vacía
            fabPlay.isEnabled = true
            fabPlay.alpha = 1.0f // Opacidad normal

            fabPlay.setOnClickListener {
                // Abrir el reproductor externo (Opción 1 que elegimos)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(song.audioUrl)
                startActivity(intent)
            }
        } else {
            // Si la URL está vacía
            fabPlay.isEnabled = false
            fabPlay.alpha = 0.3f // Hacemos que se vea "apagado"
        }
    }

    // Función para gestionar el clic en el botón "Atrás" de la barra
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Función segura para recibir el objeto Song (compatible con varias versiones de Android)
    private fun getSongFromIntent(): Song? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("SONG_DATA", Song::class.java)
        } else {
            // Versiones antiguas (API < 33)
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("SONG_DATA")
        }
    }
}