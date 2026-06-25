package com.example.lession

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.lession.ui.theme.LESSIONTheme
import com.example.lession.ui.theme.LessionEnergy
import com.example.lession.ui.theme.LessionFatigue
import com.example.lession.ui.theme.LessionOrange
import com.example.lession.ui.theme.LessionRed
import com.example.lession.ui.theme.LessionTemp
import com.example.lession.ui.theme.LessionViolet
import com.example.lession.ui.theme.LessionVioletAccent
import com.example.lession.ui.theme.LessionVioletLight
import com.example.lession.ui.theme.LessionYellow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private val viewModel: LessionViewModel by lazy {
        ViewModelProvider(this)[LessionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LESSIONTheme { LessionApp(viewModel) } }
    }
}

@Composable
fun LessionApp(viewModel: LessionViewModel) {
    val state = viewModel.state
    val alertaSobrecarga = state.alertaSobrecarga
    val lesion = state.lesion
    val configuration = LocalConfiguration.current
    val compact = configuration.screenWidthDp < 760

    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = LessionViolet) { innerPadding ->
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
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SimuladorContainer(state, viewModel, Modifier.fillMaxWidth().height(420.dp))
                        PanelMetricas(state)
                        PanelMusculos(state)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SimuladorContainer(state, viewModel, Modifier.weight(1.15f).fillMaxHeight())
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PanelMetricas(state)
                            PanelMusculos(state)
                        }
                    }
                }
                PanelInferior(state, viewModel)
            }

            if (alertaSobrecarga != null) {
                AlertaSobrecarga(alertaSobrecarga)
            }
            if (lesion != null) {
                OverlayLesion(lesion, viewModel)
            }
        }
    }
}

