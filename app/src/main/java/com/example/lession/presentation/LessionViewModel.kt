package com.example.lession.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lession.data.SimulacionState
import com.example.lession.data.TipoActividad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        val targetHR = act.hrMax.toFloat()
        val deltaHR = targetHR - _state.value.hr
        val factorHR = if (act == TipoActividad.DESCANSO) 0.2f else 0.08f
        val stepHR = (deltaHR * factorHR).coerceIn(-3.0f, 4.0f)
        val nuevoHR = (_state.value.hr + stepHR).coerceIn(60f, 205f)

        val factorEnergia = if (act == TipoActividad.DESCANSO) {
            -1.5f
        } else {
            (act.consumoEnergia * (nuevoHR / 80f)) * 0.3f
        }
        val nuevaEnergia = (_state.value.energia - factorEnergia).coerceIn(0f, 100f)

        val factorTemp = if (act == TipoActividad.DESCANSO) {
            -0.06f
        } else {
            (act.aumentoTemp * (nuevoHR / 110f)) * 0.5f
        }
        val nuevaTemp = (_state.value.temperatura + factorTemp).coerceIn(36.5f, 42.5f)

        val nuevosMusculos = _state.value.musculos.toMutableMap()
        val musculosActivos = obtenerMusculosPorActividad(act)

        nuevosMusculos.forEach { (nombre, mState) ->
            val activo = musculosActivos.contains(nombre)
            val cargaNueva = if (activo) {
                (mState.carga + act.impactoMuscular * 0.9f).coerceIn(0f, 100f)
            } else {
                (mState.carga - 2.5f).coerceIn(0f, 100f)
            }
            val fatigaNueva = if (activo) {
                (mState.fatiga + (cargaNueva * 0.028f)).coerceIn(0f, 100f)
            } else {
                (mState.fatiga - 1.5f).coerceIn(0f, 100f)
            }
            nuevosMusculos[nombre] = mState.copy(carga = cargaNueva, fatiga = fatigaNueva)
        }

        val nuevaFatigaG = nuevosMusculos.values.map { it.fatiga }.average().toFloat()
        val musculoCritico = nuevosMusculos.values
            .filter { it.carga > 88f && it.fatiga > 82f }
            .maxByOrNull { it.carga + it.fatiga }

        var lesionNueva: String? = null
        if (nuevoHR > 170 && nuevaFatigaG > 80f && nuevaTemp > 39.5f) {
            val candidato = nuevosMusculos.values.filter { it.fatiga > 85f }.randomOrNull()
            if (candidato != null && (0..100).random() < 22) {
                lesionNueva = candidato.nombre
            }
        }

        val alertaSobrecarga = when {
            lesionNueva != null -> null
            musculoCritico != null -> musculoCritico.nombre
            _state.value.actividadActual == TipoActividad.DESCANSO -> null
            else -> _state.value.alertaSobrecarga
        }
        val actividadNueva = if (alertaSobrecarga != null) TipoActividad.DESCANSO else act

        val maxPoints = 40
        _state.value = _state.value.copy(
            actividadActual = actividadNueva,
            hr = nuevoHR,
            energia = nuevaEnergia,
            temperatura = nuevaTemp,
            musculos = nuevosMusculos,
            fatigaGeneral = nuevaFatigaG,
            lesion = lesionNueva,
            alertaSobrecarga = alertaSobrecarga,
            tiempoActividad = _state.value.tiempoActividad + 1,
            hrHistory = (_state.value.hrHistory + nuevoHR).takeLast(maxPoints),
            energyHistory = (_state.value.energyHistory + nuevaEnergia).takeLast(maxPoints),
            tempHistory = (_state.value.tempHistory + nuevaTemp).takeLast(maxPoints),
            fatigueHistory = (_state.value.fatigueHistory + nuevaFatigaG).takeLast(maxPoints)
        )
    }

    private fun obtenerMusculosPorActividad(act: TipoActividad): List<String> = when (act) {
        TipoActividad.CAMINAR -> listOf("Cuadriceps", "Gemelos", "Tibiales")
        TipoActividad.TROTAR -> listOf("Cuadriceps", "Isquiotibiales", "Gemelos")
        TipoActividad.CORRER -> listOf("Cuadriceps", "Gemelos", "Gluteos")
        TipoActividad.SPRINTAR -> listOf("Cuadriceps", "Gemelos", "Isquiotibiales", "Gluteos")
        TipoActividad.SALTAR -> listOf("Rodillas", "Gemelos", "Tobillos")
        TipoActividad.SENTADILLAS -> listOf("Cuadriceps", "Gluteos", "Isquiotibiales")
        TipoActividad.ESTOCADAS -> listOf("Cuadriceps", "Gluteos")
        TipoActividad.FUTBOL -> listOf("Cuadriceps", "Gemelos", "Rodillas", "Tobillos", "Tibiales")
        TipoActividad.BASQUET -> listOf("Cuadriceps", "Gemelos", "Hombros", "Rodillas")
        TipoActividad.CICLISMO -> listOf("Cuadriceps", "Gemelos", "Isquiotibiales")
        TipoActividad.FUERZA -> listOf("Biceps", "Triceps", "Hombros", "Cuadriceps")
        TipoActividad.DESCANSO -> emptyList()
    }

    fun cambiarActividad(nueva: TipoActividad) {
        if (_state.value.lesion != null) return
        _state.value = _state.value.copy(
            actividadActual = nueva,
            alertaSobrecarga = null,
            tiempoActividad = if (_state.value.actividadActual == nueva) _state.value.tiempoActividad else 0
        )
    }

    fun iniciarRecuperacion() {
        _state.value = _state.value.copy(
            lesion = null,
            alertaSobrecarga = null,
            actividadActual = TipoActividad.DESCANSO
        )
    }

    fun alternarVista() {
        _state.value = _state.value.copy(esFrontal = !_state.value.esFrontal)
    }
}
