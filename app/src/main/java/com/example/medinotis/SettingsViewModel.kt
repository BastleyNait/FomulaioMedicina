// Archivo: SettingsViewModel.kt
package com.example.medinotis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia de nuestro gestor de DataStore
    private val settingsDataStore = SettingsDataStore(application)

    // Exponemos el valor del factor de escala como un StateFlow.
    // La UI observará este StateFlow para obtener el valor actualizado.
    val textScaleFactor: StateFlow<Float> = settingsDataStore.textScaleFactor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Inicia cuando la UI está visible
            initialValue = 1.0f // Valor inicial mientras se carga el dato real
        )

    // Función que la UI llamará para actualizar el valor
    fun updateTextScaleFactor(newScaleFactor: Float) {
        viewModelScope.launch {
            settingsDataStore.saveTextScaleFactor(newScaleFactor)
        }
    }
}