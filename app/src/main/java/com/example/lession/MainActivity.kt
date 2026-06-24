package com.example.lession

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lession.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * LESSION - Aplicación Deportiva Profesional
 * Prevención y monitoreo de lesiones mediante simulación bio-realista.
 */

// --- MODELOS DE DATOS ---

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
cd 'C:\Users\proalafalda\AndroidStudioProjects\LESSION2'
git branch -M main
git remote add origin https://github.com/TU_USUARIO/Lession2026.git
git push -u origin main    FUERZA("Entr. Fuerza", 105, 150, 4.2f, 0.14f, 5.5f, Icons.Default.FitnessCenter, 30f)
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

// --- VIEWMODEL ---

class LessionViewModel : ViewModel() {
    var state by mutableStateOf(SimulacionState())
        private set

    init {
        iniciarSimulacion()
    }

    private fun iniciarSimulacion() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                actualizarEstado()
            }
        }
    }

    private fun actualizarEstado() {
        if (state.lesion != null) return

        val act = state.actividadActual
        
        // 1. FRECUENCIA CARDÍACA
        val targetHR = act.hrMax.toFloat()
        val deltaHR = targetHR - state.hr
        val factorHR = if (act == TipoActividad.DESCANSO) 0.2f else 0.08f
        val stepHR = (deltaHR * factorHR).coerceIn(-3.0f, 4.0f)
        val nuevoHR = (state.hr + stepHR).coerceIn(60f, 205f)

        // 2. ENERGÍA
        val factorEnergia = if (act == TipoActividad.DESCANSO) -1.5f else (act.consumoEnergia * (nuevoHR / 80f)) * 0.3f
        val nuevaEnergia = (state.energia - factorEnergia).coerceIn(0f, 100f)

        // 3. TEMPERATURA
        val factorTemp = if (act == TipoActividad.DESCANSO) -0.06f else (act.aumentoTemp * (nuevoHR / 110f)) * 0.5f
        val nuevaTemp = (state.temperatura + factorTemp).coerceIn(36.5f, 42.5f)

        // 4. MÚSCULOS
        val nuevosMusculos = state.musculos.toMutableMap()
        val musculosActivos = obtenerMusculosPorActividad(act)
        
        nuevosMusculos.forEach { (nombre, mState) ->
            var cargaNueva = mState.carga
            var fatigaNueva = mState.fatiga

            if (musculosActivos.contains(nombre)) {
                cargaNueva = (cargaNueva + act.impactoMuscular * 0.9f).coerceIn(0f, 100f)
                fatigaNueva = (fatigaNueva + (cargaNueva * 0.028f)).coerceIn(0f, 100f)
            } else {
                cargaNueva = (cargaNueva - 2.5f).coerceIn(0f, 100f)
                fatigaNueva = (fatigaNueva - 1.5f).coerceIn(0f, 100f)
            }
            nuevosMusculos[nombre] = mState.copy(carga = cargaNueva, fatiga = fatigaNueva)
        }

        val nuevaFatigaG = nuevosMusculos.values.map { it.fatiga }.average().toFloat()

        // 5. SISTEMA DE LESIONES
        var lesionNueva: String? = null
        if (nuevoHR > 170 && nuevaFatigaG > 80f && nuevaTemp > 39.5f) {
            val candidato = nuevosMusculos.values.filter { it.fatiga > 85f }.randomOrNull()
            if (candidato != null && (0..100).random() < 22) { 
                lesionNueva = candidato.nombre
            }
        }

        // 6. HISTORIALES
        val maxPoints = 40
        val hrHist = (state.hrHistory + nuevoHR).takeLast(maxPoints)
        val enHist = (state.energyHistory + nuevaEnergia).takeLast(maxPoints)
        val teHist = (state.tempHistory + nuevaTemp).takeLast(maxPoints)
        val faHist = (state.fatigueHistory + nuevaFatigaG).takeLast(maxPoints)

        state = state.copy(
            hr = nuevoHR,
            energia = nuevaEnergia,
            temperatura = nuevaTemp,
            musculos = nuevosMusculos,
            fatigaGeneral = nuevaFatigaG,
            lesion = lesionNueva,
            tiempoActividad = state.tiempoActividad + 1,
            hrHistory = hrHist,
            energyHistory = enHist,
            tempHistory = teHist,
            fatigueHistory = faHist
        )
    }

    private fun obtenerMusculosPorActividad(act: TipoActividad): List<String> = when (act) {
        TipoActividad.CAMINAR -> listOf("Cuádriceps", "Gemelos", "Tibiales")
        TipoActividad.TROTAR -> listOf("Cuádriceps", "Isquiotibiales", "Gemelos")
        TipoActividad.CORRER -> listOf("Cuádriceps", "Gemelos", "Glúteos")
        TipoActividad.SPRINTAR -> listOf("Cuádriceps", "Gemelos", "Isquiotibiales", "Glúteos")
        TipoActividad.SALTAR -> listOf("Rodillas", "Gemelos", "Tobillos")
        TipoActividad.SENTADILLAS -> listOf("Cuádriceps", "Glúteos", "Isquiotibiales")
        TipoActividad.ESTOCADAS -> listOf("Cuádriceps", "Glúteos")
        TipoActividad.FUTBOL -> listOf("Cuádriceps", "Gemelos", "Rodillas", "Tobillos", "Tibiales")
        TipoActividad.BASQUET -> listOf("Gemelos", "Rodillas", "Hombros", "Tobillos")
        TipoActividad.CICLISMO -> listOf("Cuádriceps", "Gemelos", "Isquiotibiales")
        TipoActividad.FUERZA -> listOf("Bíceps", "Tríceps", "Hombros", "Cuádriceps")
        else -> emptyList()
    }

    fun cambiarActividad(nueva: TipoActividad) {
        if (state.lesion != null) return
        state = state.copy(actividadActual = nueva)
    }

    fun iniciarRecuperacion() {
        state = state.copy(
            lesion = null,
            actividadActual = TipoActividad.DESCANSO
        )
    }

    fun alternarVista() {
        state = state.copy(esFrontal = !state.esFrontal)
    }
}

