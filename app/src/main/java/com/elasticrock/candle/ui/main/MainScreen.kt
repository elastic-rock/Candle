package com.elasticrock.candle.ui.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elasticrock.candle.R
import com.elasticrock.candle.ui.components.PreferenceSlider
import com.elasticrock.candle.ui.components.SavedColor
import com.elasticrock.candle.util.maxBrightness
import com.elasticrock.candle.util.minBrightness
import com.elasticrock.candle.util.setBrightness
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onKeepScreenOn: () -> Unit,
    onAllowOnLockScreen: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(skipHiddenState = false))
    val state = viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val brightness = state.value.brightness
    val selectedHue = state.value.hue
    val selectedLightness = state.value.lightness

    val isCandleEffectRunning = state.value.isCandleEffectRunning

    val keepScreenOn = state.value.keepScreenOn
    val allowOnLockScreen = state.value.allowOnLockScreen

    val isLocked = state.value.isLocked

    val view = LocalView.current
    val window = (view.context as Activity).window

    if (isLocked) {
        BackHandler {
            // Block back navigation
        }
    }

    LaunchedEffect(brightness) {
        setBrightness(window, brightness)
    }

    LaunchedEffect(keepScreenOn) {
        if (keepScreenOn) {
            onKeepScreenOn()
        }
    }

    LaunchedEffect(allowOnLockScreen) {
        if (allowOnLockScreen) {
            onAllowOnLockScreen()
        }
    }

    if (!view.isInEditMode) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = selectedLightness > 0.5f
    }

    fun startCandleEffect() {
        scope.launch { scaffoldState.bottomSheetState.hide() }
        viewModel.startCandleEffect()
    }

    fun stopCandleEffect() {
        viewModel.stopCandleEffect()
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
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    val hue0 = state.value.hue0
                    val lightness0 = state.value.lightness0
                    SavedColor(
                        {
                            viewModel.loadPreset(0)
                        },
                        {
                            viewModel.savePreset(selectedHue, selectedLightness, brightness, 0)
                        },
                        Color.hsl(hue = hue0, saturation = 1f, lightness = lightness0)
                    )

                    val hue1 = state.value.hue1
                    val lightness1 = state.value.lightness1
                    SavedColor(
                        {
                            viewModel.loadPreset(1)
                        },
                        {
                            viewModel.savePreset(selectedHue, selectedLightness, brightness, 1)
                        },
                        Color.hsl(hue = hue1, saturation = 1f, lightness = lightness1)
                    )

                    val hue2 = state.value.hue2
                    val lightness2 = state.value.lightness2
                    SavedColor(
                        {
                            viewModel.loadPreset(2)
                        },
                        {
                            viewModel.savePreset(selectedHue, selectedLightness, brightness, 2)
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
                        viewModel.onHueChange(it)
                    },
                    onValueChangeFinished = {
                        viewModel.onHueSave()
                    }
                )
                PreferenceSlider(
                    icon = Icons.Filled.Exposure,
                    range = 0f..1f,
                    value = selectedLightness,
                    onValueChange = {
                        viewModel.onLightnessChange(it)
                    },
                    onValueChangeFinished = {
                        viewModel.onLightnessSave()
                    }
                )
                PreferenceSlider(
                    range = minBrightness..maxBrightness,
                    icon = Icons.Filled.Brightness6,
                    value = brightness,
                    onValueChange = {
                        viewModel.onBrightnessChange(it)
                    },
                    onValueChangeFinished = {
                        viewModel.onBrightnessSave()
                    }
                )

                Box(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    contentAlignment = Alignment.CenterStart // Aligns the row content to start
                ) {
                    IconButton(
                        onClick = {
                            viewModel.onLockChange(true)
                            scope.launch { scaffoldState.bottomSheetState.hide() }
                        },
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.lock)
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .align(Alignment.Center),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
                    ) {
                        Text(text = stringResource(id = R.string.settings))
                    }
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
                    val buttonColor = if (selectedLightness > 0.5f) {
                        Color.Black} else {
                        Color.White}
                    if (!isLocked) {
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
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.onLockChange(false)
                            },
                            modifier = Modifier
                                .padding(start = 0.dp, end = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LockOpen,
                                contentDescription = stringResource(R.string.unlock)
                            )
                        }
                    }
                }
            }
        }
    )
}