@Composable
fun HeaderLession() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = LessionYellow, modifier = Modifier.size(34.dp))
        Spacer(Modifier.width(12.dp))
        Text("LESSION", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f))
        Text("MONITOREO ACTIVO", color = LessionYellow, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SimuladorContainer(state: SimulacionState, viewModel: LessionViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(LessionVioletLight)
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(28.dp))
    ) {
        LessionAvatarCanvas(state = state, modifier = Modifier.fillMaxSize())
        Surface(
            onClick = { viewModel.alternarVista() },
            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
            color = Color.Black.copy(0.42f),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.esFrontal) "Frente" else "Espalda",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            color = Color.Black.copy(0.75f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                val alert = state.lesion != null || state.alertaSobrecarga != null
                Icon(if (alert) Icons.Default.Info else state.actividadActual.icon, null, tint = if (alert) LessionRed else LessionYellow)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (state.lesion != null) "LESION DETECTADA" else state.alertaSobrecarga?.let { "SOBRECARGA: $it" } ?: state.actividadActual.nombre.uppercase(),
                    color = if (alert) LessionRed else LessionYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    maxLines = 1
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
        val s = (minOf(size.width, size.height) / 520f).coerceAtMost(1.25f)
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
        val baseCy = size.height / 2f + 14f * s
        val injured = state.lesion != null || state.alertaSobrecarga != null

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
        val bodyColor = LessionVioletAccent
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
            drawLine(LessionYellow.copy(0.5f), Offset(cx - 78 * s, wheelY), crank, 5 * s)
            drawLine(LessionYellow.copy(0.5f), Offset(cx + 78 * s, wheelY), crank, 5 * s)
            drawLine(LessionYellow.copy(0.5f), Offset(cx - 78 * s, wheelY), Offset(cx + 78 * s, wheelY), 4 * s)
            drawCircle(Color.White.copy(0.18f), 48 * s, Offset(cx - 78 * s, wheelY), style = Stroke(5 * s))
            drawCircle(Color.White.copy(0.18f), 48 * s, Offset(cx + 78 * s, wheelY), style = Stroke(5 * s))
            drawCircle(LessionYellow.copy(0.75f), 8 * s, crank)
            drawLine(bodyColor, hipLeft, leftFoot, 18 * s, cap = StrokeCap.Round)
            drawLine(bodyColor, hipRight, rightFoot, 18 * s, cap = StrokeCap.Round)
            drawLine(LessionYellow, crank, leftFoot, 4 * s, cap = StrokeCap.Round)
            drawLine(LessionYellow, crank, rightFoot, 4 * s, cap = StrokeCap.Round)
        } else {
            val leftFoot = Offset(cx - 34 * s - stride * 0.45f, cy + 166 * s + stride * 0.25f)
            val rightFoot = Offset(cx + 34 * s + stride * 0.45f, cy + 166 * s - stride * 0.25f)
            drawLine(bodyColor, hipLeft, leftFoot, 21 * s, cap = StrokeCap.Round)
            drawLine(bodyColor, hipRight, rightFoot, 21 * s, cap = StrokeCap.Round)
        }

        if (act == TipoActividad.FUTBOL) {
            val ballX = cx + 96 * s + abs(sin(time)) * 74 * s
            drawCircle(Color.White, 18 * s, Offset(ballX, cy + 158 * s))
            drawCircle(Color.Black.copy(0.45f), 5 * s, Offset(ballX, cy + 158 * s))
        }
        if (act == TipoActividad.BASQUET) {
            val ballY = cy - 35 * s + abs(sin(time)) * 95 * s
            drawCircle(LessionOrange, 18 * s, Offset(cx + 78 * s, ballY))
            drawLine(Color.Black.copy(0.35f), Offset(cx + 60 * s, ballY), Offset(cx + 96 * s, ballY), 2 * s)
        }
        if (act == TipoActividad.FUERZA) {
            drawRoundRect(LessionYellow, Offset(leftHand.x - 16 * s, leftHand.y - 8 * s), Size(32 * s, 16 * s), CornerRadius(5 * s))
            drawRoundRect(LessionYellow, Offset(rightHand.x - 16 * s, rightHand.y - 8 * s), Size(32 * s, 16 * s), CornerRadius(5 * s))
        }

        val flowColor = when {
            state.hr > 170 -> LessionRed
            state.hr > 140 -> LessionOrange
            state.hr > 100 -> LessionYellow
            else -> Color(0xFF00E5FF)
        }
        drawPath(body, flowColor.copy(alpha = 0.55f), style = Stroke(3f * s, pathEffect = PathEffect.dashPathEffect(floatArrayOf(48f, 112f), flow)))

        state.musculos.forEach { (name, muscle) ->
            val affected = state.lesion == name || state.alertaSobrecarga == name
            val intensity = ((muscle.fatiga * 0.45f + muscle.carga * 0.55f) / 100f).coerceIn(0f, 1f)
            val pos = musclePosition(name, cx, cy, s, state.esFrontal)
            if (pos != null && (affected || muscle.carga > 18f || muscle.fatiga > 25f)) {
                val hot = affected || muscle.fatiga > 82f
                val color = if (hot) LessionRed else if (muscle.fatiga > 60f) LessionOrange else LessionYellow
                val pulse = if (affected) 1f + abs(sin(time * 3f)) * 0.28f else 1f
                val zoneWidth = muscleZoneWidth(name) * s * (0.85f + intensity * 0.35f) * pulse
                val zoneHeight = muscleZoneHeight(name) * s * (0.85f + intensity * 0.35f) * pulse
                drawOval(
                    color = color,
                    topLeft = Offset(pos.x - zoneWidth / 2f, pos.y - zoneHeight / 2f),
                    size = Size(zoneWidth, zoneHeight),
                    alpha = if (affected) 0.72f else 0.28f + intensity * 0.28f
                )
                if (affected || muscle.fatiga > 70f) {
                    drawOval(
                        color = color,
                        topLeft = Offset(pos.x - zoneWidth / 2f, pos.y - zoneHeight / 2f),
                        size = Size(zoneWidth, zoneHeight),
                        style = Stroke(2.5f * s),
                        alpha = 0.55f
                    )
                }
            }
        }

        if (injured) {
            drawCircle(LessionRed, 6 * s, Offset(cx - 10 * s, cy - 176 * s))
            drawCircle(LessionRed, 6 * s, Offset(cx + 10 * s, cy - 176 * s))
            drawLine(LessionRed, Offset(cx - 14 * s, cy - 162 * s), Offset(cx + 14 * s, cy - 162 * s), 4 * s)
            val target = musclePosition(state.lesion ?: state.alertaSobrecarga.orEmpty(), cx, cy, s, state.esFrontal) ?: Offset(cx - 50 * s, cy - 85 * s)
            drawLine(LessionYellow, Offset(cx - 54 * s, cy - 82 * s), target, 8 * s, cap = StrokeCap.Round)
        }
    }
}

