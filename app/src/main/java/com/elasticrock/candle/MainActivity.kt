package com.elasticrock.candle

import android.app.Activity
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elasticrock.candle.ui.theme.CandleTheme
import com.elasticrock.candle.ui.theme.applyOpacity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarService = applicationContext.getSystemService(StatusBarManager::class.java)
            statusBarService.requestAddTileService(
                ComponentName(applicationContext, QSTileService::class.java.name),
                applicationContext.getString(R.string.candle),
                android.graphics.drawable.Icon.createWithResource(applicationContext,R.drawable.rounded_candle_qs),
                {}) {}
        }

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
}

private const val horizontal = 8
private const val vertical = 16

@Composable
fun TorchApp(dataStore: DataStore<Preferences>) {
    val view = LocalView.current
    val window = (view.context as Activity).window
    var keepScreenOn by remember { mutableStateOf(runBlocking { DataStore(dataStore).readKeepScreenOn() }) }
    val scope = rememberCoroutineScope()
    val onKeepScreenOnPreferenceChange = {
        if (!keepScreenOn) {
            keepScreenOn = true
            scope.launch { DataStore(dataStore).saveKeepScreenOn(true) }

        } else {
            keepScreenOn = false
            scope.launch { DataStore(dataStore).saveKeepScreenOn(false) }
        }
    }

    if (keepScreenOn) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable("main") { Torch(dataStore, navController) }
        composable("settings") {
            Settings(
                navController = navController,
                onKeepScreenOnPreferenceChange = { onKeepScreenOnPreferenceChange() },
                keepScreenOn = keepScreenOn
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController, onKeepScreenOnPreferenceChange: (() -> Unit) = {}, keepScreenOn: Boolean) {
    Scaffold(Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = { IconButton(onClick = {
                    navController.navigate("main")
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                }
                }
            )
        },
        content = { padding ->
            LazyColumn(Modifier.padding(padding)) {
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.keep_screen_on),
                        description = stringResource(id = R.string.keep_screen_on_description),
                        icon = Icons.Filled.Lock,
                        isChecked = keepScreenOn,
                        onClick = { onKeepScreenOnPreferenceChange() }
                    )
                }
            }
        }
    )
}

@Composable
fun PreferenceSwitch(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isChecked: Boolean = true,
    checkedIcon: ImageVector = Icons.Outlined.Check,
    onClick: (() -> Unit) = {},
) {
    val thumbContent: (@Composable () -> Unit)? = if (isChecked) {
        {
            Icon(
                imageVector = checkedIcon,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }
    Surface(
        modifier = Modifier.toggleable(value = isChecked,
            enabled = enabled,
            onValueChange = { onClick() })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal.dp, vertical.dp)
                .padding(start = if (icon == null) 12.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 16.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                PreferenceItemTitle(
                    text = title,
                    enabled = enabled
                )
                if (!description.isNullOrEmpty()) PreferenceItemDescription(
                    text = description,
                    enabled = enabled
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 20.dp, end = 6.dp),
                enabled = enabled,
                thumbContent = thumbContent
            )
        }
    }
}

@Composable
fun PreferenceItemTitle(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 2,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onBackground.applyOpacity(enabled),
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        style = style,
        color = color,
        overflow = overflow
    )
}

