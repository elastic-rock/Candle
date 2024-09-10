package com.elasticrock.candle.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elasticrock.candle.data.preferences.PreferencesRepository
import com.elasticrock.candle.util.maxBrightness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainScreenState(
    val hue: Float = 0f,
    val lightness: Float = 1f,
    val brightness: Float = maxBrightness,

    val hue0: Float = 1f,
    val lightness0: Float = 1f,
    val brightness0: Float = maxBrightness,

    val hue1: Float = 0.5f,
    val lightness1: Float = 0f,
    val brightness1: Float = maxBrightness,

    val hue2: Float = 0.5f,
    val lightness2: Float = 0.75f,
    val brightness2: Float = maxBrightness,

    val isCandleEffectRunning: Boolean = false,

    val keepScreenOn: Boolean = false,
    val allowOnLockScreen: Boolean = false,

    val isLocked: Boolean = false
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
): ViewModel() {
    private val _hue = MutableStateFlow(0f)
    private val _lightness = MutableStateFlow(1f)
    private val _brightness = MutableStateFlow(maxBrightness)

    private val _hue0 = preferencesRepository.hue0
    private val _lightness0 = preferencesRepository.lightness0
    private val _brightness0 = preferencesRepository.brightness0

    private val _hue1 = preferencesRepository.hue1
    private val _lightness1 = preferencesRepository.lightness1
    private val _brightness1 = preferencesRepository.brightness1

    private val _hue2 = preferencesRepository.hue2
    private val _lightness2 = preferencesRepository.lightness2
    private val _brightness2 = preferencesRepository.brightness2

    private val _isCandleEffectRunning = MutableStateFlow(false)

    private val _keepScreenOn = preferencesRepository.keepScreenOn
    private val _allowOnLockScreen = preferencesRepository.lockScreenAllowed

    private val _isLocked = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _hue.value = preferencesRepository.previousHue.first()
            _lightness.value = preferencesRepository.previousLightness.first()
            _brightness.value = preferencesRepository.previousBrightness.first()
        }
    }

    private val _state = MutableStateFlow(MainScreenState())
    val state = combine(
        _state,
        _hue,
        _lightness,
        _brightness,

        _hue0,
        _lightness0,
        _brightness0,

        _hue1,
        _lightness1,
        _brightness1,
        _hue2,

        _lightness2,
        _brightness2,

        _isCandleEffectRunning,

        _keepScreenOn,
        _allowOnLockScreen,

        _isLocked
    ) { flowValues ->
        val state = flowValues[0] as MainScreenState
        val hue = flowValues[1] as Float
        val lightness = flowValues[2] as Float
        val brightness = flowValues[3] as Float

        val hue0 = flowValues[4] as Float
        val lightness0 = flowValues[5] as Float
        val brightness0 = flowValues[6] as Float

        val hue1 = flowValues[7] as Float
        val lightness1 = flowValues[8] as Float
        val brightness1 = flowValues[9] as Float

        val hue2 = flowValues[10] as Float
        val lightness2 = flowValues[11] as Float
        val brightness2 = flowValues[12] as Float

        val isCandleEffectRunning = flowValues[13] as Boolean

        val keepScreenOn = flowValues[14] as Boolean
        val allowOnLockScreen = flowValues[15] as Boolean

        val isLocked = flowValues[16] as Boolean

        state.copy(
            hue = hue,
            lightness = lightness,
            brightness = brightness,

            hue0 = hue0,
            lightness0 = lightness0,
            brightness0 = brightness0,

            hue1 = hue1,
            lightness1 = lightness1,
            brightness1 = brightness1,

            hue2 = hue2,
            lightness2 = lightness2,
            brightness2 = brightness2,

            isCandleEffectRunning = isCandleEffectRunning,

            keepScreenOn = keepScreenOn,
            allowOnLockScreen = allowOnLockScreen,

            isLocked = isLocked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenState())

    fun onHueChange(hue: Float) {
        _hue.value = hue
    }

    fun onHueSave() {
        viewModelScope.launch {
            preferencesRepository.savePreviousHue(_hue.value)
        }
    }

    fun onLightnessChange(lightness: Float) {
        _lightness.value = lightness
    }

    fun onLightnessSave() {
        viewModelScope.launch {
            preferencesRepository.savePreviousLightness(_lightness.value)
        }
    }

    fun onBrightnessChange(brightness: Float) {
        _brightness.value = brightness
    }

    fun onBrightnessSave() {
        viewModelScope.launch {
            preferencesRepository.savePreviousBrightness(_brightness.value)
        }
    }

    fun onLockChange(locked: Boolean) {
        _isLocked.value = locked
    }

    fun savePreset(hue: Float, lightness: Float, brightness: Float, preset: Int) {
        viewModelScope.launch {
            when (preset) {
                0 -> {
                    preferencesRepository.saveHue0(hue)
                    preferencesRepository.saveLightness0(lightness)
                    preferencesRepository.saveBrightness0(brightness)
                }
                1 -> {
                    preferencesRepository.saveHue1(hue)
                    preferencesRepository.saveLightness1(lightness)
                    preferencesRepository.saveBrightness1(brightness)
                }
                2 -> {
                    preferencesRepository.saveHue2(hue)
                    preferencesRepository.saveLightness2(lightness)
                    preferencesRepository.saveBrightness2(brightness)
                }
                else -> {
                    Log.e("MainScreenViewModel","This preset number does not exist and, therefore, cannot be saved")
                }
            }
        }
    }

    fun loadPreset(preset: Int) {
        viewModelScope.launch {
            when (preset) {
                0 -> {
                    _hue.value = _hue0.first()
                    _lightness.value = _lightness0.first()
                    _brightness.value = _brightness0.first()
                    preferencesRepository.savePreviousHue(_hue0.first())
                    preferencesRepository.savePreviousLightness(_lightness0.first())
                    preferencesRepository.savePreviousBrightness(_brightness0.first())
                }
                1 -> {
                    _hue.value = _hue1.first()
                    _lightness.value = _lightness1.first()
                    _brightness.value = _brightness1.first()
                    preferencesRepository.savePreviousHue(_hue1.first())
                    preferencesRepository.savePreviousLightness(_lightness1.first())
                    preferencesRepository.savePreviousBrightness(_brightness1.first())
                }
                2 -> {
                    _hue.value = _hue2.first()
                    _lightness.value = _lightness2.first()
                    _brightness.value = _brightness2.first()
                    preferencesRepository.savePreviousHue(_hue2.first())
                    preferencesRepository.savePreviousLightness(_lightness2.first())
                    preferencesRepository.savePreviousBrightness(_brightness2.first())
                }
                else -> {
                    Log.e("MainScreenViewModel","This preset number does not exist and, therefore, cannot be loaded")
                }
            }
        }
    }

    private var _candleJob: Job = viewModelScope.launch(start = CoroutineStart.LAZY) {

        val updateIntervalMillis: Long = 200
        val lightnessRandomRange = 0.15f

        while (true) {
            val lightnessRandom = (Math.random() * lightnessRandomRange).toFloat()

            val hue = 30f
            val lightness = 0.4f + lightnessRandom

            _hue.value = hue
            _lightness.value = lightness.coerceIn(0f, 1f)

            delay(updateIntervalMillis)
            Log.e("MainScreenViewModel","candleJob Run")
        }
    }

    fun startCandleEffect() {
        _candleJob.start()
        _isCandleEffectRunning.update { true }
        Log.e("MainScreenViewModel","startCandleEffect()")
    }

    fun stopCandleEffect() {
        _candleJob.cancel()
        _isCandleEffectRunning.update { false }
        Log.e("MainScreenViewModel","stopCandleEffect()")
    }

    override fun onCleared() {
        super.onCleared()
        stopCandleEffect()
    }
}