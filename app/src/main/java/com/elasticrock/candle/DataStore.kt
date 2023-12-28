package com.elasticrock.candle

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStore(private val dataStore: DataStore<Preferences>) {

    private val previousBrightnessKey = floatPreferencesKey("previousBrightness")
    private val previousHueKey = floatPreferencesKey("previousHue")
    private val previousLightnessKey = floatPreferencesKey("previousLightness")

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

    suspend fun savePreset(hue: Float, lightness: Float, brightness: Float, preference: Int) {
        try {
            dataStore.edit { preferences -> preferences[floatPreferencesKey("hue$preference")] = hue }
            dataStore.edit { preferences -> preferences[floatPreferencesKey("lightness$preference")] = lightness }
            dataStore.edit { preferences -> preferences[floatPreferencesKey("brightness$preference")] = brightness }
        } catch (e: IOException) {
            Log.e(tag,"Error writing preset$preference")
        }
    }

    suspend fun readPreset(preference: Int): Triple<Float, Float, Float> {
        if (preference > 2) {
            throw Exception("preference not in range")
        }
        val hue: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[floatPreferencesKey("hue$preference")] ?: when (preference) {
                    0 -> 1f
                    1 -> 0.5f
                    2 -> 0.5f
                    else -> 1f
                }
            }
        val lightness: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[floatPreferencesKey("lightness$preference")] ?: when (preference) {
                    0 -> 1f
                    1 -> 0f
                    2 -> 0.75f
                    else -> 1f
                }
            }
        val brightness: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[floatPreferencesKey("brightness$preference")] ?: maxBrightness
            }
        return Triple(hue.first(), lightness.first(), brightness.first())
    }

    suspend fun readPreviousBrightness() : Float {
        val previousBrightness: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[previousBrightnessKey] ?: maxBrightness
            }
        return previousBrightness.first()
    }

    suspend fun readPreviousHue() : Float {
        val previousHue: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[previousHueKey] ?: 0f
            }
        return previousHue.first()
    }

    suspend fun readPreviousLightness() : Float {
        val previousLightness: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[previousLightnessKey] ?: 1f
            }
        return previousLightness.first()
    }
}