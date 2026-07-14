package com.example.mozic

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mozic.core.designsystem.theme.MozicTheme
import com.example.mozic.core.domain.model.ThemeSetting
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val uiState by appViewModel.uiState.collectAsStateWithLifecycle()

            when (val state = uiState) {
                AppUiState.Loading -> Unit
                is AppUiState.Ready -> {
                    val darkTheme = when (state.preferences.theme) {
                        ThemeSetting.LIGHT -> false
                        ThemeSetting.DARK -> true
                        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
                    }
                    MozicTheme(darkTheme = darkTheme, fontScale = state.preferences.fontScale) {
                        MozicApp()
                    }
                }
            }
        }
    }
}
