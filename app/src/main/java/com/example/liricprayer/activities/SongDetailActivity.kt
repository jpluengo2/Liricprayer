package com.example.liricprayer.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.liricprayer.R
import com.example.liricprayer.data.Song
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.regex.Pattern

class SongDetailActivity : AppCompatActivity() {

    private lateinit var youTubePlayerView: YouTubePlayerView
    private var isPlayerInitialized = false // Control para no cargar dos veces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        // 1. Configurar Toolbar
        val toolbar: Toolbar = findViewById(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 2. Encontrar Vistas
        val lyricsText: TextView = findViewById(R.id.lyrics_text)
        val liturgyText: TextView = findViewById(R.id.detail_liturgy_text)
        val fabPlay: FloatingActionButton = findViewById(R.id.fab_play)
        youTubePlayerView = findViewById(R.id.youtube_player_view)

        // Añadir el reproductor al ciclo de vida
        lifecycle.addObserver(youTubePlayerView)

        // 3. Recibir el objeto Song
        val song = getSongFromIntent()

        if (song == null) {
            Toast.makeText(this, "Error al cargar la canción", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 4. Rellenar Textos Iniciales
        supportActionBar?.title = song.title
        lyricsText.text = song.lyrics
        liturgyText.text = song.liturgyTypes.joinToString(", ")

        // 5. Lógica del Botón Play y Video
        // Limpiamos la URL de posibles espacios en blanco invisibles con .trim()
        val cleanUrl = song.audioUrl.trim()
        val videoId = if (cleanUrl.isNotEmpty()) extractYouTubeId(cleanUrl) else null

        if (videoId != null) {
            // Si hay video válido, mostramos el botón FAB
            fabPlay.visibility = View.VISIBLE

            // Configurar el click del botón
            fabPlay.setOnClickListener {
                if (!isPlayerInitialized) {
                    // CAMBIO DE DISEÑO AL PULSAR PLAY:
                    // 1. Ocultar Liturgia
                    liturgyText.visibility = View.GONE
                    // 2. Mostrar marco de video
                    youTubePlayerView.visibility = View.VISIBLE
                    // 3. Ocultar el botón play (ya no hace falta)
                    fabPlay.visibility = View.GONE

                    // 4. Cargar y arrancar video
                    youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            isPlayerInitialized = true
                            // loadVideo arranca automáticamente. cueVideo solo lo carga.
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    })
                }
            }
        } else {
            // No hay URL válida
            fabPlay.visibility = View.GONE
            youTubePlayerView.visibility = View.GONE
        }
    }

    // Extrae el ID del video con limpieza de caracteres
    // Función de extracción robusta (funciona con youtube.com y youtu.be)
    private fun extractYouTubeId(url: String): String? {
        val pattern = "^.*(youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=|&v=)([^#&?]*).*"
        val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = compiledPattern.matcher(url)

        return if (matcher.find()) {
            matcher.group(2) // Devuelve solo el ID (ej: 7KcN3lY3g8g)
        } else {
            null
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        youTubePlayerView.release()
    }

    private fun getSongFromIntent(): Song? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("SONG_DATA", Song::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("SONG_DATA")
        }
    }
}