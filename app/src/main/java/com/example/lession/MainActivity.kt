package com.example.lession

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BluetoothConnected
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.lession.data.EstadoMusculo
import com.example.lession.data.SimulacionState
import com.example.lession.data.TipoActividad
import com.example.lession.presentation.LessionViewModel
import com.example.lession.ui.theme.*
import kotlin.math.*

class MainActivity : ComponentActivity() {
    private val viewModel: LessionViewModel by lazy {
        ViewModelProvider(this)[LessionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LESSIONTheme {
                LessionApp(viewModel)
            }
        }
    }
}

@Composable
fun LessionApp(viewModel: LessionViewModel) {
    val state = viewModel.state
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 760

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SportyBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HeaderLession()
                
                if (compact) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        SimuladorContainer(state, viewModel, Modifier.fillMaxWidth().height(420.dp))
                        PanelMetricas(state)
                        PanelMusculos(state)
                        Spacer(Modifier.height(130.dp))
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SimuladorContainer(state, viewModel, Modifier.weight(1.3f).fillMaxHeight())
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            PanelMetricas(state)
                            PanelMusculos(state)
                            Spacer(Modifier.height(130.dp))
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                PanelInferior(state, viewModel)
            }

            AnimatedVisibility(
                visible = state.alertaSobrecarga != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                state.alertaSobrecarga?.let { AlertaSobrecarga(it) }
            }

            if (state.lesion != null) {
                OverlayLesion(state.lesion, viewModel)
            }
        }
    }
}

@Composable
fun HeaderLession() {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "LESSION",
                color = SportyWhite,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
            )
            Text(
                "PRO PERFORMANCE MONITOR",
                color = SportyRed,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
        }
        Spacer(Modifier.weight(1f))
        
        IconButton(
            onClick = { },
            modifier = Modifier
                .background(SportySurface, CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Outlined.Notifications, "Alertas", tint = SportyWhite, modifier = Modifier.size(20.dp))
        }
        
        Spacer(Modifier.width(12.dp))
        
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .background(SportyRed, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.MoreVert, "Menú", tint = SportyWhite, modifier = Modifier.size(20.dp))
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .background(SportySurface)
                    .border(1.dp, SportyGreyDark, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("Historial de Actividad", color = SportyWhite) },
                    onClick = { showMenu = false },
                    leadingIcon = { Icon(Icons.Default.History, null, tint = SportyRed) }
                )
                DropdownMenuItem(
                    text = { Text("Dispositivos", color = SportyWhite) },
                    onClick = { showMenu = false },
                    leadingIcon = { Icon(Icons.Outlined.BluetoothConnected, null, tint = SportyRed) }
                )
                HorizontalDivider(color = SportyGreyDark, thickness = 0.5.dp)
                DropdownMenuItem(
                    text = { Text("Configuración", color = SportyWhite) },
                    onClick = { showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Settings, null, tint = SportyGrey) }
                )
            }
        }
    }
}

