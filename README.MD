# CURSO: INTRODUCCIÓN AL DESARROLLO DE NUEVAS PLATAFORMAS

## Docente
Ernesto Suárez

## Integrantes
* Sebastián Arley Chirinos Negrón
* Rodrigo Estéfano Viza Cuti

---

# MediNotis: Prototipo de App Accesible

MediNotis es un prototipo de aplicación para Android, construida con tecnologías modernas, diseñada para el registro de recordatorios de medicamentos. Su objetivo principal es demostrar un fuerte enfoque en la **accesibilidad (A11y)**, permitiendo a los usuarios (especialmente adultos mayores) ajustar dinámicamente la interfaz a sus necesidades visuales.



## 🚀 Características Principales

* **Formulario Simple:** Un formulario claro para registrar el nombre del medicamento, el día y la hora.
* **Slider de Accesibilidad:** Un control deslizante que permite al usuario escalar dinámicamente todo el tamaño del texto de la UI (de 100% a 180%) para una mejor legibilidad.
* **Persistencia de Ajustes:** La app **recuerda** la configuración de tamaño de texto preferida por el usuario. Esta preferencia se guarda de forma persistente incluso si la aplicación se cierra por completo.
* **UI Accesible:** Diseño con objetivos táctiles grandes (botones y campos de texto), etiquetas claras fuera de los campos y un layout espaciado para evitar confusiones.

---

## 🏛️ Arquitectura y Explicación Detallada del Código

Este proyecto utiliza un stack moderno de Android basado en la arquitectura **MVVM (Model-View-ViewModel)** y un **flujo de datos unidireccional (UDF)**.

### 1. Stack Tecnológico

* **Lenguaje:** **Kotlin** 100%.
* **UI:** **Jetpack Compose** (Material 3) para una interfaz de usuario declarativa.
* **Arquitectura:** **MVVM (Model-View-ViewModel)**.
* **Persistencia:** **Jetpack DataStore (Preferences)** para guardar datos clave-valor de forma asíncrona.
* **Asincronía:** **Coroutines** y **Flow** de Kotlin para manejar el estado y las operaciones de fondo.

### 2. Estructura de Archivos y Clases (Detalle)

A continuación, se detalla el rol de cada archivo y clase clave en el proyecto:

#### 📄 `build.gradle.kts (Módulo app)`

Es el archivo de configuración de dependencias. Las adiciones clave fueron:

* `androidx.datastore:datastore-preferences:1.1.1`: Esta es la biblioteca de **DataStore**. Se eligió sobre `SharedPreferences` porque es **asíncrona**, utiliza Coroutines y Flow, y no bloquea el Hilo Principal (UI Thread), previniendo errores "ANR" (Application Not Responding).
* `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0`: Permite integrar un `ViewModel` de forma nativa con Jetpack Compose, usando la función `viewModel()`.
* `androidx.lifecycle:lifecycle-runtime-ktx:2.8.0`: Proporciona `collectAsState()` para convertir un `Flow` en un `State` que Compose pueda observar.

---

#### 📄 `SettingsDataStore.kt` (Capa de Datos)

Esta clase es la **única fuente de verdad** para la configuración guardada. Aísla toda la lógica de lectura y escritura del resto de la app.

* `Context.dataStore`: Es una extensión que crea una **instancia única (singleton)** de DataStore llamada `"settings"`.
* `TEXT_SCALE_FACTOR_KEY`: Es una `floatPreferencesKey("text_scale_factor")`. Funciona como una llave segura para identificar nuestro dato. Evita errores de escritura (ej. "text_scale" vs "text_size").
* `val textScaleFactor: Flow<Float>`: Esta es la función de **lectura**.
    * Expone un `Flow`, que es un flujo de datos asíncrono. La UI se "suscribirá" a este flujo.
    * `dataStore.data.map { ... }`: Se activa cada vez que los datos cambian.
    * `preferences[TEXT_SCALE_FACTOR_KEY] ?: 1.0f`: Intenta leer el valor `Float`. Si no existe (ej. la primera vez que se abre la app), devuelve un valor por defecto de `1.0f` (100%).
