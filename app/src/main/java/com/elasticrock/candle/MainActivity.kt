package com.elasticrock.candle

import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.elasticrock.candle.ui.theme.CandleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CandleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TorchApp(contentResolver, window)
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun TorchApp(contentResolver: ContentResolver, window: Window) {
    val tag = "MainActivity"
    val brightness = Settings.System.getFloat(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
    var brightnessPercentage by remember { mutableFloatStateOf(brightness/255f) }
    Slider(
        value = brightnessPercentage,
        onValueChange = {
            brightnessPercentage = it
            val params = window.attributes
            params.screenBrightness = it
            window.attributes = params
        }
    )
}