private fun musclePosition(name: String, cx: Float, cy: Float, s: Float, frontal: Boolean): Offset? = when (name) {
    "Cuadriceps" -> if (frontal) Offset(cx - 22 * s, cy + 60 * s) else null
    "Gemelos" -> if (!frontal) Offset(cx - 22 * s, cy + 148 * s) else null
    "Tibiales" -> if (frontal) Offset(cx - 22 * s, cy + 150 * s) else null
    "Isquiotibiales" -> if (!frontal) Offset(cx - 22 * s, cy + 82 * s) else null
    "Gluteos" -> if (!frontal) Offset(cx, cy + 26 * s) else null
    "Hombros" -> Offset(cx + 54 * s, cy - 122 * s)
    "Biceps" -> if (frontal) Offset(cx - 55 * s, cy - 82 * s) else null
    "Triceps" -> if (!frontal) Offset(cx - 55 * s, cy - 82 * s) else null
    "Rodillas" -> if (frontal) Offset(cx - 20 * s, cy + 108 * s) else null
    "Tobillos" -> Offset(cx - 20 * s, cy + 176 * s)
    else -> null
}

private fun muscleZoneWidth(name: String): Float = when (name) {
    "Cuadriceps", "Isquiotibiales", "Gemelos", "Tibiales" -> 22f
    "Gluteos" -> 48f
    "Hombros" -> 34f
    "Biceps", "Triceps" -> 18f
    "Rodillas", "Tobillos" -> 24f
    else -> 22f
}

private fun muscleZoneHeight(name: String): Float = when (name) {
    "Cuadriceps", "Isquiotibiales" -> 56f
    "Gemelos", "Tibiales" -> 46f
    "Gluteos" -> 28f
    "Hombros" -> 24f
    "Biceps", "Triceps" -> 42f
    "Rodillas", "Tobillos" -> 24f
    else -> 28f
}

@Composable
fun PanelMetricas(state: SimulacionState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TarjetaMetrica("Frecuencia cardiaca", "${state.hr.toInt()} BPM", state.hr / 200f, LessionYellow, state.hrHistory, 200f)
        TarjetaMetrica("Energia corporal", "${state.energia.toInt()}%", state.energia / 100f, LessionEnergy, state.energyHistory, 100f)
        TarjetaMetrica("Temperatura", "${"%.1f".format(state.temperatura)} C", (state.temperatura - 36) / 7f, LessionTemp, state.tempHistory, 43f, 36f)
        TarjetaMetrica("Fatiga muscular", "${state.fatigaGeneral.toInt()}%", state.fatigaGeneral / 100f, LessionFatigue, state.fatigueHistory, 100f)
    }
}

@Composable
fun TarjetaMetrica(label: String, value: String, progress: Float, color: Color, history: List<Float>, max: Float, min: Float = 0f) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LessionVioletLight),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, color.copy(0.25f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(label.uppercase(), color = Color.White.copy(0.62f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(48.dp).drawBehind {
                if (history.size > 1) {
                    val p = Path()
                    val dx = size.width / (history.size - 1)
                    val range = max - min
                    history.forEachIndexed { i, v ->
                        val x = i * dx
                        val y = size.height - ((v - min) / range * size.height).coerceIn(0f, size.height)
                        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    drawPath(p, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    p.lineTo(size.width, size.height)
                    p.lineTo(0f, size.height)
                    p.close()
                    drawPath(p, Brush.verticalGradient(listOf(color.copy(0.35f), Color.Transparent)))
                }
            })
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = color,
                trackColor = Color.White.copy(0.07f)
            )
        }
    }
}

@Composable
fun PanelMusculos(state: SimulacionState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LessionVioletLight),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("MUSCULOS EN TIEMPO REAL", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(14.dp))
            val activos = state.musculos.values.filter { it.carga > 0.5f || it.fatiga > 5f }.sortedByDescending { it.carga + it.fatiga }
            if (activos.isEmpty()) {
                Text("Cuerpo en reposo y recuperacion", color = Color.White.copy(0.45f), fontSize = 12.sp)
            } else {
                activos.take(7).forEach { MusculoRow(it) }
            }
        }
    }
}

