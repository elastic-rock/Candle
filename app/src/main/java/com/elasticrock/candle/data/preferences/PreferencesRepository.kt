package com.elasticrock.candle.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.elasticrock.candle.util.maxBrightness
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private val tag = "PreferenceRepository"

    private val previousBrightnessKey = floatPreferencesKey("previousBrightness")
    private val previousHueKey = floatPreferencesKey("previousHue")
    private val previousLightnessKey = floatPreferencesKey("previousLightness")
    private val keepScreenOnKey = booleanPreferencesKey("keepscreenon")
    private val lockScreenAllowedKey = booleanPreferencesKey("lockScreenAllowed")
    private val hue0key = floatPreferencesKey("hue0")
    private val lightness0key = floatPreferencesKey("lightness0")
    private val brightness0key = floatPreferencesKey("brightness0")
    private val hue1key = floatPreferencesKey("hue1")
    private val lightness1key = floatPreferencesKey("lightness1")
    private val brightness1key = floatPreferencesKey("brightness1")
    private val hue2key = floatPreferencesKey("hue2")
    private val lightness2key = floatPreferencesKey("lightness2")
    private val brightness2key = floatPreferencesKey("brightness2")


    suspend fun saveLockScreenAllowed(lockScreenAllowed: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[lockScreenAllowedKey] = lockScreenAllowed
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing lock screen allowed setting")
        }
    }

    suspend fun saveKeepScreenOn(keepScreenOn: Boolean) {
        try {
            dataStore.edit { preferences ->
                preferences[keepScreenOnKey] = keepScreenOn
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing keep screen on setting")
        }
    }

    suspend fun savePreviousBrightness(previousBrightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[previousBrightnessKey] = previousBrightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing previous brightness")
        }
    }

    suspend fun savePreviousHue(previousHue: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[previousHueKey] = previousHue
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing previous hue")
        }
    }

    suspend fun savePreviousLightness(previousLightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[previousLightnessKey] = previousLightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing previous lightness")
        }
    }

    suspend fun saveHue0(hue: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[hue0key] = hue
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing hue0")
        }
    }

    suspend fun saveLightness0(lightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[lightness0key] = lightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing lightness0")
        }
    }

    suspend fun saveBrightness0(brightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[brightness0key] = brightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing brightness0")
        }
    }

    suspend fun saveHue1(hue: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[hue1key] = hue
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing hue1")
        }
    }

    suspend fun saveLightness1(lightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[lightness1key] = lightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing lightness1")
        }
    }

    suspend fun saveBrightness1(brightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[brightness1key] = brightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing brightness1")
        }
    }

    suspend fun saveHue2(hue: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[hue2key] = hue
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing hue2")
        }
    }

    suspend fun saveLightness2(lightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[lightness2key] = lightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing lightness2")
        }
    }

    suspend fun saveBrightness2(brightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[brightness2key] = brightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing brightness2")
        }
    }

    val hue0: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[hue0key] ?: 1f
        }

    val hue1: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[hue1key] ?: 0.5f
        }

    val hue2: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[hue2key] ?: 0.5f
        }

    val lightness0: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[lightness0key] ?: 1f
        }

    val lightness1: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[lightness1key] ?: 0f
        }

    val lightness2: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[lightness2key] ?: 0.75f
        }

    val brightness0: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[brightness0key] ?: maxBrightness
        }

    val brightness1: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[brightness1key] ?: maxBrightness
        }

    val brightness2: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[brightness2key] ?: maxBrightness
        }

    val previousBrightness: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[previousBrightnessKey] ?: maxBrightness
        }

    val previousHue: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[previousHueKey] ?: 0f
        }

    val previousLightness: Flow<Float> = dataStore.data
        .map { preferences ->
            preferences[previousLightnessKey] ?: 1f
        }

    val keepScreenOn: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[keepScreenOnKey] ?: true
        }

    val lockScreenAllowed: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[lockScreenAllowedKey] ?: true
        }
}