@Composable
fun SimuladorContainer(state: SimulacionState, viewModel: LessionViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SportyDeepGrey, SportyBlack)
                )
            )
            .border(1.dp, SportyGreyDark, RoundedCornerShape(32.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridStep = 40.dp.toPx()
            for (x in 0 until (size.width / gridStep).toInt() + 1) {
                drawLine(SportyGreyDark.copy(0.15f), Offset(x * gridStep, 0f), Offset(x * gridStep, size.height))
            }
            for (y in 0 until (size.height / gridStep).toInt() + 1) {
                drawLine(SportyGreyDark.copy(0.15f), Offset(0f, y * gridStep), Offset(size.width, y * gridStep))
            }
        }

        LessionAvatarCanvas(state = state, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        ) {
            Surface(
                onClick = { viewModel.alternarVista() },
                color = SportySurface.copy(0.7f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SportyWhite.copy(0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sync, null, tint = SportyWhite, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.esFrontal) "VISTA FRONTAL" else "VISTA POSTERIOR", 
                        color = SportyWhite, 
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SportySurface.copy(0.9f))
                .border(1.dp, if (state.lesion != null) SportyRed else SportyGreyDark, RoundedCornerShape(20.dp))
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isAlert = state.lesion != null || state.alertaSobrecarga != null
                
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (isAlert) SportyRed else Color(0xFF00E676), CircleShape)
                        .shadow(6.dp, CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = if (state.lesion != null) "SISTEMA BLOQUEADO: LESIÓN" 
                           else state.alertaSobrecarga?.let { "ADVERTENCIA: $it" } ?: state.actividadActual.nombre.uppercase(),
                    color = SportyWhite,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
fun LessionAvatarCanvas(state: SimulacionState, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state.actividadActual) {
                    TipoActividad.DESCANSO -> 3600
                    TipoActividad.CAMINAR -> 1500
                    TipoActividad.TROTAR -> 1100
                    TipoActividad.CORRER -> 820
                    TipoActividad.SPRINTAR -> 560
                    TipoActividad.SALTAR -> 1100
                    TipoActividad.SENTADILLAS -> 1700
                    TipoActividad.ESTOCADAS -> 1600
                    TipoActividad.FUTBOL -> 1050
                    TipoActividad.BASQUET -> 1050
                    TipoActividad.CICLISMO -> 880
                    TipoActividad.FUERZA -> 1350
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "activity"
    )
    val flow by infiniteTransition.animateFloat(
        0f,
        1400f,
        infiniteRepeatable(tween((14000 / (state.hr / 50f).coerceIn(1f, 10f)).toInt(), easing = LinearEasing)),
        label = "flow"
    )

    Canvas(modifier = modifier) {
        val s = (minOf(size.width, size.height) / 580f).coerceAtMost(1.2f)
        val act = state.actividadActual
        val sceneTravel = when (act) {
            TipoActividad.CAMINAR -> 28f
            TipoActividad.TROTAR -> 38f
            TipoActividad.CORRER, TipoActividad.FUTBOL -> 48f
            TipoActividad.SPRINTAR -> 58f
            TipoActividad.CICLISMO -> 24f
            else -> 0f
        } * sin(time * 0.5f) * s
        val cx = size.width / 2f + sceneTravel
        val baseCy = size.height / 2f + 10f * s
        
        val breath = if (act == TipoActividad.DESCANSO) sin(time) * 5f * s else sin(time * 2f) * 2f * s
        val squat = if (act == TipoActividad.SENTADILLAS) (1f - cos(time)) * 26f * s else 0f
        val jump = if (act == TipoActividad.SALTAR) -abs(sin(time)) * 58f * s else 0f
        val runBounce = when (act) {
            TipoActividad.CAMINAR -> abs(sin(time)) * 5f
            TipoActividad.TROTAR -> abs(sin(time)) * 8f
            TipoActividad.CORRER, TipoActividad.FUTBOL -> abs(sin(time)) * 11f
            TipoActividad.SPRINTAR -> abs(sin(time)) * 13f
            else -> 0f
        } * s
        val cy = baseCy + breath + squat + jump - runBounce
        val stride = when (act) {
            TipoActividad.CAMINAR -> 16f
            TipoActividad.TROTAR -> 28f
            TipoActividad.CORRER, TipoActividad.FUTBOL -> 42f
            TipoActividad.SPRINTAR -> 62f
            TipoActividad.ESTOCADAS -> 52f
            else -> 0f
        } * sin(time) * s
        val cycling = act == TipoActividad.CICLISMO
        val pedal = if (cycling) 42f * s else 0f
        val arm = when (act) {
            TipoActividad.FUERZA -> -abs(sin(time)) * 42f * s
            TipoActividad.BASQUET -> abs(sin(time)) * 26f * s
            TipoActividad.CICLISMO -> 18f * s
            else -> -stride * 0.7f
        }
        val lean = when (act) {
            TipoActividad.CICLISMO -> 20f * s
            TipoActividad.SPRINTAR -> 12f * s
            TipoActividad.CORRER, TipoActividad.FUTBOL -> 8f * s
            else -> 0f
        }
        
        val bodyColor = SportyGreyDark
        val torsoTop = Offset(cx + lean, cy - 138 * s)
        val torsoBottom = Offset(cx, cy - 22 * s)
        
        val body = Path().apply {
            addOval(Rect(cx + lean - 20 * s, cy - 188 * s, cx + lean + 20 * s, cy - 148 * s))
            moveTo(torsoTop.x - 30 * s, torsoTop.y)
            lineTo(torsoTop.x + 30 * s, torsoTop.y)
            lineTo(torsoBottom.x + 34 * s, torsoBottom.y)
            lineTo(torsoBottom.x - 34 * s, torsoBottom.y)
            close()
        }

        drawPath(body, bodyColor)
        val leftShoulder = Offset(torsoTop.x - 30 * s, torsoTop.y + 12 * s)
        val rightShoulder = Offset(torsoTop.x + 30 * s, torsoTop.y + 12 * s)
        val leftHand = if (cycling) Offset(cx - 62 * s, cy + 88 * s) else Offset(cx - 72 * s, cy - 38 * s + arm)
        val rightHand = if (cycling) Offset(cx + 66 * s, cy + 88 * s) else Offset(cx + 72 * s, cy - 38 * s - arm)
        drawLine(bodyColor, leftShoulder, leftHand, 17 * s, cap = StrokeCap.Round)
        drawLine(bodyColor, rightShoulder, rightHand, 17 * s, cap = StrokeCap.Round)

        val hipLeft = Offset(cx - 22 * s, cy - 16 * s)
        val hipRight = Offset(cx + 22 * s, cy - 16 * s)
        
        if (cycling) {
            val wheelY = cy + 142 * s
            val crank = Offset(cx, cy + 82 * s)
            val leftFoot = Offset(crank.x + cos(time) * pedal, crank.y + sin(time) * pedal)
            val rightFoot = Offset(crank.x + cos(time + PI.toFloat()) * pedal, crank.y + sin(time + PI.toFloat()) * pedal)
            drawLine(SportyRed.copy(0.4f), Offset(cx - 78 * s, wheelY), crank, 5 * s)
            drawLine(SportyRed.copy(0.4f), Offset(cx + 78 * s, wheelY), crank, 5 * s)
            drawCircle(Color.White.copy(0.08f), 48 * s, Offset(cx - 78 * s, wheelY), style = Stroke(4 * s))
            drawCircle(Color.White.copy(0.08f), 48 * s, Offset(cx + 78 * s, wheelY), style = Stroke(4 * s))
            drawLine(bodyColor, hipLeft, leftFoot, 18 * s, cap = StrokeCap.Round)
            drawLine(bodyColor, hipRight, rightFoot, 18 * s, cap = StrokeCap.Round)
        } else {
            val leftFoot = Offset(cx - 34 * s - stride * 0.45f, cy + 166 * s + stride * 0.25f)
            val rightFoot = Offset(cx + 34 * s + stride * 0.45f, cy + 166 * s - stride * 0.25f)
            drawLine(bodyColor, hipLeft, leftFoot, 21 * s, cap = StrokeCap.Round)
            drawLine(bodyColor, hipRight, rightFoot, 21 * s, cap = StrokeCap.Round)
        }

        val pulseColor = when {
            state.hr > 165 -> SportyRed
            state.hr > 130 -> SportyRedLight
            else -> SportyWhite.copy(0.7f)
        }
        drawPath(
            body, 
            pulseColor.copy(alpha = 0.45f), 
            style = Stroke(2.5f * s, pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 70f), flow))
        )

        state.musculos.forEach { (name, muscle) ->
            val affected = state.lesion == name || state.alertaSobrecarga == name
            val intensity = ((muscle.fatiga * 0.45f + muscle.carga * 0.55f) / 100f).coerceIn(0f, 1f)
            val pos = musclePosition(name, cx, cy, s, state.esFrontal)
            if (pos != null && (affected || muscle.carga > 12f)) {
                val color = if (affected) SportyRed else if (muscle.fatiga > 75f) SportyRedLight else SportyRed.copy(0.6f)
                val pulse = if (affected) 1f + abs(sin(time * 4.5f)) * 0.35f else 1f
                val zw = muscleZoneWidth(name) * s * pulse
                val zh = muscleZoneHeight(name) * s * pulse
                drawOval(
                    color = color,
                    topLeft = Offset(pos.x - zw / 2f, pos.y - zh / 2f),
                    size = Size(zw, zh),
                    alpha = if (affected) 0.85f else (0.25f + intensity * 0.5f).coerceIn(0f, 0.75f)
                )
            }
        }
    }
}

