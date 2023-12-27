package com.elasticrock.candle

import android.app.Activity
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.elasticrock.candle.ui.theme.CandleTheme
import kotlinx.coroutines.launch
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

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CandleTheme {
                TorchApp(dataStore)
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
fun TorchApp(dataStore: DataStore<Preferences>) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    var brightness by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousBrightness() }) }
    var selectedHue by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousHue() }) }
    var selectedLightness by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousLightness() }) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val window = (view.context as Activity).window

    if (!view.isInEditMode) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = selectedLightness > 0.5f
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetSwipeEnabled = true,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            PreferenceSlider(
                icon = Icons.Filled.Palette,
                range = 0f..360f,
                value = selectedHue,
                onValueChange = {
                    selectedHue = it
                    runBlocking { DataStore(dataStore).savePreviousHue(it) }
                }
            )
            PreferenceSlider(
                icon = Icons.Filled.Exposure,
                range = 0f..1f,
                value = selectedLightness,
                onValueChange = {
                    selectedLightness = it
                    runBlocking { DataStore(dataStore).savePreviousLightness(it) }
                }
            )
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
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
        },
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.hsl(hue = selectedHue, saturation = 1f, lightness = selectedLightness)
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.safeContentPadding()
                ) {
                    Button(
                        onClick = { scope.launch { scaffoldState.bottomSheetState.expand() }},
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "Colours")
                    }
                }
            }
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
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
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