// --- UI PRINCIPAL ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LESSIONTheme {
                LessionApp()
            }
        }
    }
}

@Composable
fun LessionApp(viewModel: LessionViewModel = viewModel()) {
    val state = viewModel.state

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LessionViolet
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HeaderLession()
            
            Row(modifier = Modifier.weight(1f)) {
                // LADO IZQUIERDO: SIMULADOR
                Box(modifier = Modifier.weight(1.2f)) {
                    SimuladorContainer(state, viewModel)
                }
                
                // LADO DERECHO: PANEL DE MÉTRICAS
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    PanelMetricas(state)
                    PanelMusculos(state)
                }
            }

            // SELECTOR DE ACTIVIDADES
            SelectorActividades(state, viewModel)
        }

        // DIÁLOGO DE LESIÓN
        if (state.lesion != null) {
            OverlayLesion(state.lesion, viewModel)
        }
    }
}

@Composable
fun HeaderLession() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = LessionYellow, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            "LESSION",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            color = LessionYellow,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "MONITOREO ACTIVO", 
                color = LessionViolet, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun SimuladorContainer(state: SimulacionState, viewModel: LessionViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(LessionVioletLight)
            .border(2.dp, Color.White.copy(0.08f), RoundedCornerShape(40.dp))
    ) {
        // LIENZO DEL AVATAR
        LessionAvatarCanvas(
            state = state,
            modifier = Modifier.fillMaxSize()
        )

        // CONTROL DE CÁMARA
        IconButton(
            onClick = { viewModel.alternarVista() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .background(Color.Black.copy(0.4f), CircleShape)
        ) {
            Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = Color.White)
        }

        // INDICADOR DE ACTIVIDAD ACTUAL
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            color = Color.Black.copy(0.8f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (state.lesion != null) Icons.Default.Warning else state.actividadActual.icon, 
                    contentDescription = null, 
                    tint = if (state.lesion != null) LessionRed else LessionYellow, 
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    if (state.lesion != null) "LESIÓN DETECTADA" else state.actividadActual.nombre.uppercase(),
                    color = if (state.lesion != null) LessionRed else LessionYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun LessionAvatarCanvas(state: SimulacionState, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    
    // FLUJO ENERGÉTICO ANIMADO
    val flowSpeed = (state.hr / 50f).coerceIn(1f, 12f)
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween((14000 / flowSpeed).toInt(), easing = LinearEasing)
        ), label = "energy"
    )

    // MOVIMIENTO BIO-MECÁNICO
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (state.actividadActual == TipoActividad.DESCANSO) 4000 else 600),
            repeatMode = RepeatMode.Restart
        ), label = "move"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val s = h / 450f

        val isInjured = state.lesion != null
        val amp = if (isInjured) 0.04f else state.actividadActual.amplitudMov * s
        val legY = sin(time) * amp
        val armY = -sin(time) * amp

        val bodyColor = LessionVioletAccent

        // 1. DIBUJO DEL CUERPO HUMANO ESTILIZADO
        val bodyPath = Path().apply {
            // Cabeza
            addOval(Rect(cx - 24 * s, cy - 195 * s, cx + 24 * s, cy - 147 * s)) 
            // Cuello
            moveTo(cx - 10 * s, cy - 147 * s)
            lineTo(cx + 10 * s, cy - 147 * s)
            lineTo(cx + 14 * s, cy - 137 * s)
            lineTo(cx - 14 * s, cy - 137 * s)
            close()
            // Torso
            moveTo(cx - 42 * s, cy - 137 * s)
            lineTo(cx + 42 * s, cy - 137 * s)
            lineTo(cx + 36 * s, cy - 15 * s)
            lineTo(cx - 36 * s, cy - 15 * s)
            close()
        }
        drawPath(bodyPath, bodyColor)

        // 2. EXTREMIDADES DINÁMICAS
        // Brazos
        var leftArmOffset = armY
        var rightArmOffset = -armY
        
        // Animación de dolor
        if (isInjured) {
            when (state.lesion) {
                "Bíceps", "Hombros", "Tríceps" -> leftArmOffset = 30 * s
                "Cuádriceps", "Rodillas" -> leftArmOffset = 110 * s
            }
        }

        drawRoundRect(bodyColor, Offset(cx - 75 * s, cy - 135 * s + leftArmOffset), Size(28 * s, 115 * s), CornerRadius(14 * s))
        drawRoundRect(bodyColor, Offset(cx + 47 * s, cy - 135 * s + rightArmOffset), Size(28 * s, 115 * s), CornerRadius(14 * s))

        // Piernas
        var leftLegOffset = legY
        var rightLegOffset = -legY
        if (isInjured) {
            leftLegOffset = 10 * s
            rightLegOffset = 10 * s
        }

        drawRoundRect(bodyColor, Offset(cx - 42 * s, cy - 5 * s + leftLegOffset), Size(36 * s, 200 * s), CornerRadius(18 * s))
        drawRoundRect(bodyColor, Offset(cx + 6 * s, cy - 5 * s + rightLegOffset), Size(36 * s, 200 * s), CornerRadius(18 * s))

        // 3. SISTEMA ENERGÉTICO
        val flowColor = when {
            state.hr > 170 -> LessionRed
            state.hr > 140 -> LessionOrange
            state.hr > 100 -> LessionYellow
            else -> Color(0xFF00E5FF)
        }
        val flowStroke = Stroke(width = 5f * s, pathEffect = PathEffect.dashPathEffect(floatArrayOf(60f, 120f), flowOffset))
        drawPath(bodyPath, flowColor, style = flowStroke)

        // 4. MAPEO DE MÚSCULOS
        state.musculos.forEach { (name, m) ->
            val intensity = m.fatiga / 100f
            if (intensity > 0.05f || state.lesion == name) {
                val colorM = when {
                    state.lesion == name || m.fatiga > 85f -> LessionRed
                    m.fatiga > 70f -> LessionOrange
                    m.fatiga > 45f -> LessionYellow
                    else -> Color.Green.copy(0.6f)
                }

                val mPos = when (name) {
                    "Cuádriceps" -> if (state.esFrontal) Offset(cx - 24 * s, cy + 70 * s + leftLegOffset) else null
                    "Gemelos" -> if (!state.esFrontal) Offset(cx - 24 * s, cy + 155 * s + leftLegOffset) else null
                    "Tibiales" -> if (state.esFrontal) Offset(cx - 24 * s, cy + 155 * s + leftLegOffset) else null
                    "Isquiotibiales" -> if (!state.esFrontal) Offset(cx - 24 * s, cy + 85 * s + leftLegOffset) else null
                    "Glúteos" -> if (!state.esFrontal) Offset(cx, cy + 35 * s) else null
                    "Hombros" -> Offset(cx + 60 * s, cy - 125 * s + rightArmOffset)
                    "Bíceps" -> if (state.esFrontal) Offset(cx - 60 * s, cy - 85 * s + leftArmOffset) else null
                    "Tríceps" -> if (!state.esFrontal) Offset(cx - 60 * s, cy - 85 * s + leftArmOffset) else null
                    "Rodillas" -> if (state.esFrontal) Offset(cx - 24 * s, cy + 115 * s + leftLegOffset) else null
                    "Tobillos" -> Offset(cx - 24 * s, cy + 190 * s + leftLegOffset)
                    else -> null
                }

                mPos?.let {
                    val radius = (16 + 16 * intensity) * s
                    drawCircle(colorM, radius, it, alpha = 0.3f + 0.7f * intensity)
                    if (state.lesion == name) {
                        val pSize = abs(sin(time * 5)) * 30 * s
                        drawCircle(LessionRed, radius + pSize, it, style = Stroke(6f * s))
                    }
                }
            }
        }
        
        // EXPRESIÓN DE DOLOR
        if (isInjured) {
            drawCircle(LessionRed, 7 * s, Offset(cx - 12 * s, cy - 180 * s))
            drawCircle(LessionRed, 7 * s, Offset(cx + 12 * s, cy - 180 * s))
            drawLine(LessionRed, Offset(cx - 15 * s, cy - 165 * s), Offset(cx + 15 * s, cy - 165 * s), strokeWidth = 4 * s)
        }
    }
}