@Composable
fun MusculoRow(m: EstadoMusculo) {
    val riesgo = when {
        m.fatiga > 82f || m.carga > 88f -> "Critico"
        m.fatiga > 60f || m.carga > 68f -> "Alto"
        m.carga > 30f -> "Medio"
        else -> "Bajo"
    }
    val color = when (riesgo) {
        "Critico" -> LessionRed
        "Alto" -> LessionOrange
        else -> LessionYellow
    }
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(m.nombre, color = Color.White.copy(0.9f), fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text("${m.carga.toInt()}%  $riesgo", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { (m.carga / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(0.07f)
        )
    }
}

@Composable
fun PanelInferior(state: SimulacionState, viewModel: LessionViewModel) {
    Surface(color = LessionVioletLight, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)) {
        Column(Modifier.padding(top = 14.dp, bottom = 18.dp)) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(TipoActividad.entries) { act ->
                    ActividadItem(act, state.actividadActual == act) { viewModel.cambiarActividad(act) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatoSecundario("Activos", state.musculos.values.count { it.carga > 5f }.toString())
                DatoSecundario("Fatigados", state.musculos.values.count { it.fatiga > 55f }.toString())
                DatoSecundario("Tiempo", "${state.tiempoActividad}s")
                DatoSecundario("Consumo", "${(100 - state.energia).toInt()}%")
                DatoSecundario("Recuperacion", "${(100 - state.fatigaGeneral).toInt()}%")
                DatoSecundario("Riesgo", riesgoGeneral(state))
                DatoSecundario("Estado", estadoFisiologico(state))
            }
        }
    }
}

@Composable
fun ActividadItem(act: TipoActividad, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.06f else 1f, label = "scale")
    Column(
        modifier = Modifier
            .width(104.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) LessionYellow else LessionVioletAccent)
            .clickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(act.icon, contentDescription = act.nombre, tint = if (isSelected) LessionViolet else Color.White, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text(act.nombre, color = if (isSelected) LessionViolet else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
fun DatoSecundario(label: String, value: String) {
    Column(
        modifier = Modifier
            .width(118.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LessionViolet.copy(0.55f))
            .border(1.dp, Color.White.copy(0.07f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(label.uppercase(), color = Color.White.copy(0.45f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

private fun riesgoGeneral(state: SimulacionState): String = when {
    state.lesion != null -> "Lesion"
    state.alertaSobrecarga != null -> "Critico"
    state.fatigaGeneral > 70f || state.temperatura > 39.4f || state.hr > 170f -> "Alto"
    state.fatigaGeneral > 35f || state.hr > 135f -> "Medio"
    else -> "Bajo"
}

private fun estadoFisiologico(state: SimulacionState): String = when {
    state.lesion != null -> "Bloqueado"
    state.alertaSobrecarga != null -> "Descanso"
    state.actividadActual == TipoActividad.DESCANSO -> "Recuperando"
    state.hr > 160f -> "Intenso"
    else -> "Estable"
}

@Composable
fun AlertaSobrecarga(musculo: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier.padding(top = 76.dp, start = 16.dp, end = 16.dp),
            color = LessionRed.copy(0.92f),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                "Sobrecarga en $musculo. Actividad detenida e inicio de recuperacion.",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OverlayLesion(musculo: String, viewModel: LessionViewModel) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.92f)), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = LessionViolet),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.padding(22.dp).border(2.dp, LessionRed, RoundedCornerShape(28.dp))
        ) {
            Column(Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info, contentDescription = null, tint = LessionRed, modifier = Modifier.size(68.dp))
                Spacer(Modifier.height(20.dp))
                Text("LESION DETECTADA", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text(
                    "Se produjo una rotura o distension critica en: $musculo.\n\nLa actividad fisica fue bloqueada. Inicia el protocolo de recuperacion.",
                    color = Color.White.copy(0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(26.dp))
                Button(
                    onClick = { viewModel.iniciarRecuperacion() },
                    colors = ButtonDefaults.buttonColors(containerColor = LessionYellow),
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("INICIAR RECUPERACION", color = LessionViolet, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LessionPreview() {
    LESSIONTheme { LessionApp(LessionViewModel()) }
}
