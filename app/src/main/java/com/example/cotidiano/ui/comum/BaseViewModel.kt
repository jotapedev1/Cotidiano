package com.example.cotidiano.ui.comum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.Repositorio
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Base dos ViewModels que dão XP (Hoje, Metas, Obrigações, Pomodoro).
 * Guarda o repositório e o canal "eventosXp", que a tela observa para mostrar "+10 XP".
 */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {
    protected val repo = Repositorio.obter(app)

    private val _eventosXp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val eventosXp = _eventosXp.asSharedFlow()

    /** Executa a ação (que devolve o XP ganho) e avisa a tela. */
    protected fun ganharXp(acao: suspend () -> Int) = viewModelScope.launch {
        _eventosXp.emit(acao())
    }
}
