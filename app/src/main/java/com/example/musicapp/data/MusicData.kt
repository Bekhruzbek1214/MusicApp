package com.example.musicapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicData (
    val id: Long,
    val title: String,
    val musicPath: String,
    val imagePath: String,
    val duration: Int,
    val size: Int,
    val artist: String
):Parcelable