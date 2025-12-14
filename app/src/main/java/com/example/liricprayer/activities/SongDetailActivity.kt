package com.example.liricprayer.activities

import android.net.Uri
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class SongDetailActivity : AppCompatActivity() {

    private lateinit var youTubePlayerView: YouTubePlayerView
    private var isPlayerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_detail)

        // 1. Configuración de UI
        val toolbar: Toolbar = findViewById(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val lyricsText: TextView = findViewById(R.id.lyrics_text)
        val liturgyText: TextView = findViewById(R.id.detail_liturgy_text)
        val fabPlay: FloatingActionButton = findViewById(R.id.fab_play)
        youTubePlayerView = findViewById(R.id.youtube_player_view)

        // IMPORTANTE: Añadir al ciclo de vida para evitar fugas de memoria
        lifecycle.addObserver(youTubePlayerView)

        // 2. Obtener datos
        val song = getSongFromIntent()
        if (song == null) {
            finish()
            return
        }

        // 3. Pintar datos básicos
        supportActionBar?.title = song.title
        lyricsText.text = song.lyrics
        liturgyText.text = song.liturgyTypes.joinToString(", ")

        // 4. Lógica "Blindada" del Video
        // Limpiamos espacios, saltos de línea y tabuladores que puedan ensuciar el link
        val rawUrl = song.audioUrl.trim().replace("\\s+".toRegex(), "")
        val videoId = if (rawUrl.isNotEmpty()) extractYouTubeId(rawUrl) else null

        if (videoId != null) {
            fabPlay.visibility = View.VISIBLE

            // Configuración del botón Play
            fabPlay.setOnClickListener {
                if (!isPlayerInitialized) {
                    // Cambio visual
                    liturgyText.visibility = View.GONE
                    fabPlay.visibility = View.GONE
                    youTubePlayerView.visibility = View.VISIBLE

                    // CONFIGURACIÓN INFALIBLE (IFrameOptions)
                    // Esto le dice a YouTube que somos un reproductor legítimo y evita el error de "Video no disponible"
                    val options = IFramePlayerOptions.Builder(this)
                        .controls(1)       // Mostrar controles (play, volumen)
                        .rel(0)            // No mostrar videos relacionados al acabar
                        .ivLoadPolicy(3)   // Ocultar anotaciones de video
                        .ccLoadPolicy(0)   // Sin subtítulos por defecto
                        .build()

                    // Inicialización manual con las opciones Y EL CONTEXTO
                    youTubePlayerView.initialize(
                        youTubePlayerListener = object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                isPlayerInitialized = true
                                // Cargar y reproducir inmediatamente
                                youTubePlayer.loadVideo(videoId, 0f)
                            }

                            override fun onError(youTubePlayer: YouTubePlayer, error: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError) {
                                super.onError(youTubePlayer, error)
                                // Si falla, mostramos un aviso discreto pero útil
                                Toast.makeText(this@SongDetailActivity, "Error YouTube: $error", Toast.LENGTH_LONG).show()
                            }
                        },
                        handleNetworkEvents = true, // It's good practice to handle network events
                        playerOptions = options
                    )
                }
            }
        } else {
            // Sin video o URL inválida
            fabPlay.visibility = View.GONE
            youTubePlayerView.visibility = View.GONE
        }
    }

    // Metodo de extracción robusto usando URI nativo de Android
    private fun extractYouTubeId(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return null

            when {
                // Caso: youtube.com/watch?v=ID
                host.contains("youtube") -> uri.getQueryParameter("v")
                // Caso: youtu.be/ID (enlace corto)
                host.contains("youtu.be") -> uri.lastPathSegment
                // Caso: Enlace embed
                url.contains("embed/") -> uri.lastPathSegment
                else -> null
            }
        } catch (e: Exception) {
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