private fun musclePosition(name: String, cx: Float, cy: Float, s: Float, frontal: Boolean): Offset? = when (name) {
    "Cuadriceps" -> if (frontal) Offset(cx - 20 * s, cy + 62 * s) else null
    "Gemelos" -> if (!frontal) Offset(cx - 20 * s, cy + 150 * s) else null
    "Tibiales" -> if (frontal) Offset(cx - 20 * s, cy + 152 * s) else null
    "Isquiotibiales" -> if (!frontal) Offset(cx - 20 * s, cy + 84 * s) else null
    "Gluteos" -> if (!frontal) Offset(cx, cy + 28 * s) else null
    "Hombros" -> Offset(cx + 54 * s, cy - 120 * s)
    "Biceps" -> if (frontal) Offset(cx - 55 * s, cy - 80 * s) else null
    "Triceps" -> if (!frontal) Offset(cx - 55 * s, cy - 80 * s) else null
    "Rodillas" -> if (frontal) Offset(cx - 20 * s, cy + 110 * s) else null
    "Tobillos" -> Offset(cx - 20 * s, cy + 178 * s)
    else -> null
}

private fun muscleZoneWidth(name: String): Float = when (name) {
    "Cuadriceps", "Isquiotibiales", "Gemelos" -> 22f
    "Gluteos" -> 46f
    "Hombros" -> 34f
    else -> 18f
}

