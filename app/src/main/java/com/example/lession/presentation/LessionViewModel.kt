package com.example.lession.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lession.data.SimulacionState
import com.example.lession.data.TipoActividad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// sqrt not used

/**
 * ViewModel para la lógica de simulación del cuerpo humano
 * Maneja la fisiología de la simulación: HR, energía, temperatura, fatiga
 */
class LessionViewModel : ViewModel() {
    private val _state = mutableStateOf(SimulacionState())
    val state: SimulacionState get() = _state.value

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
        if (_state.value.lesion != null) return

        val act = _state.value.actividadActual
        
        // 1. FRECUENCIA CARDÍACA - Converge gradualmente hacia el rango de la actividad
        val targetHR = act.hrMax.toFloat()
        val deltaHR = targetHR - _state.value.hr
        val factorHR = if (act == TipoActividad.DESCANSO) 0.2f else 0.08f
        val stepHR = (deltaHR * factorHR).coerceIn(-3.0f, 4.0f)
        val nuevoHR = (_state.value.hr + stepHR).coerceIn(60f, 205f)

        // 2. ENERGÍA - Disminuye con actividad, recupera en descanso
        val factorEnergia = if (act == TipoActividad.DESCANSO) -1.5f else (act.consumoEnergia * (nuevoHR / 80f)) * 0.3f
        val nuevaEnergia = (_state.value.energia - factorEnergia).coerceIn(0f, 100f)

        // 3. TEMPERATURA - Relacionada con actividad física e intensidad
        val factorTemp = if (act == TipoActividad.DESCANSO) -0.06f else (act.aumentoTemp * (nuevoHR / 110f)) * 0.5f
        val nuevaTemp = (_state.value.temperatura + factorTemp).coerceIn(36.5f, 42.5f)

        // 4. MÚSCULOS Y FATIGA - Cálculo realista de carga muscular
        val nuevosMusculos = _state.value.musculos.toMutableMap()
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

        // 5. SISTEMA DE LESIONES - Combinación de factores de riesgo
        var lesionNueva: String? = null
        if (nuevoHR > 170 && nuevaFatigaG > 80f && nuevaTemp > 39.5f) {
            val candidato = nuevosMusculos.values.filter { it.fatiga > 85f }.randomOrNull()
            if (candidato != null && (0..100).random() < 22) { 
                lesionNueva = candidato.nombre
            }
        }

        // 6. HISTORIALES - Mantener últimos 40 puntos para gráficos
        val maxPoints = 40
        val hrHist = (_state.value.hrHistory + nuevoHR).takeLast(maxPoints)
        val enHist = (_state.value.energyHistory + nuevaEnergia).takeLast(maxPoints)
        val teHist = (_state.value.tempHistory + nuevaTemp).takeLast(maxPoints)
        val faHist = (_state.value.fatigueHistory + nuevaFatigaG).takeLast(maxPoints)

        _state.value = _state.value.copy(
            hr = nuevoHR,
            energia = nuevaEnergia,
            temperatura = nuevaTemp,
            musculos = nuevosMusculos,
            fatigaGeneral = nuevaFatigaG,
            lesion = lesionNueva,
            tiempoActividad = _state.value.tiempoActividad + 1,
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
        if (_state.value.lesion != null) return
        _state.value = _state.value.copy(actividadActual = nueva)
    }

    fun iniciarRecuperacion() {
        _state.value = _state.value.copy(
            lesion = null,
            actividadActual = TipoActividad.DESCANSO
        )
    }

    fun alternarVista() {
        _state.value = _state.value.copy(esFrontal = !_state.value.esFrontal)
    }
}