@Composable
fun PanelMetricas(state: SimulacionState) {
    Column {
        TarjetaMetrica("FRECUENCIA CARDÍACA", "${state.hr.toInt()} BPM", state.hr / 200f, LessionYellow, state.hrHistory, 200f)
        TarjetaMetrica("ENERGÍA CORPORAL", "${state.energia.toInt()}%", state.energia / 100f, LessionEnergy, state.energyHistory, 100f)
        TarjetaMetrica("TEMPERATURA", "${"%.1f".format(state.temperatura)}°C", (state.temperatura - 36) / 7f, LessionTemp, state.tempHistory, 43f, 36f)
        TarjetaMetrica("FATIGA MUSCULAR", "${state.fatigaGeneral.toInt()}%", state.fatigaGeneral / 100f, LessionFatigue, state.fatigueHistory, 100f)
    }
}

@Composable
fun TarjetaMetrica(label: String, value: String, progress: Float, color: Color, history: List<Float>, max: Float, min: Float = 0f) {
    Card(
        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LessionVioletLight),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, color.copy(0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(14.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(60.dp).drawBehind {
                if (history.size > 1) {
                    val p = Path()
                    val dx = size.width / (history.size - 1)
                    val range = max - min
                    history.forEachIndexed { i, v ->
                        val x = i * dx
                        val y = size.height - ((v - min) / range * size.height)
                        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    drawPath(p, color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                    p.lineTo(size.width, size.height)
                    p.lineTo(0f, size.height)
                    p.close()
                    drawPath(p, Brush.verticalGradient(listOf(color.copy(0.4f), Color.Transparent)))
                }
            })

            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = color,
                trackColor = Color.White.copy(0.06f)
            )
        }
    }
}

