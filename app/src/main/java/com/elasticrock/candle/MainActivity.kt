package com.elasticrock.candle

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarService = applicationContext.getSystemService(StatusBarManager::class.java)
            statusBarService.requestAddTileService(
                ComponentName(applicationContext, QSTileService::class.java.name),
                applicationContext.getString(R.string.candle),
                android.graphics.drawable.Icon.createWithResource(applicationContext,R.drawable.rounded_candle_qs),
                {}) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorchApp(window: Window, dataStore: DataStore<Preferences>) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var selectedHue by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = Color.hsl(hue = selectedHue, saturation = 1f, lightness = 0.5f),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeContentPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PreferenceSlider(
                    icon = Icons.Filled.Palette,
                    range = 0f..360f,
                    value = selectedHue,
                    onValueChange = { selectedHue = it }
                )
                var brightness by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousBrightness() }) }
                PreferenceSlider(
                    range = minBrightness..maxBrightness,
                    icon = Icons.Filled.Brightness6,
                    value = brightness,
                    onValueChange = {
                        brightness = it
                        setBrightness(window, it)
                        runBlocking { DataStore(dataStore).savePreviousBrightness(it) }
                    }
                )
            }
        },
        content = {

        }
    )
}

@Composable
fun PreferenceSlider(
    icon: ImageVector,
    range: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .size(24.dp),
        )

        Slider(
            modifier = Modifier.padding(end = 8.dp),
            value = value,
            valueRange = range,
            onValueChange = onValueChange
        )
    }
}