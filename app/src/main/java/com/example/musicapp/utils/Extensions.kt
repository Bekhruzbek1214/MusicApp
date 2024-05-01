package com.example.musicapp.utils

import android.database.Cursor
import android.provider.MediaStore
import com.example.musicapp.data.MusicData
import timber.log.Timber



fun Cursor.toMusicData(position: Int): MusicData {
    this.moveToPosition(position)
    return MusicData(
        id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
        title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
        musicPath = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
        duration = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
        size = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
        artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
        imagePath = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
    )
}