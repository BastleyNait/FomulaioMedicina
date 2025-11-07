# CURSO: INTRODUCCIÓN AL DESARROLLO DE NUEVAS PLATAFORMAS

## Docente
Ernesto Suárez

## Integrantes
- Sebastián Arley Chirinos Negrón
- Rodrigo Estéfano Viza Cuti

---

# MediNotis: Prototipo de App Accesible

MediNotis es un prototipo de aplicación Android, construido con Kotlin y Jetpack Compose, diseñado para registrar recordatorios de medicamentos. Su foco principal es la accesibilidad (A11y): permite a los usuarios ajustar dinámicamente el tamaño del texto de toda la UI (100%–180%) y recuerda esta preferencia de forma persistente.

## Resumen

- Registro básico de medicamento, día y hora.
- Slider de accesibilidad que escala tipografías globalmente.
- Persistencia de preferencia de tamaño de texto con Jetpack DataStore.
- Arquitectura MVVM + flujo de datos unidireccional (UDF) con StateFlow.

## Características Principales

- UI clara con objetivos táctiles grandes y etiquetas externas.
- Escalado de texto consistente en títulos, campos y botones.
- Estado desacoplado y observable con `Flow`/`StateFlow`.
- Configuración guardada en disco, no se pierde al cerrar la app.

## Stack Tecnológico

- Lenguaje: `Kotlin`.
- UI: `Jetpack Compose` + `Material 3`.
- Arquitectura: `MVVM` y `UDF`.
- Persistencia: `Jetpack DataStore (Preferences)`.
- Asincronía y estado: `Coroutines` y `Flow`/`StateFlow`.

## Requisitos

- Android Studio más reciente (Koala/Iguana).
- JDK `11` (configurado en Gradle del proyecto).
- `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`.

## Instalación y Ejecución

- Abrir la carpeta del proyecto en Android Studio y ejecutar `Run` en un emulador o dispositivo físico.
- Por consola en Windows:
  - `.\gradlew.bat assembleDebug` compila el APK.
  - `.\gradlew.bat installDebug` instala en un dispositivo conectado.

## Estructura del Proyecto

```
app/src/main/java/com/example/medinotis/
├── MainActivity.kt
├── SettingsDataStore.kt
├── SettingsViewModel.kt
├── data/               # reservado para futuras fuentes de datos
└── ui/theme/           # theming de Compose (Material 3)
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt

app/src/main/res/
├── values/ (colors, strings, themes)
└── xml/ (backup_rules, data_extraction_rules)
```

## Arquitectura (MVVM + UDF)

- Estado fluye hacia abajo: ViewModel ➜ Composables.
- Eventos suben: Composables ➜ ViewModel.
- `SettingsDataStore` es la única fuente de verdad para la preferencia `text_scale_factor`.

## Código Clave

**Persistencia con DataStore (`SettingsDataStore.kt`)**

```kotlin
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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val TEXT_SCALE_FACTOR_KEY = floatPreferencesKey("text_scale_factor")
    }

    suspend fun saveTextScaleFactor(scaleFactor: Float) {
        context.dataStore.edit { settings ->
            settings[TEXT_SCALE_FACTOR_KEY] = scaleFactor
        }
    }

    val textScaleFactor: Flow<Float> = context.dataStore.data
        .map { preferences -> preferences[TEXT_SCALE_FACTOR_KEY] ?: 1.0f }
}
```

**ViewModel (`SettingsViewModel.kt`)**

```kotlin
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
    private val settingsDataStore = SettingsDataStore(application)

    val textScaleFactor: StateFlow<Float> = settingsDataStore.textScaleFactor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

    fun updateTextScaleFactor(newScaleFactor: Float) {
        viewModelScope.launch { settingsDataStore.saveTextScaleFactor(newScaleFactor) }
    }
}
```

**UI (`MainActivity.kt` y Composables)**

```kotlin
// Archivo: MainActivity.kt
package com.example.medinotis
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp

private val BASE_FONT_SIZE = 16.sp
private const val MAX_FONT_SCALE_FACTOR = 1.8f

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val textScaleFactor by settingsViewModel.textScaleFactor.collectAsState()
            Surface { PantallaRegistroMedicamentos(
                currentScaleFactor = textScaleFactor,
                onScaleFactorChange = { settingsViewModel.updateTextScaleFactor(it) }
            ) }
        }
    }
}

@Composable
fun PantallaRegistroMedicamentos(
    currentScaleFactor: Float,
    onScaleFactorChange: (Float) -> Unit
) {
    Text(
        text = "Tamaño del Texto: ${(currentScaleFactor * 100).toInt()}%",
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = MaterialTheme.typography.titleSmall.fontSize * currentScaleFactor
        )
    )
    Slider(
        value = currentScaleFactor,
        onValueChange = onScaleFactorChange,
        valueRange = 1.0f..MAX_FONT_SCALE_FACTOR,
        steps = 5
    )
}
```

