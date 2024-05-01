package com.example.musicapp.presenter.viewmodel

import android.database.Cursor
import com.example.musicapp.data.MusicData
import kotlinx.coroutines.flow.Flow

interface MainViewModel {

    val allMusicList : Flow<List<MusicData>>
    val refreshState : Flow<Boolean>

    fun requestAllMusic()
    fun shuffleAllMusic()

    fun onClickMusic(musicData: MusicData)
}