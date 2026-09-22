package com.cybersec.liquidstream

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.cybersec.liquidstream.core.glass.LiquidGlassColors
import com.cybersec.liquidstream.ui.navigation.AppNavHost
import com.google.accompanist.systemuicontroller.rememberSystemUiController

object PipManager {
    var isVideoActive: Boolean = false
    val isInPipMode = mutableStateOf(false)

    fun enterPip(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                activity.enterPictureInPictureMode(params)
            } catch (_: Exception) {
                false
            }
        }
        return false
    }
}

class MainActivity : ComponentActivity() {

    val currentIntentState = mutableStateOf<android.content.Intent?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentState.value = intent
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (PipManager.isVideoActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PipManager.enterPip(this)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PipManager.isInPipMode.value = isInPictureInPictureMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntentState.value = intent
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = rememberSystemUiController()
            val isDarkTheme = isSystemInDarkTheme()

            // Apply transparent system bars for immersive CINEVA experience
            LaunchedEffect(isDarkTheme) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = !isDarkTheme
                )
                systemUiController.setNavigationBarColor(
                    color = Color.Transparent,
                    darkIcons = !isDarkTheme
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (isDarkTheme) LiquidGlassColors.Background else LiquidGlassColors.LightBackground
            ) {
                AppNavHost(incomingIntent = currentIntentState.value)
            }
        }
    }
}