* `suspend fun saveTextScaleFactor(scaleFactor: Float)`: Esta es la función de **escritura**.
    * Es una `suspend fun` (función de suspensión) porque la escritura en disco es una operación lenta que no debe bloquear la UI.
    * `dataStore.edit { ... }`: Inicia una transacción de escritura segura en un hilo de fondo.

---

#### 📄 `SettingsViewModel.kt` (Capa de Lógica / ViewModel)

Esta clase actúa como el **"cerebro" de la UI** y el puente entre la Capa de Datos (DataStore) y la Capa de UI (Compose).

* `class SettingsViewModel(application: Application) : AndroidViewModel(application)`: Hereda de `AndroidViewModel` porque necesita el `Context` de la aplicación para poder instanciar `SettingsDataStore`.
* `private val settingsDataStore = SettingsDataStore(application)`: Crea la instancia de nuestro gestor de datos.
* `val textScaleFactor: StateFlow<Float>`: Esta es la propiedad que la UI observará.
    * `settingsDataStore.textScaleFactor.stateIn(...)`: Convierte el `Flow` "frío" de DataStore en un `StateFlow` "caliente".
    * `viewModelScope`: Asegura que la corutina que escucha a DataStore viva solo mientras el ViewModel esté vivo.
    * `SharingStarted.WhileSubscribed(5000)`: Optimización que le dice a DataStore que deje de escuchar si la app pasa a segundo plano por más de 5 segundos.
* `fun updateTextScaleFactor(newScaleFactor: Float)`: Esta es la función que la UI llama cuando el usuario mueve el slider.
    * `viewModelScope.launch { ... }`: Inicia una nueva corutina en el `viewModelScope` para realizar el trabajo de guardado.
    * `settingsDataStore.saveTextScaleFactor(newScaleFactor)`: Llama a la `suspend fun` de DataStore de forma segura, sin bloquear la UI.

---

#### 📄 `MainActivity.kt` (Capa de UI / Vista)

Esta es la Actividad principal que aloja nuestra interfaz de Jetpack Compose.

* `private val settingsViewModel: SettingsViewModel by viewModels()`: Esta línea **inyecta** la instancia del ViewModel en la Actividad. `by viewModels()` se encarga de crear el ViewModel o de proveer el existente si la pantalla rota (sobrevive a cambios de configuración).
* `val textScaleFactor by settingsViewModel.textScaleFactor.collectAsState()`: Esta es la línea clave que conecta la UI con el ViewModel.
    * `collectAsState()` se suscribe al `StateFlow` del ViewModel.
    * Cada vez que el valor en el `StateFlow` cambia, Compose **automáticamente "recompone"** (redibuja) solo las partes de la UI que dependen de `textScaleFactor`.
* `PantallaRegistroMedicamentos(currentScaleFactor = textScaleFactor, ...)`: Aquí se aplica el **Flujo de Datos Unidireccional**:
    1.  El estado (`textScaleFactor`) fluye **hacia abajo** (de ViewModel a Composable).
    2.  Los eventos (`onScaleFactorChange`) fluyen **hacia arriba** (de Composable a ViewModel).

#### 📄 `Composables` (Ej. `PantallaRegistroMedicamentos` y `CampoDeTextoAccesible`)

Estos son los bloques de construcción de la UI.

* `PantallaRegistroMedicamentos(currentScaleFactor: Float, onScaleFactorChange: (Float) -> Unit)`:
    * Este Composable es **"Stateless" (sin estado)**. No tiene su propia variable `textScaleFactor`; recibe el estado desde arriba. Esto lo hace más predecible, reutilizable y fácil de probar.
    * Pasa el `currentScaleFactor` a todos los `Text` y `CampoDeTextoAccesible` para aplicar el escalado.
    * Contiene el `Slider` que, en su `onValueChange`, llama a la función `onScaleFactorChange(newValue)`.
* `CampoDeTextoAccesible(...)`:
    * Es un Composable **reutilizable** creado para no repetir código (principio DRY - Don't Repeat Yourself).
    * Define un estándar de accesibilidad (etiqueta `Text` grande y visible fuera del `TextField`) que se aplica a todos los campos de texto del formulario.