private fun muscleZoneHeight(name: String): Float = when (name) {
    "Cuadriceps", "Isquiotibiales" -> 56f
    "Gemelos" -> 46f
    "Gluteos" -> 28f
    else -> 22f
}

@Composable
fun PanelMetricas(state: SimulacionState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaMetricaSmall("Ritmo Cardíaco", "${state.hr.toInt()}", "BPM", state.hr / 200f, SportyRed, Icons.Default.Favorite, Modifier.weight(1f))
            TarjetaMetricaSmall("Temp. Corporal", "${"%.1f".format(state.temperatura)}", "°C", (state.temperatura - 36) / 6.5f, LessionTemp, Icons.Default.Thermostat, Modifier.weight(1f))
        }
        TarjetaMetricaLarge("Índice Fatiga Muscular", "${state.fatigaGeneral.toInt()}%", state.fatigaGeneral / 100f, LessionFatigue, state.fatigueHistory, 100f)
        TarjetaMetricaLarge("Energía Metabólica", "${state.energia.toInt()}%", state.energia / 100f, LessionEnergy, state.energyHistory, 100f)
    }
}

@Composable
fun TarjetaMetricaSmall(label: String, value: String, unit: String, progress: Float, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SportySurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, SportyGreyDark)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(label.uppercase(), color = SportyGrey, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = SportyWhite, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
                Spacer(Modifier.width(4.dp))
                Text(unit, color = SportyGrey, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = color,
                trackColor = SportyGreyDark
            )
        }
    }
}

@Composable
fun TarjetaMetricaLarge(label: String, value: String, progress: Float, color: Color, history: List<Float>, max: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SportySurface),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, SportyGreyDark)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(label.uppercase(), color = SportyGrey, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Text(value, color = SportyWhite, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black))
                }
                Box(Modifier.size(52.dp).drawBehind {
                    drawArc(SportyGreyDark, 0f, 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(color, -90f, progress * 360f, false, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                })
            }
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().height(56.dp).drawBehind {
                if (history.size > 1) {
                    val p = Path()
                    val dx = size.width / (history.size - 1)
                    history.forEachIndexed { i, v ->
                        val x = i * dx
                        val y = size.height - (v / max * size.height).coerceIn(0f, size.height)
                        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    drawPath(p, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                    p.lineTo(size.width, size.height)
                    p.lineTo(0f, size.height)
                    p.close()
                    drawPath(p, Brush.verticalGradient(listOf(color.copy(0.2f), Color.Transparent)))
                }
            })
        }
    }
}

@Composable
fun PanelMusculos(state: SimulacionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SportySurface),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, SportyGreyDark)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("ANÁLISIS MUSCULAR EN VIVO", color = SportyWhite, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
            Spacer(Modifier.height(24.dp))
            val activos = state.musculos.values.filter { it.carga > 0.5f }.sortedByDescending { it.carga }
            if (activos.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
                    Text("No se detecta carga. Sistema en recuperación.", color = SportyGrey, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                activos.take(5).forEach { MusculoRow(it) }
            }
        }
    }
}

