package com.example.musicapp.presenter.screen

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.data.EnumAction
import com.example.musicapp.data.MusicData
import com.example.musicapp.databinding.ScreenMainBinding
import com.example.musicapp.presenter.adapter.MusicListAdapter
import com.example.musicapp.presenter.service.MusicService
import com.example.musicapp.presenter.viewmodel.MainViewModel
import com.example.musicapp.presenter.viewmodel.implementation.MainViewModelImpl
import com.example.musicapp.utils.isPlayingLiveData
import com.example.musicapp.utils.mediaPlayer
import com.example.musicapp.utils.playMusicLiveData
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File


@AndroidEntryPoint
class MainScreen : Fragment(R.layout.screen_main) {
    private val binding by viewBinding(ScreenMainBinding::bind)
    private val viewModel: MainViewModel by viewModels<MainViewModelImpl>()
    private val musicCursorAdapter = MusicListAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.requestAllMusic()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initFlow()
    }

    private fun initView() = binding.apply {
        checkPermission()
        rvList.adapter = musicCursorAdapter
        rvList.layoutManager = LinearLayoutManager(requireContext())
        musicCursorAdapter.setOnClickMusic {

            val file = File(it.musicPath)
            if (file.exists()) {
                startMyService(EnumAction.PLAY)
            } else {
                viewModel.requestAllMusic()
            }
        }
        srl.setOnRefreshListener {
            viewModel.requestAllMusic()
        }

        shuffle.setOnClickListener {
            startMyService(EnumAction.NEXT)
        }
        requireActivity().window.statusBarColor = Color.parseColor("#242F3D")
    }

    private fun initFlow() = binding.apply {
        playMusicLiveData.observe(viewLifecycleOwner, playMusicObserver)
        isPlayingLiveData.observe(viewLifecycleOwner, isPlayingObserver)
        viewModel.allMusicList
            .onEach {
                musicCursorAdapter.submitList(it)
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
        viewModel
            .refreshState
            .onEach {
                srl.isRefreshing = it
            }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }

    private fun startMyService(actionEnum: EnumAction) {
        val intent = Intent(requireContext(), MusicService::class.java)
        intent.putExtra("COMMAND", actionEnum)
        if (Build.VERSION.SDK_INT >= 26) {
            requireActivity().startForegroundService(intent)
        } else requireActivity().startService(intent)
    }

    private val playMusicObserver = Observer<MusicData> {
        binding.tagPlayer.setOnClickListener { _ ->
            val file = File(it.musicPath)
            if (file.exists()) {
                viewModel.onClickMusic(it)
            } else {
                mediaPlayer?.stop()
                viewModel.requestAllMusic()
                startMyService(EnumAction.NEXT)
            }
        }
//        binding.btnManage.setOnClickListener {
//            startMyService(if (mediaPlayer!!.isPlaying) EnumAction.PAUSE else EnumAction.RESUME)
//        }
        binding.playMusic.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                startMyService(EnumAction.PAUSE)
                binding.playPauseText.text = "Play"
            }
            else {
            binding.playPauseText.text = "Pause"
                startMyService(EnumAction.RESUME)
            }
        }
        musicCursorAdapter.playingChange()
        binding.tagName.text = it.title
        binding.tagAuthor.text = it.artist
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(it.imagePath)

        //binding.tagImg.setImageResource(R.drawable.music_app_image2)
//        val coverBytes = retriever.embeddedPicture
//        if (coverBytes != null) {
//            val bitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
//            binding.tagImg.setImageBitmap(bitmap)
//        } else {
//
//        }
//        retriever.release()
    }

    private val isPlayingObserver = Observer<Boolean> {temp ->
        binding.top.isVisible = false
        binding.topMusic.isVisible = true
        Log.d("TTT", "isPlayingObserver ${temp}")
        musicCursorAdapter.setManageState(temp)
//        binding.btnManage.setOnClickListener {
//            if (temp) binding.btnImage.setImageResource(R.drawable.pause_circle_svgrepo_com)
//            else binding.btnImage.setImageResource(R.drawable.play_stream)
//        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(
                permissions = listOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                allGrantedOk = {
                    viewModel.requestAllMusic()
                },
                allGrantedNo = {
                    showAlertDialog(
                        onClickOk = {
                            requestPermission(
                                permissions = listOf(
                                    Manifest.permission.READ_MEDIA_AUDIO,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ),
                                allGrantedOk = { viewModel.requestAllMusic() },
                                allGrantedNo = { requireActivity().finish() }
                            )
                        },
                        onClickCancel = {
                            requireActivity().finish()
                        }
                    )
                }
            )
        } else {
            requestPermission(
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                allGrantedOk = {
                    viewModel.requestAllMusic()
                },
                allGrantedNo = {
                    showAlertDialog(
                        onClickOk = {
                            requestPermission(
                                permissions = listOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                ),
                                allGrantedOk = { viewModel.requestAllMusic() },
                                allGrantedNo = { requireActivity().finish() }
                            )
                        },
                        onClickCancel = {
                            requireActivity().finish()
                        }
                    )
                }
            )
        }
    }

    private fun showAlertDialog(onClickOk: () -> Unit, onClickCancel: () -> Unit) {
        AlertDialog.Builder(requireActivity())
            .setTitle("Important")
            .setMessage("Please allow the permissions!")
            .setPositiveButton("Allow") { dialog, _ ->
                onClickOk.invoke()
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                onClickCancel.invoke()
                dialog.dismiss()
            }
            .show()
    }

    private fun requestPermission(
        permissions: List<String>,
        allGrantedOk: () -> Unit,
        allGrantedNo: () -> Unit
    ) {
        PermissionX
            .init(this)
            .permissions(permissions)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    allGrantedOk.invoke()
                } else {
                    allGrantedNo.invoke()
                }
            }
    }
}