package com.example.musicapp.domain

import android.database.Cursor
import com.example.musicapp.data.MusicData
import kotlinx.coroutines.flow.Flow



interface MusicRepository {

    fun getAllMusicList() : Flow<Result<List<MusicData>>>
}