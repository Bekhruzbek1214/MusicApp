package com.example.musicapp.presenter.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.EnumAction
import com.example.musicapp.data.MusicData
import com.example.musicapp.databinding.ScreenMusicBinding
import com.example.musicapp.presenter.service.MusicService
import com.example.musicapp.presenter.viewmodel.MusicViewModel
import com.example.musicapp.presenter.viewmodel.implementation.MusicViewModelImp
import com.example.musicapp.utils.changeProgressState
import com.example.musicapp.utils.currentTimeLiveData
import com.example.musicapp.utils.isPlayingLiveData
import com.example.musicapp.utils.mediaPlayer
import com.example.musicapp.utils.messageReceiver
import com.example.musicapp.utils.playMusicLiveData
import com.example.musicapp.utils.setChangeProgress
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class MusicScreen : Fragment(R.layout.screen_music) {
    private val binding by viewBinding(ScreenMusicBinding::bind)
    private val viewModel: MusicViewModel by viewModels<MusicViewModelImp>()
    private val navArgs by navArgs<MusicScreenArgs>()

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() = binding.apply {
        musicName.text = navArgs.musicdata.title
        next.setOnClickListener { startMyService(EnumAction.NEXT) }
        prev.setOnClickListener { startMyService(EnumAction.PREV) }
        playPause.setOnClickListener {
            if (mediaPlayer!!.isPlaying)
                startMyService(EnumAction.PAUSE)
            else {
                startMyService(EnumAction.RESUME)
            }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
              //  musicLogger("progress -> $progress fromUser -> $fromUser", "seek")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //musicLogger("onstart progress -> ${seekBar?.progress}", "seek")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
             //   musicLogger(" onstop progress -> ${seekBar?.progress}", "seek")
                changeProgressState = binding.seekBar.progress.toLong()
                startMyService(EnumAction.PROGRESS)
            }
        })
        playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        isPlayingLiveData.observe(viewLifecycleOwner, isPlayingObserver)
        currentTimeLiveData.observe(viewLifecycleOwner, currentTimeObserver)
        btnBack.setOnClickListener { viewModel.onClickBack() }
        requireActivity().changeColorStatusBar(R.color.app_bar_color)
    }


    private fun startMyService(actionEnum: EnumAction) {
        val intent = Intent(requireContext(), MusicService::class.java)
        intent.putExtra("COMMAND", actionEnum)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    private val playMusicObserver = Observer<MusicData> {
        binding.seekBar.max = it.duration
        binding.musicName.text = it.title
        binding.end.text = getDateFormat(it.duration.toLong())
        binding.author.text = it.artist
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(it.imagePath)
        val coverBytes = retriever.embeddedPicture
        if (coverBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
            Glide
                .with(binding.root.context)
                .load(bitmap)
                .placeholder(R.drawable.music)
                .error(R.drawable.music)
                .into(binding.musicImg)
        } else {
            Glide
                .with(binding.root.context)
                .load(R.drawable.music)
                .into(binding.musicImg)
        }
        retriever.release()
    }

    private val isPlayingObserver = Observer<Boolean> {
        if (it) binding.playPause.setImageResource(R.drawable.pause_circle_svgrepo_com)
        else binding.playPause.setImageResource(R.drawable.play_circle_svgrepo_com)
    }

    private val currentTimeObserver = Observer<Long> {
        binding.seekBar.progress = it.toInt()
        binding.start.text = getDateFormat(it)
    }

    private fun getDateFormat(mill: Long): String {
        return if (mill < 3_600_000) {
            val formatter = SimpleDateFormat("mm:ss", Locale.ROOT)
            formatter.timeZone = TimeZone.getTimeZone("GMT")
            formatter.format(mill)
        } else {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
            formatter.timeZone = TimeZone.getTimeZone("GMT")
            formatter.format(mill)
        }
    }

    private fun Activity.changeColorStatusBar(color: Int = R.color.black) {
        val window: Window = this.window
        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(this, color)
    }
}