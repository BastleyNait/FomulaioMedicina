// Archivo: SettingsDataStore.kt
package com.example.medinotis

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creamos una extensión para el Context que nos dará una única instancia de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    // Creamos una "llave" para identificar nuestro valor. Es como el nombre de la variable que guardaremos.
    companion object {
        val TEXT_SCALE_FACTOR_KEY = floatPreferencesKey("text_scale_factor")
    }

    // Función para guardar el factor de escala
    suspend fun saveTextScaleFactor(scaleFactor: Float) {
        context.dataStore.edit { settings ->
            settings[TEXT_SCALE_FACTOR_KEY] = scaleFactor
        }
    }

    // Creamos un Flow para leer el valor. Flow permite que la UI "escuche" los cambios automáticamente.
    val textScaleFactor: Flow<Float> = context.dataStore.data
        .map { preferences ->
            // Si el valor no existe todavía, devolvemos 1.0f (tamaño normal) como valor por defecto.
            preferences[TEXT_SCALE_FACTOR_KEY] ?: 1.0f
        }
}