@Composable
fun PanelMusculos(state: SimulacionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LessionVioletLight),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text("CARGA POR GRUPO MUSCULAR", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(20.dp))
            
            val activos = state.musculos.values.filter { it.carga > 0.5f }.sortedByDescending { it.carga }
            if (activos.isEmpty()) {
                Text("Cuerpo en estado de reposo absoluto", color = Color.White.copy(0.35f), fontSize = 12.sp)
            } else {
                activos.take(6).forEach { m ->
                    Column(modifier = Modifier.padding(vertical = 7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(m.nombre, color = Color.White.copy(0.85f), fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("${m.carga.toInt()}%", color = LessionYellow, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { m.carga / 100f },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                            color = if (m.carga > 80) LessionRed else LessionYellow.copy(0.85f),
                            trackColor = Color.White.copy(0.06f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectorActividades(state: SimulacionState, viewModel: LessionViewModel) {
    Surface(
        color = LessionVioletLight,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
    ) {
        Column(modifier = Modifier.padding(top = 24.dp, bottom = 36.dp)) {
            Text(
                "SELECCIONAR ACTIVIDAD FÍSICA",
                color = Color.White.copy(0.45f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 36.dp, bottom = 18.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(TipoActividad.values()) { act ->
                    ActividadItem(
                        act = act,
                        isSelected = state.actividadActual == act,
                        onClick = { viewModel.cambiarActividad(act) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActividadItem(act: TipoActividad, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "scale")
    
    Column(
        modifier = Modifier
            .width(120.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(28.dp))
            .background(if (isSelected) LessionYellow else LessionVioletAccent)
            .clickable { onClick() }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            act.icon, 
            contentDescription = null, 
            tint = if (isSelected) LessionViolet else Color.White,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            act.nombre,
            color = if (isSelected) LessionViolet else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun OverlayLesion(musculo: String, viewModel: LessionViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.95f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LessionViolet),
            shape = RoundedCornerShape(48.dp),
            modifier = Modifier.padding(24.dp).border(3.dp, LessionRed, RoundedCornerShape(48.dp))
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(110.dp).background(LessionRed.copy(0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = LessionRed, modifier = Modifier.size(70.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("¡LESIÓN DETECTADA!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    "Se ha producido una rotura o distensión crítica en: $musculo.\n\nEl sistema ha bloqueado la actividad física. Inicie el protocolo de recuperación inmediata.",
                    color = Color.White.copy(0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { viewModel.iniciarRecuperacion() },
                    colors = ButtonDefaults.buttonColors(containerColor = LessionYellow),
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text("INICIAR RECUPERACIÓN", color = LessionViolet, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LessionPreview() {
    LESSIONTheme {
        LessionApp()
    }
}
