// Archivo: MainActivity.kt (¡Código actualizado!)
package com.example.medinotis
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Importa esta clase
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medinotis.ui.theme.MedinotisTheme

// ... (Las constantes BASE_FONT_SIZE y MAX_FONT_SCALE_FACTOR se quedan igual)
private val BASE_FONT_SIZE = 16.sp
private const val MAX_FONT_SCALE_FACTOR = 1.8f

class MainActivity : ComponentActivity() {

    // Obtenemos la instancia del ViewModel
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedinotisTheme {
                // Recolectamos el valor del StateFlow del ViewModel.
                // `collectAsState` convierte el Flow en un State que Compose puede usar.
                val textScaleFactor by settingsViewModel.textScaleFactor.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaRegistroMedicamentos(
                        // Pasamos el valor actual y la función para actualizarlo
                        currentScaleFactor = textScaleFactor,
                        onScaleFactorChange = { newFactor ->
                            settingsViewModel.updateTextScaleFactor(newFactor)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaRegistroMedicamentos(
    currentScaleFactor: Float,
    onScaleFactorChange: (Float) -> Unit
) {
    var medicamento by rememberSaveable { mutableStateOf("") }
    var dia by rememberSaveable { mutableStateOf("") }
    var hora by rememberSaveable { mutableStateOf("") }
    // Ya no necesitamos un estado para el factor de escala aquí,
    // porque nos lo pasan desde la MainActivity.

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Registrar Recordatorio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize * currentScaleFactor
                ),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tamaño del Texto: ${(currentScaleFactor * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = MaterialTheme.typography.titleSmall.fontSize * currentScaleFactor
                    )
                )
                Slider(
                    value = currentScaleFactor,
                    onValueChange = onScaleFactorChange, // ¡Ahora llamamos a la función del ViewModel!
                    valueRange = 1.0f..MAX_FONT_SCALE_FACTOR,
                    steps = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ... (Los campos de texto y el botón se quedan igual, pero usando `currentScaleFactor`)
            CampoDeTextoAccesible(
                valor = medicamento,
                alCambiar = { medicamento = it },
                etiqueta = "Nombre del Medicamento",
                placeholder = "Ej: Paracetamol 500mg",
                textScaleFactor = currentScaleFactor
            )

            CampoDeTextoAccesible(
                valor = dia,
                alCambiar = { dia = it },
                etiqueta = "Día(s) de la toma",
                placeholder = "Ej: Lunes, Miércoles, Viernes",
                textScaleFactor = currentScaleFactor
            )

            CampoDeTextoAccesible(
                valor = hora,
                alCambiar = { hora = it },
                etiqueta = "Hora de la toma",
                placeholder = "Ej: 8:00 AM",
                textScaleFactor = currentScaleFactor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Lógica de guardado */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    text = "Guardar Recordatorio",
                    fontSize = BASE_FONT_SIZE * currentScaleFactor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ... El Composable `CampoDeTextoAccesible` y el Preview se quedan exactamente igual.
@Composable
fun CampoDeTextoAccesible(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    placeholder: String,
    textScaleFactor: Float = 1.0f
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = MaterialTheme.typography.titleMedium.fontSize * textScaleFactor
            ),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
        TextField(
            value = valor,
            onValueChange = alCambiar,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = BASE_FONT_SIZE * textScaleFactor
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = BASE_FONT_SIZE * textScaleFactor),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}