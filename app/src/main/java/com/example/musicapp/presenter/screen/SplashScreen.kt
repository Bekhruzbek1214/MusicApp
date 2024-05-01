package com.example.musicapp.presenter.screen

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.musicapp.R
import com.example.musicapp.databinding.ScreenSplashBinding
import com.example.musicapp.presenter.viewmodel.SplashViewModel
import com.example.musicapp.presenter.viewmodel.implementation.SplashViewModelImpl
import com.permissionx.guolindev.PermissionX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment(R.layout.screen_splash) {
    private val binding by viewBinding(ScreenSplashBinding::bind)
    private val viewModel: SplashViewModel by viewModels<SplashViewModelImpl>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initView() {
        lifecycleScope.launch {
            delay(1200)
            PermissionX
                .init(this@SplashScreen)
                .permissions(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                .request { allGranted, _, _ ->
                    run {
                        if (allGranted) {
                            viewModel.openSplash()
                        } else {

                            requireActivity().finish()
                        }
                    }
                }
        }

        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
    }
}