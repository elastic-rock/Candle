package com.elasticrock.candle.ui.settings

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elasticrock.candle.R
import com.elasticrock.candle.ui.components.AboutItem
import com.elasticrock.candle.ui.components.PreferenceSubtitle
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

    val context = LocalContext.current
    val clipboard = getSystemService(context, ClipboardManager::class.java) as ClipboardManager

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

                item {
                    PreferenceSubtitle(text = stringResource(R.string.about))
                }

                item {
                    AboutItem(
                        title = stringResource(id = R.string.author),
                        subtitle = stringResource(id = R.string.david_weis)
                    )
                }

                item {
                    val url = "https://github.com/elastic-rock/Candle"
                    val intent = Intent(Intent.ACTION_VIEW)
                    AboutItem(
                        title = stringResource(id = R.string.source_code),
                        subtitle = stringResource(id = R.string.github),
                        onClick = {
                            intent.data = Uri.parse(url)
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    val appId = "com.elasticrock.candle"
                    AboutItem(
                        title = stringResource(id = R.string.application_id),
                        subtitle = appId,
                        onClick = {
                            val clip: ClipData = ClipData.newPlainText("simple text", appId)
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                }

                item {
                    fun getAppVersion(context: Context): String {
                        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        return packageInfo.versionName!!
                    }

                    val version = getAppVersion(context)

                    AboutItem(
                        title = stringResource(id = R.string.version),
                        subtitle = version,
                        onClick = {
                            val clip: ClipData = ClipData.newPlainText("simple text", version)
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                }

                item {
                    val url = "https://gnu.org/licenses/gpl-3.0.txt"
                    val intent = Intent(Intent.ACTION_VIEW)
                    AboutItem(
                        title = stringResource(id = R.string.license),
                        subtitle = "GPL-3.0",
                        onClick = {
                            intent.data = Uri.parse(url)
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    AboutItem(
                        title = stringResource(id = R.string.third_party_licenses),
                        subtitle = stringResource(id = R.string.third_party_licenses_description),
                        onClick = onLicensesOptionClick
                    )
                }
            }
        }
    )
}