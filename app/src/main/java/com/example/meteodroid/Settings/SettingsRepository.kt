package com.example.meteodroid.Settings

import android.content.Context
import androidx.compose.runtime.key
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository @Inject constructor (private val context: Context) {
    private val KEY_UNIT = stringPreferencesKey("temperature_unit")

    val unitFlow = context.dataStore.data.map {
        it[KEY_UNIT]?.let {
            runCatching {
                TemperatureUnit.valueOf(it)
            }.getOrNull()
        } ?: TemperatureUnit.CELSIUS
    }

    suspend fun setUnit(unit: TemperatureUnit) {
        context.dataStore.edit { it[KEY_UNIT] = unit.name }
    }
}