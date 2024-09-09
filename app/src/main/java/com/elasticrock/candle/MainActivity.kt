package com.elasticrock.candle

import android.app.Activity
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
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elasticrock.candle.ui.main.MainScreen
import com.elasticrock.candle.ui.settings.SettingsScreen
import com.elasticrock.candle.ui.theme.CandleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CandleTheme {
                TorchApp(this)
            }
        }

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
}

@Composable
fun TorchApp(activity: ComponentActivity) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable("main") {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onKeepScreenOn = {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                },
                onAllowOnLockScreen = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        activity.setShowWhenLocked(true)
                    }
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackArrowClick = {
                    navController.navigateUp()
                },
                onLicensesOptionClick = {

                },
                onEnableKeepScreenOn = {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                },
                onDisableKeepScreenOn = {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                },
                onAllowOnLockScreen = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        activity.setShowWhenLocked(true)
                    }
                },
                onDisallowOnLockScreen = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        activity.setShowWhenLocked(false)
                    }
                }
            )
        }
    }
}