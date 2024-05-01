package com.example.musicapp.presenter.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.musicapp.R
import com.example.musicapp.data.EnumAction
import com.example.musicapp.data.MusicData
import com.example.musicapp.utils.changeProgressState
import com.example.musicapp.utils.currentTime
import com.example.musicapp.utils.currentTimeLiveData
import com.example.musicapp.utils.cursor
import com.example.musicapp.utils.fullTime
import com.example.musicapp.utils.isPlayingLiveData
import com.example.musicapp.utils.mediaPlayer
import com.example.musicapp.utils.messageReceiver
import com.example.musicapp.utils.musicList
import com.example.musicapp.utils.playMusicLiveData
import com.example.musicapp.utils.selectMusicPos
import com.example.musicapp.utils.toMusicData
import java.io.File

class MusicService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null
    private var timeTicker: CountDownTimer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var playBackState: PlaybackStateCompat

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "bahaDewMusicPlayer")
        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPause() {
                //musicLogger("MediaSession onPause", "YYY")
                mediaPlayer?.pause()
                updatePlayState(mediaPlayer!!.currentPosition.toLong())
                isPlayingLiveData.value = false
            }

            override fun onPlay() {
                //musicLogger("MediaSession onPlay", "YYY")
                mediaPlayer?.start()
                changeProgress()
                updatePlayState(mediaPlayer!!.currentPosition.toLong())
                isPlayingLiveData.value = true
            }

            override fun onRewind() {
                super.onRewind()
                //musicLogger("MediaSession onRewind", "YYY")
            }

            override fun onSeekTo(pos: Long) {
               // musicLogger("MediaSession onSeekTo", "YYY")
                changeProgressState = pos
                mediaPlayer!!.seekTo(pos.toInt())
                updatePlayState(mediaPlayer!!.currentPosition.toLong())
                changeProgress()
            }

            override fun onSkipToNext() {
                //musicLogger("MediaSession onSkipToNext", "YYY")
                doneCommand(EnumAction.NEXT)
            }

            override fun onSkipToPrevious() {
                //musicLogger("MediaSession onSkipToPrevious", "YYY")
                doneCommand(EnumAction.PREV)
            }

            override fun onStop() {
               // musicLogger("MediaSession onStop", "YYY")
                mediaPlayer?.pause()
                isPlayingLiveData.value = false
                stopSelf()
            }

            override fun onCustomAction(action: String?, extras: Bundle?) {
                if(action == "CANCEL") {
                    mediaPlayer?.pause()
                    isPlayingLiveData.value = false
                    mediaSession.isActive = false
                    mediaSession.release()
                    stopSelf()
                }
            }
        })
        startMediaSession()
        mediaSession.isActive = true
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun startMediaSession() {
        val metaDataBuilder = MediaMetadataCompat.Builder()
        val data: MusicData = musicList[selectMusicPos]
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, data.title)
        metaDataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, data.artist)
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(data.imagePath)
        val coverBytes = retriever.embeddedPicture
        val bitmap = if (coverBytes != null) {
            BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
        } else {
            resources.getDrawable(R.drawable.music_circle, null).toBitmap()
        }
        updatePlayState(0)
        metaDataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
        retriever.release()
        metaDataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, data.duration.toLong())
        mediaSession.setMetadata(metaDataBuilder.build())
        createNotification()
    }

    private fun createNotification() {
        val mediaSessionToken = mediaSession.sessionToken
        val notificationBuilder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "my_channel_id"
                val channel =
                    NotificationChannel(channelId, "My Channel", NotificationManager.IMPORTANCE_LOW)
                val manager = getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
                NotificationCompat.Builder(this, channelId)
            } else {
                NotificationCompat.Builder(this)
            }

        notificationBuilder
            .setContentTitle("Music App")
            .setContentText("Playing music")
            .setSmallIcon(R.drawable.music_icon2)
            .setAutoCancel(false)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionToken)
                    .setShowActionsInCompactView(
                        0, 1, 2
                    )
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val notification: Notification = notificationBuilder.build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent!!.extras?.getSerializable("COMMAND") as EnumAction
        doneCommand(command)
        return START_STICKY
    }

    private fun doneCommand(command: EnumAction) {
        val data: MusicData = musicList[selectMusicPos]
        when (command) {
            EnumAction.PLAY -> {
                if (mediaPlayer?.isPlaying == true)
                    mediaPlayer?.stop()
                mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(data.musicPath)))
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { doneCommand(EnumAction.NEXT) }
                fullTime = data.duration.toLong()
                changeProgress()
                isPlayingLiveData.value = true
                playMusicLiveData.value = data
                mediaSession.controller.transportControls.play()
                startMediaSession()
            }

            EnumAction.RESUME -> {
                mediaPlayer?.start()
                changeProgress()
                updatePlayState(mediaPlayer!!.currentPosition.toLong())
                isPlayingLiveData.value = true
            }

            EnumAction.PAUSE -> {
                mediaPlayer?.pause()
                isPlayingLiveData.value = false
                mediaSession.controller.transportControls.pause()
                updatePlayState(mediaPlayer!!.currentPosition.toLong())
            }

            EnumAction.NEXT -> {
                currentTime = 0
                if (selectMusicPos + 1 == musicList.size) {
                    selectMusicPos = 0
                } else {
                    selectMusicPos++
                }
                doneCommand(EnumAction.PLAY)
                startMediaSession()
            }

            EnumAction.PREV -> {
                currentTime = 0
                if (selectMusicPos == 0) selectMusicPos =
                    musicList.size - 1
                else selectMusicPos--
                doneCommand(EnumAction.PLAY)
                startMediaSession()
            }

            EnumAction.CANCEL -> {
                mediaPlayer?.pause()
                isPlayingLiveData.value = false
                stopSelf()
            }

            EnumAction.PROGRESS -> {
                mediaPlayer?.seekTo(changeProgressState.toInt())
                updatePlayState(mediaPlayer!!.currentPosition.toLong())
                changeProgress()
            }

            else -> {}
        }
    }

    private fun changeProgress() {
        timeTicker?.cancel()
        timeTicker = object : CountDownTimer(
            (mediaPlayer?.duration?.toLong() ?: 1) - (mediaPlayer?.currentPosition?.toLong() ?: 1),
            100
        ) {
            override fun onTick(millisUntilFinished: Long) {
                currentTime = (mediaPlayer?.currentPosition?.toLong() ?: 0)
                currentTimeLiveData.value =
                    (mediaPlayer?.currentPosition?.toLong() ?: 0)
            }

            override fun onFinish() {
            }
        }
        timeTicker?.start()
    }

    private fun updatePlayState(seekTo: Long) {
        playBackState = PlaybackStateCompat.Builder()
            .setState(
                if (mediaPlayer?.isPlaying != false) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                seekTo,
                1.0f
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .addCustomAction("CANCEL", "CANCEL", R.drawable.cancel_svgrepo_com)
            .build()
        mediaSession.setPlaybackState(playBackState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.isActive = false
        mediaSession.release()
        timeTicker?.cancel()
    }
}