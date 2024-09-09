package com.elasticrock.candle.ui.settings

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elasticrock.candle.R
import com.elasticrock.candle.ui.components.PreferenceSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackArrowClick: () -> Unit,
    onLicensesOptionClick: () -> Unit,
    onEnableKeepScreenOn: () -> Unit,
    onDisableKeepScreenOn: () -> Unit,
    onAllowOnLockScreen: () -> Unit,
    onDisallowOnLockScreen: () -> Unit,
    viewModel: SettingsScreenViewModel = hiltViewModel()
) {
    val view = LocalView.current
    val window = (view.context as Activity).window
    if (!view.isInEditMode) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isSystemInDarkTheme()
    }

    val state = viewModel.state.collectAsStateWithLifecycle()
    val keepScreenOn = state.value.keepScreenOn
    val allowOnLockScreen = state.value.allowOnLockScreen

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings)) },
                navigationIcon = { IconButton(onClick = onBackArrowClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.keep_screen_on),
                        description = stringResource(id = R.string.keep_screen_on_description),
                        icon = Icons.Filled.Timer,
                        isChecked = keepScreenOn,
                        onClick = {
                            if (keepScreenOn) {
                                onDisableKeepScreenOn()
                                viewModel.onKeepScreenOnChange(false)
                            } else {
                                onEnableKeepScreenOn()
                                viewModel.onKeepScreenOnChange(true)
                            }
                        }
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    item {
                        PreferenceSwitch(
                            title = stringResource(id = R.string.allow_on_lock_screen),
                            description = stringResource(id = R.string.allow_on_lock_screen_description),
                            icon = Icons.Filled.Lock,
                            isChecked = allowOnLockScreen,
                            onClick = {
                                if (allowOnLockScreen) {
                                    onDisallowOnLockScreen()
                                    viewModel.onAllowOnLockScreenChange(false)
                                } else {
                                    onAllowOnLockScreen()
                                    viewModel.onAllowOnLockScreenChange(true)
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}