**Campo de texto accesible**

```kotlin
@Composable
fun CampoDeTextoAccesible(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    placeholder: String,
    textScaleFactor: Float = 1.0f
) {
    Text(
        text = etiqueta,
        style = MaterialTheme.typography.titleMedium.copy(
            fontSize = MaterialTheme.typography.titleMedium.fontSize * textScaleFactor
        )
    )
    TextField(
        value = valor,
        onValueChange = alCambiar,
        placeholder = { Text(text = placeholder, fontSize = BASE_FONT_SIZE * textScaleFactor) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = BASE_FONT_SIZE * textScaleFactor)
    )
}
```

## Slider de Accesibilidad

- Componente: `Slider` de Material 3.
- Estado: `currentScaleFactor` desde `SettingsViewModel` (observado con `collectAsState()`).
- Rango: `1.0f..1.8f` (ajustable mediante `MAX_FONT_SCALE_FACTOR`).
- Pasos: `steps = 5` para puntos intermedios accesibles.
- Persistencia: `onValueChange = onScaleFactorChange` que llama a `updateTextScaleFactor(it)` en el ViewModel y guarda en DataStore.
- Uso en tipografías: multiplicar `fontSize * currentScaleFactor` en `Text`, `TextField` y botones.

```kotlin
Slider(
    value = currentScaleFactor,
    onValueChange = onScaleFactorChange,
    valueRange = 1.0f..MAX_FONT_SCALE_FACTOR,
    steps = 5
)
```

## DataStore (Preferences) – Detalle

- Extensión: `val Context.dataStore by preferencesDataStore(name = "settings")`.
- Clave: `TEXT_SCALE_FACTOR_KEY = floatPreferencesKey("text_scale_factor")`.
- Lectura: `context.dataStore.data.map { it[TEXT_SCALE_FACTOR_KEY] ?: 1.0f }`.
- Escritura: `context.dataStore.edit { it[TEXT_SCALE_FACTOR_KEY] = scaleFactor }`.
- Integración con ViewModel: `stateIn(viewModelScope, WhileSubscribed(5000), 1.0f)` para exponer `StateFlow<Float>`.

## Accesibilidad (A11y)

- Escalado de texto consistente en toda la UI mediante `currentScaleFactor`.
- Etiquetas visibles y externas a los campos para evitar ambigüedad.
- Controles con tamaño mínimo recomendado (botón alto y `padding` generoso).
- Layout espaciado (`Arrangement.spacedBy`) y soporte de `verticalScroll`.
- Preparado para lectores de pantalla: estructura clara y semántica de Compose.

## Configuración y Parámetros Clave

- `BASE_FONT_SIZE = 16.sp`: tamaño base del cuerpo de texto.
- `MAX_FONT_SCALE_FACTOR = 1.8f`: límite superior del slider.
- `TEXT_SCALE_FACTOR_KEY = "text_scale_factor"`: clave de preferencia en DataStore.
- Valor por defecto: `1.0f` (100%).

Para ajustar la escala máxima:

```kotlin
private const val MAX_FONT_SCALE_FACTOR = 2.0f // ejemplo
```

## Dependencias y Versiones

- AGP `8.13.0`, Kotlin `2.0.21`.
- Compose BOM `2024.09.00`, Material 3.
- `androidx.datastore:datastore-preferences:1.1.1`.
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0`.
- `androidx.lifecycle:lifecycle-runtime-ktx:2.8.0`.

## Pruebas y Validación

- Usar Previews de Compose para verificar escalado de texto.
- Prueba manual moviendo el slider y reiniciando la app; el ajuste persiste.
- Opcional: `.\gradlew.bat connectedAndroidTest` para tests de instrumentación si se agregan.

## Guía de Desarrollo

- Añadir nuevas pantallas como Composables sin estado, recibiendo `currentScaleFactor`.
- Mantener estado global en ViewModel y persistir con DataStore cuando aplique.
- Propagar estado hacia abajo y eventos hacia arriba (UDF).
- Reutilizar `CampoDeTextoAccesible` para consistencia.

## Roadmap

- Lista de recordatorios con almacenamiento local (Room/SQLite).
- Notificaciones y alarmas (WorkManager/AlarmManager).
- Internacionalización (ES/EN) y soporte de dark mode.
- Tests unitarios y de UI (JUnit/Espresso/Compose Testing).

## Solución de Problemas

- Preferencia no se guarda: confirmar que `updateTextScaleFactor` llama a `saveTextScaleFactor` dentro de `viewModelScope`.
- Estado no se actualiza: asegurar `collectAsState()` vive dentro de `setContent` y composables.
- Rendimiento: usar `WhileSubscribed(5000)` para pausar recolección en background.

## Créditos

- Curso: Introducción al Desarrollo de Nuevas Plataformas.
- Docente: Ernesto Suárez.
- Integrantes: Sebastián Arley Chirinos Negrón, Rodrigo Estéfano Viza Cuti.