package com.example.lession.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo de datos para tipos de actividades físicas
 * Define características fisiológicas de cada actividad
 */
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
    DESCANSO("Descanso", 60, 75, -1.8f, -0.06f, -3.0f, Icons.Default.Bedtime, 0f),
    CAMINAR("Caminar", 75, 95, 0.5f, 0.02f, 0.8f, Icons.Default.DirectionsWalk, 14f),
    TROTAR("Trotar", 95, 120, 1.4f, 0.05f, 1.6f, Icons.Default.DirectionsRun, 24f),
    CORRER("Correr", 120, 150, 3.2f, 0.11f, 3.0f, Icons.Default.DirectionsRun, 42f),
    SPRINTAR("Sprintar", 150, 190, 7.5f, 0.28f, 6.5f, Icons.Default.FlashOn, 70f),
    SALTAR("Saltar", 130, 165, 4.8f, 0.18f, 5.2f, Icons.Default.ArrowUpward, 55f),
    SENTADILLAS("Sentadillas", 110, 145, 4.2f, 0.13f, 5.8f, Icons.Default.Accessibility, 38f),
    ESTOCADAS("Estocadas", 100, 135, 3.6f, 0.10f, 4.8f, Icons.Default.AccessibilityNew, 36f),
    FUTBOL("Fútbol", 130, 180, 5.5f, 0.24f, 4.5f, Icons.Default.SportsSoccer, 60f),
    BASQUET("Básquet", 125, 175, 5.2f, 0.22f, 4.2f, Icons.Default.SportsBasketball, 55f),
    CICLISMO("Ciclismo", 115, 160, 4.0f, 0.16f, 3.2f, Icons.Default.DirectionsBike, 18f),
    FUERZA("Entr. Fuerza", 105, 150, 4.2f, 0.14f, 5.5f, Icons.Default.FitnessCenter, 30f)
}

/**
 * Estado de un grupo muscular individual
 */
data class EstadoMusculo(
    val nombre: String,
    val carga: Float = 0f,
    val fatiga: Float = 0f
)

/**
 * Estado completo de la simulación
 */
data class SimulacionState(
    val actividadActual: TipoActividad = TipoActividad.DESCANSO,
    val hr: Float = 68f,
    val energia: Float = 100f,
    val temperatura: Float = 36.6f,
    val fatigaGeneral: Float = 0f,
    val esFrontal: Boolean = true,
    val musculos: Map<String, EstadoMusculo> = mapOf(
        "Cuádriceps" to EstadoMusculo("Cuádriceps"),
        "Gemelos" to EstadoMusculo("Gemelos"),
        "Isquiotibiales" to EstadoMusculo("Isquiotibiales"),
        "Glúteos" to EstadoMusculo("Glúteos"),
        "Hombros" to EstadoMusculo("Hombros"),
        "Bíceps" to EstadoMusculo("Bíceps"),
        "Tríceps" to EstadoMusculo("Tríceps"),
        "Rodillas" to EstadoMusculo("Rodillas"),
        "Tobillos" to EstadoMusculo("Tobillos"),
        "Tibiales" to EstadoMusculo("Tibiales")
    ),
    val lesion: String? = null,
    val tiempoActividad: Int = 0,
    val hrHistory: List<Float> = emptyList(),
    val energyHistory: List<Float> = emptyList(),
    val tempHistory: List<Float> = emptyList(),
    val fatigueHistory: List<Float> = emptyList()
)

