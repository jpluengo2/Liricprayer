package com.example.liricprayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liricprayer.R
import com.example.liricprayer.data.Song
import android.content.Intent
import com.example.liricprayer.activities.SongDetailActivity

// Usamos ListAdapter para mejor rendimiento.
class SongAdapter : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    // 1. El ViewHolder: "Sostiene" las vistas de la fila
    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.song_title)
        private val snippetView: TextView = itemView.findViewById(R.id.song_snippet)
        private val liturgyView: TextView = itemView.findViewById(R.id.song_liturgy)

        // ¡NUEVA! Variable para guardar la canción actual
        private var currentSong: Song? = null

        // ¡NUEVO! Bloque init para el click
        init {
            itemView.setOnClickListener {
                currentSong?.let { song ->
                    val context = itemView.context
                    // 1. Creamos el Intent para ir a SongDetailActivity
                    val intent = Intent(context, SongDetailActivity::class.java).apply {
                        // 2. Metemos el objeto 'Song' (que ya es Parcelable)
                        putExtra("SONG_DATA", song)
                    }
                    // 3. Iniciamos la nueva Activity
                    context.startActivity(intent)
                }
            }
        }

        // Función para "rellenar" la fila con datos
        fun bind(song: Song) {
            currentSong = song // <-- ¡NUEVO! Guardamos la canción

            titleView.text = song.title

            val snippet = song.lyrics.lines().take(2).joinToString("\n")
            snippetView.text = snippet

            liturgyView.text = song.liturgyTypes.joinToString(", ")
        }
    }

    // 2. Crea la vista de la fila (inflando el XML)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_list_item, parent, false)
        return SongViewHolder(view)
    }

    // 3. Conecta los datos (de la lista) con la fila (ViewHolder)
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }

    // 4. Clase de ayuda para que el Adapter sepa qué ha cambiado
    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.title == newItem.title // Asumimos títulos únicos
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}