@Composable
fun MusculoRow(m: EstadoMusculo) {
    val color = when {
        m.fatiga > 82f -> SportyRed
        m.fatiga > 55f -> SportyRedLight
        else -> SportyWhite
    }
    Column(Modifier.padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(m.nombre, color = SportyWhite.copy(0.9f), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("${m.carga.toInt()}%", color = color, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (m.carga / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = color,
            trackColor = SportyGreyDark
        )
    }
}

@Composable
fun PanelInferior(state: SimulacionState, viewModel: LessionViewModel) {
    Surface(
        color = SportySurface.copy(0.98f),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        border = BorderStroke(1.dp, SportyGreyDark)
    ) {
        Column(Modifier.padding(vertical = 24.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(TipoActividad.entries) { act ->
                    ActividadItem(act, state.actividadActual == act) { viewModel.cambiarActividad(act) }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStat("Tiempo Activo", "${state.tiempoActividad}s")
                QuickStat("Nivel Riesgo", riesgoGeneral(state))
                QuickStat("Estado Sistema", estadoFisiologico(state))
                QuickStat("Intensidad", "%.1f".format(state.actividadActual.consumoEnergia.absoluteValue * 2.5f))
            }
        }
    }
}

@Composable
fun ActividadItem(act: TipoActividad, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.08f else 1f, label = "scale")
    val color = if (isSelected) SportyRed else SportyGreyDark
    
    Column(
        modifier = Modifier
            .width(96.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable { onClick() }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            act.icon, 
            null, 
            tint = SportyWhite, 
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            act.nombre, 
            color = SportyWhite, 
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), 
            textAlign = TextAlign.Center, 
            maxLines = 1
        )
    }
}

@Composable
fun QuickStat(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SportyBlack.copy(0.4f))
            .border(1.dp, SportyGreyDark, RoundedCornerShape(14.dp))
            .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 12.dp)
    ) {
        Text(label.uppercase(), color = SportyGrey, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Text(value, color = SportyWhite, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black))
    }
}

private fun riesgoGeneral(state: SimulacionState): String = when {
    state.lesion != null -> "CRÍTICO"
    state.alertaSobrecarga != null -> "ALTO"
    state.fatigaGeneral > 65f -> "MEDIO"
    else -> "BAJO"
}

private fun estadoFisiologico(state: SimulacionState): String = when {
    state.lesion != null -> "FALLO"
    state.actividadActual == TipoActividad.DESCANSO -> "IDLE"
    state.hr > 155f -> "PICO"
    else -> "ACTIVO"
}

@Composable
fun AlertaSobrecarga(musculo: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 110.dp),
            color = SportyRed,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 12.dp
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = SportyWhite, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text(
                    "SOBRECARGA: $musculo. Movimiento inhibido.",
                    color = SportyWhite,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun OverlayLesion(musculo: String, viewModel: LessionViewModel) {
    Box(Modifier.fillMaxSize().background(SportyBlack.copy(0.96f)), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SportySurface),
            shape = RoundedCornerShape(36.dp),
            modifier = Modifier.padding(28.dp).border(2.dp, SportyRed, RoundedCornerShape(36.dp))
        ) {
            Column(Modifier.padding(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Report, null, tint = SportyRed, modifier = Modifier.size(90.dp))
                Spacer(Modifier.height(28.dp))
                Text("FALLO DE SISTEMA", color = SportyWhite, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black))
                Spacer(Modifier.height(18.dp))
                Text(
                    "Daño estructural detectado en: $musculo.\n\nBiomecánica bloqueada para prevenir lesiones crónicas. Iniciar protocolo de recuperación.",
                    color = SportyGrey,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(36.dp))
                Button(
                    onClick = { viewModel.iniciarRecuperacion() },
                    colors = ButtonDefaults.buttonColors(containerColor = SportyRed),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("REINICIAR SISTEMA", color = SportyWhite, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LessionPreview() {
    LESSIONTheme {
        LessionApp(LessionViewModel())
    }
}
