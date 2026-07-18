package com.example.mozic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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

            NotificationPermissionEffect()

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

/**
 * Android 13+ requires runtime consent for any notification, including the media session's —
 * without it the service still plays audio, it just never shows the notification/lockscreen
 * controls A3 depends on. Requested once per cold start rather than gated behind the first
 * `play()` call, matching how most media apps front-load this ask.
 */
@Composable
private fun NotificationPermissionEffect() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Denial just means no notification; playback itself is unaffected. */ }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
