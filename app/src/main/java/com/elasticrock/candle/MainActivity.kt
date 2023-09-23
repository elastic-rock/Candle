package com.elasticrock.candle

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.elasticrock.candle.ui.theme.CandleTheme
import kotlinx.coroutines.runBlocking

const val tag = "MainActivity"
const val maxBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
const val minBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

fun setBrightness(window: Window, brightness: Float) {
    val params = window.attributes
    params.screenBrightness = brightness
    window.attributes = params
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        }

        runBlocking { setBrightness(window, DataStore(dataStore).readPreviousBrightness()) }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CandleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TorchApp(window, dataStore)
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

private const val horizontal = 8
private const val vertical = 32

@Composable
fun TorchApp(window: Window, dataStore: DataStore<Preferences>) {
    Surface(
        modifier = Modifier.safeContentPadding(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal.dp, vertical.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Brightness6,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                )
                var brightness by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousBrightness() }) }
                Slider(
                    modifier = Modifier.padding(end = 8.dp),
                    value = brightness,
                    valueRange = minBrightness..maxBrightness,
                    onValueChange = {
                        brightness = it
                        setBrightness(window, it)
                        runBlocking { DataStore(dataStore).savePreviousBrightness(it) }
                    }
                )
            }
        }
    }
}