@Composable
fun PreferenceItemDescription(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.applyOpacity(enabled)
) {
    Text(
        modifier = modifier.padding(top = 2.dp),
        text = text,
        maxLines = maxLines,
        style = style,
        color = color,
        overflow = overflow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Torch(dataStore: DataStore<Preferences>, navController: NavHostController) {
    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))
    var brightness by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousBrightness() }) }
    var selectedHue by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousHue() }) }
    var selectedLightness by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreviousLightness() }) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val window = (view.context as Activity).window

    if (!view.isInEditMode) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = selectedLightness > 0.5f
    }

    var isCandleEffectRunning by remember { mutableStateOf(false) }
    var candleJob: Job? = null

    suspend fun simulateCandleEffect() {

        val updateIntervalMillis: Long = 200
        val lightnessRandomRange = 0.15f

        while (isCandleEffectRunning) {
            val lightnessRandom = (Math.random() * lightnessRandomRange).toFloat()

            val hue = 30f
            val lightness = 0.4f + lightnessRandom
            
            selectedHue = hue
            selectedLightness = lightness.coerceIn(0f, 1f)

            delay(updateIntervalMillis)
        }
    }

    fun startCandleEffect() {
        if (!isCandleEffectRunning) {
            isCandleEffectRunning = true
            scope.launch { scaffoldState.bottomSheetState.hide() }
            candleJob = scope.launch {
                simulateCandleEffect()
            }
        }
    }

    fun stopCandleEffect() {
        isCandleEffectRunning = false
        candleJob?.cancel()
    }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetSwipeEnabled = true,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    var hue0 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(0).first }) }
                    var lightness0 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(0).second }) }
                    var brightness0 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(0).third }) }
                    SavedColor(
                        {
                            selectedHue = hue0
                            selectedLightness = lightness0
                            brightness = brightness0
                            setBrightness(window, brightness0)
                            scope.launch { DataStore(dataStore).savePreviousHue(hue0) }
                            scope.launch { DataStore(dataStore).savePreviousLightness(lightness0) }
                            scope.launch { DataStore(dataStore).savePreviousBrightness(brightness0) }
                        },
                        {
                            scope.launch { DataStore(dataStore).savePreset(selectedHue, selectedLightness, brightness, 0) }
                            hue0 = selectedHue
                            lightness0 = selectedLightness
                            brightness0 = brightness
                        },
                        Color.hsl(hue = hue0, saturation = 1f, lightness = lightness0)
                    )

                    var hue1 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(1).first }) }
                    var lightness1 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(1).second }) }
                    var brightness1 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(1).third }) }
                    SavedColor(
                        {
                            selectedHue = hue1
                            selectedLightness = lightness1
                            brightness = brightness1
                            setBrightness(window, brightness1)
                            scope.launch { DataStore(dataStore).savePreviousHue(hue1) }
                            scope.launch { DataStore(dataStore).savePreviousLightness(lightness1) }
                            scope.launch { DataStore(dataStore).savePreviousBrightness(brightness1) }
                        },
                        {
                            scope.launch { DataStore(dataStore).savePreset(selectedHue, selectedLightness, brightness, 1) }
                            hue1 = selectedHue
                            lightness1 = selectedLightness
                            brightness1 = brightness
                        },
                        Color.hsl(hue = hue1, saturation = 1f, lightness = lightness1)
                    )

                    var hue2 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(2).first }) }
                    var lightness2 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(2).second }) }
                    var brightness2 by remember { mutableFloatStateOf(runBlocking { DataStore(dataStore).readPreset(2).third }) }
                    SavedColor(
                        {
                            selectedHue = hue2
                            selectedLightness = lightness2
                            brightness = brightness2
                            setBrightness(window, brightness2)
                            scope.launch { DataStore(dataStore).savePreviousHue(hue2) }
                            scope.launch { DataStore(dataStore).savePreviousLightness(lightness2) }
                            scope.launch { DataStore(dataStore).savePreviousBrightness(brightness2) }
                        },
                        {
                            scope.launch { DataStore(dataStore).savePreset(selectedHue, selectedLightness, brightness, 2) }
                            hue2 = selectedHue
                            lightness2 = selectedLightness
                            brightness2 = brightness
                        },
                        Color.hsl(hue = hue2, saturation = 1f, lightness = lightness2)
                    )
                    Button(onClick = { startCandleEffect() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Image(imageVector = Icons.Filled.Cake, contentDescription = null)
                    }
                }
                PreferenceSlider(
                    icon = Icons.Filled.Palette,
                    range = 0f..360f,
                    value = selectedHue,
                    onValueChange = {
                        selectedHue = it
                        scope.launch { DataStore(dataStore).savePreviousHue(it) }
                    }
                )
                PreferenceSlider(
                    icon = Icons.Filled.Exposure,
                    range = 0f..1f,
                    value = selectedLightness,
                    onValueChange = {
                        selectedLightness = it
                        scope.launch { DataStore(dataStore).savePreviousLightness(it) }
                    }
                )
                PreferenceSlider(
                    range = minBrightness..maxBrightness,
                    icon = Icons.Filled.Brightness6,
                    value = brightness,
                    onValueChange = {
                        brightness = it
                        setBrightness(window, it)
                        scope.launch { DataStore(dataStore).savePreviousBrightness(it) }
                    }
                )

                OutlinedButton(
                    onClick = { navController.navigate("settings") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
                ) {
                    Text(text = stringResource(id = R.string.settings))
                }
                
                Spacer(modifier = Modifier.padding(4.dp))
            }
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
                    val buttonColor = if (selectedLightness > 0.5f) {Color.Black} else {Color.White}
                    OutlinedButton(
                        onClick = {
                            if (isCandleEffectRunning) {
                                stopCandleEffect()
                            } else {
                                scope.launch { scaffoldState.bottomSheetState.expand() }
                            }
                                  },
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonColor),
                        contentPadding = PaddingValues(start = 16.dp, end = 24.dp)
                    ) {
                        if (isCandleEffectRunning) {
                            Text(text = stringResource(id = R.string.stop), modifier = Modifier.padding(start = 8.dp))
                        } else {
                            Icon(imageVector = Icons.Filled.Tune, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text(text = stringResource(id = R.string.colors))
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedColor(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 40.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(containerColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {

    }
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
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = MaterialTheme.colorScheme.onSurface
            ),
            valueRange = range,
            onValueChange = onValueChange
        )
    }
}