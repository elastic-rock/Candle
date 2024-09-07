package com.elasticrock.candle

import android.app.Activity
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elasticrock.candle.ui.main.MainScreen
import com.elasticrock.candle.ui.settings.SettingsScreen
import com.elasticrock.candle.ui.theme.CandleTheme
import com.elasticrock.candle.util.setBrightness
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking { setBrightness(window, com.elasticrock.candle.data.preferences.PreferencesRepository(
            dataStore
        ).readPreviousBrightness()) }

        enableEdgeToEdge()
        setContent {
            CandleTheme {
                TorchApp(dataStore, this)
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

@Composable
fun TorchApp(dataStore: DataStore<Preferences>, activity: ComponentActivity) {
    val view = LocalView.current
    val window = (view.context as Activity).window
    var keepScreenOn by remember { mutableStateOf(runBlocking { com.elasticrock.candle.data.preferences.PreferencesRepository(
        dataStore
    ).readKeepScreenOn() }) }
    val scope = rememberCoroutineScope()
    val onKeepScreenOnPreferenceChange = {
        if (!keepScreenOn) {
            keepScreenOn = true
            scope.launch { com.elasticrock.candle.data.preferences.PreferencesRepository(dataStore).saveKeepScreenOn(true) }

        } else {
            keepScreenOn = false
            scope.launch { com.elasticrock.candle.data.preferences.PreferencesRepository(dataStore).saveKeepScreenOn(false) }
        }
    }
    var allowOnLockScreen by remember { mutableStateOf(runBlocking { com.elasticrock.candle.data.preferences.PreferencesRepository(
        dataStore
    ).readLockScreenAllowed() }) }
    val onAllowOnLockScreenPreferenceChange = {
        if (!allowOnLockScreen) {
            allowOnLockScreen = true
            scope.launch { com.elasticrock.candle.data.preferences.PreferencesRepository(dataStore).saveLockScreenAllowed(true) }

        } else {
            allowOnLockScreen = false
            scope.launch { com.elasticrock.candle.data.preferences.PreferencesRepository(dataStore).saveLockScreenAllowed(false) }
        }
    }

    if (keepScreenOn) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        if (allowOnLockScreen) {
            activity.setShowWhenLocked(true)
        } else {
            activity.setShowWhenLocked(false)
        }
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
        composable("main") { MainScreen(dataStore, navController) }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                onKeepScreenOnPreferenceChange = { onKeepScreenOnPreferenceChange() },
                keepScreenOn = keepScreenOn,
                allowOnLockScreen = allowOnLockScreen,
                onAllowOnLockScreenPreferenceChange = { onAllowOnLockScreenPreferenceChange() }
            )
        }
    }
}