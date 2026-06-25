package com.example.lession.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector

enum class TipoActividad(
    val nombre: String,
    val hrMin: Int,
    val hrMax: Int,
    val consumoEnergia: Float,
    val aumentoTemp: Float,
    val impactoMuscular: Float,
    val icon: ImageVector,
    val amplitudMov: Float
) {
    DESCANSO("Descanso", 60, 75, -1.8f, -0.06f, -3.0f, Icons.Default.HolidayVillage, 0f),
    CAMINAR("Caminar", 75, 95, 0.5f, 0.02f, 0.8f, Icons.Default.Directions, 14f),
    TROTAR("Trotar", 95, 120, 1.4f, 0.05f, 1.6f, Icons.Default.DirectionsWalk, 24f),
    CORRER("Correr", 120, 150, 3.2f, 0.11f, 3.0f, Icons.Default.DirectionsWalk, 42f),
    SPRINTAR("Sprintar", 150, 190, 7.5f, 0.28f, 6.5f, Icons.Default.Bolt, 70f),
    SALTAR("Saltar", 130, 165, 4.8f, 0.18f, 5.2f, Icons.Default.Favorite, 55f),
    SENTADILLAS("Sentadillas", 110, 145, 4.2f, 0.13f, 5.8f, Icons.Default.CropSquare, 38f),
    ESTOCADAS("Estocadas", 100, 135, 3.6f, 0.10f, 4.8f, Icons.Default.LinearScale, 36f),
    FUTBOL("Futbol", 130, 180, 5.5f, 0.24f, 4.5f, Icons.Default.SportsSoccer, 60f),
    BASQUET("Basquet", 125, 175, 5.2f, 0.22f, 4.2f, Icons.Default.SportsBasketball, 55f),
    CICLISMO("Ciclismo", 115, 160, 4.0f, 0.16f, 3.2f, Icons.Default.DriveEta, 18f),
    FUERZA("Entr. Fuerza", 105, 150, 4.2f, 0.14f, 5.5f, Icons.Default.FitnessCenter, 30f)
}

data class EstadoMusculo(
    val nombre: String,
    val carga: Float = 0f,
    val fatiga: Float = 0f
)

data class SimulacionState(
    val actividadActual: TipoActividad = TipoActividad.DESCANSO,
    val hr: Float = 68f,
    val energia: Float = 100f,
    val temperatura: Float = 36.6f,
    val fatigaGeneral: Float = 0f,
    val esFrontal: Boolean = true,
    val musculos: Map<String, EstadoMusculo> = mapOf(
        "Cuadriceps" to EstadoMusculo("Cuadriceps"),
        "Gemelos" to EstadoMusculo("Gemelos"),
        "Isquiotibiales" to EstadoMusculo("Isquiotibiales"),
        "Gluteos" to EstadoMusculo("Gluteos"),
        "Hombros" to EstadoMusculo("Hombros"),
        "Biceps" to EstadoMusculo("Biceps"),
        "Triceps" to EstadoMusculo("Triceps"),
        "Rodillas" to EstadoMusculo("Rodillas"),
        "Tobillos" to EstadoMusculo("Tobillos"),
        "Tibiales" to EstadoMusculo("Tibiales")
    ),
    val lesion: String? = null,
    val alertaSobrecarga: String? = null,
    val tiempoActividad: Int = 0,
    val hrHistory: List<Float> = emptyList(),
    val energyHistory: List<Float> = emptyList(),
    val tempHistory: List<Float> = emptyList(),
    val fatigueHistory: List<Float> = emptyList()
)
