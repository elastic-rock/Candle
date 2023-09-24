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

    suspend fun savePreviousBrightness(previousBrightness: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[previousBrightnessKey] = previousBrightness
            }
        } catch (e: IOException) {
            Log.e(tag,"Error writing previous brightness")
        }
    }

    suspend fun readPreviousBrightness() : Float {
        val previousBrightness: Flow<Float> = dataStore.data
            .map { preferences ->
                preferences[previousBrightnessKey] ?: maxBrightness
            }
        return previousBrightness.first()
    }
}