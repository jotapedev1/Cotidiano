package com.example.cotidiano.ui.pomodoro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.dominio.CalculadoraXp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PomodoroViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repositorio.obter(app)

    private var tempoInicialMs = 25 * 60 * 1000L

    private val _restanteMs = MutableStateFlow(tempoInicialMs)
    val restanteMs: StateFlow<Long> = _restanteMs.asStateFlow()

    private val _rodando = MutableStateFlow(false)
    val rodando: StateFlow<Boolean> = _rodando.asStateFlow()

    private val _eventosXp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val eventosXp = _eventosXp.asSharedFlow()

    private var timerJob: Job? = null

    fun definirDuracaoMinutos(minutos: Long) {
        timerJob?.cancel()
        _rodando.value = false
        tempoInicialMs = minutos * 60 * 1000L
        _restanteMs.value = tempoInicialMs
    }

    fun iniciarOuPausar() {
        if (_rodando.value) {
            pausar()
        } else {
            iniciar()
        }
    }

    fun iniciar() {
        if (_rodando.value || _restanteMs.value <= 0L) return
        _rodando.value = true

        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val startRestante = _restanteMs.value
            while (_rodando.value && _restanteMs.value > 0L) {
                val elapsed = System.currentTimeMillis() - startTime
                val currentRestante = (startRestante - elapsed).coerceAtLeast(0L)
                _restanteMs.value = currentRestante
                if (currentRestante <= 0L) {
                    onFinishTimer()
                    break
                }
                delay(100L)
            }
        }
    }

    fun pausar() {
        _rodando.value = false
        timerJob?.cancel()
    }

    fun resetar() {
        timerJob?.cancel()
        _rodando.value = false
        _restanteMs.value = tempoInicialMs
    }

    private fun onFinishTimer() {
        timerJob?.cancel()
        _rodando.value = false
        val minutos = (tempoInicialMs / 1000 / 60).toInt()
        viewModelScope.launch {
            try {
                repo.inserirSessaoPomodoro(minutos)
                _eventosXp.emit(CalculadoraXp.XP_POMODORO)
            } catch (e: Exception) {
                // ignore
            }
        }
        _restanteMs.value = tempoInicialMs
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
