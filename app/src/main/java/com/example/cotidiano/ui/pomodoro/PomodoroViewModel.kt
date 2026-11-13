package com.example.cotidiano.ui.pomodoro

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.ui.comum.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Timer do Pomodoro. Conta em segundos e, ao zerar, registra a sessão e dá XP.
 * É compartilhado entre telas (activityViewModels), então continua contando ao trocar de aba.
 */
class PomodoroViewModel(app: Application) : BaseViewModel(app) {

    private var minutos = 25

    private val _restanteSeg = MutableStateFlow(minutos * 60)
    val restanteSeg: StateFlow<Int> = _restanteSeg

    private val _rodando = MutableStateFlow(false)
    val rodando: StateFlow<Boolean> = _rodando

    private var timer: Job? = null

    /** Troca a duração (25, 15 ou 5 min) e reinicia o timer. */
    fun definirMinutos(m: Int) {
        minutos = m
        resetar()
    }

    fun iniciarOuPausar() = if (_rodando.value) pausar() else iniciar()

    private fun iniciar() {
        _rodando.value = true
        timer = viewModelScope.launch {
            // A cada segundo desconta 1 até chegar em zero
            while (_restanteSeg.value > 0) {
                delay(1000)
                _restanteSeg.value -= 1
            }
            concluir()
        }
    }

    private fun pausar() {
        timer?.cancel()
        _rodando.value = false
    }

    fun resetar() {
        pausar()
        _restanteSeg.value = minutos * 60
    }

    // Terminou: registra a sessão (+XP) e deixa o timer pronto para outra rodada
    private fun concluir() {
        _rodando.value = false
        _restanteSeg.value = minutos * 60
        ganharXp { repo.registrarPomodoro(minutos) }
    }
}
