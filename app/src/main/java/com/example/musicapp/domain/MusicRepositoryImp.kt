package com.example.musicapp.domain

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.example.musicapp.data.MusicData
import com.example.musicapp.utils.toMusicData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : MusicRepository {

    private val sortOrder = "${MediaStore.Audio.Media.DISC_NUMBER} ASC"
    private val selection = "is_music != 0 AND title != ''"
    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Images.Media.DATA
    )

    override fun getAllMusicList(): Flow<Result<List<MusicData>>> =
        callbackFlow<Result<List<MusicData>>> {
            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            if (cursor == null) {
                trySend(Result.failure(Throwable("Nul Pointer")))
            } else {
                val list = ArrayList<MusicData>()
                for (i in 0..<cursor.count) {
                    list.add(cursor.toMusicData(i))
                }
                trySend(Result.success(list))
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)
}