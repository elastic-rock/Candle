package com.elasticrock.candle.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elasticrock.candle.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsScreenState(
    val keepScreenOn: Boolean = false,
    val allowOnLockScreen: Boolean = false,
    val swipeToAdjustBrightness: Boolean = true,
)

@HiltViewModel
class SettingsScreenViewModel@Inject constructor(
    private val preferencesRepository: PreferencesRepository
): ViewModel() {
    private val _keepScreenOn = preferencesRepository.keepScreenOn
    private val _allowOnLockScreen = preferencesRepository.lockScreenAllowed
    private val _swipeToAdjustBrightness = preferencesRepository.swipeToAdjustBrightness

    private val _state = MutableStateFlow(SettingsScreenState())
    val state = combine(_state, _keepScreenOn, _allowOnLockScreen, _swipeToAdjustBrightness) { state, keepScreenOn, allowOnLockScreen, swipeToAdjustBrightness ->

        state.copy(
            keepScreenOn = keepScreenOn,
            allowOnLockScreen = allowOnLockScreen,
            swipeToAdjustBrightness = swipeToAdjustBrightness
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsScreenState())

    fun onKeepScreenOnChange(keepScreenOn: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveKeepScreenOn(keepScreenOn)
        }
    }

    fun onAllowOnLockScreenChange(allowOnLockScreen: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveLockScreenAllowed(allowOnLockScreen)
        }
    }

    fun onSwipeToAdjustBrightnessChange(swipeToAdjustBrightness: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveSwipeToAdjustBrightness(swipeToAdjustBrightness)
        }
    }
}