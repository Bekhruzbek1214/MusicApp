package com.example.musicapp.presenter.viewmodel.implementation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.MusicData
import com.example.musicapp.domain.MusicRepository
import com.example.musicapp.navigation.AppNavigator
import com.example.musicapp.presenter.screen.MainScreenDirections
import com.example.musicapp.presenter.viewmodel.MainViewModel
import com.example.musicapp.utils.musicList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val appNavigator: AppNavigator,
    private val musicRepository: MusicRepository
) : ViewModel(), MainViewModel {
    override val allMusicList = MutableSharedFlow<List<MusicData>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    override val refreshState = MutableStateFlow(false)

    override fun requestAllMusic() {
        refreshState.value = true
        musicRepository.getAllMusicList()
            .onEach {
                refreshState.value = false
                it.onSuccess { list ->
                    musicList = list
                    allMusicList.tryEmit(list)
                }.onFailure { thr ->

                }
            }
            .launchIn(viewModelScope)
    }

    override fun shuffleAllMusic() {
        refreshState.value = true
        musicRepository.getAllMusicList()
            .onEach {
                refreshState.value = false
                it.onSuccess { list ->
                    musicList = list.shuffled()
                    allMusicList.tryEmit(list)
                }.onFailure { thr ->

                }
            }
            .launchIn(viewModelScope)
    }

    override fun onClickMusic(musicData: MusicData) {
        viewModelScope.launch {
            appNavigator.navigateTo(MainScreenDirections.actionMainScreenToMusicScreen(musicData))
        }
    }
}