package com.example.liricprayer.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize 

@Parcelize
data class Song(
    @SerializedName("titulo")
    val title: String,

    @SerializedName("letra")
    val lyrics: String,

    @SerializedName("tipo_liturgia")
    val liturgyTypes: List<String>,

    @SerializedName("url")
    val audioUrl: String
) : Parcelable