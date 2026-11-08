package com.example.cotidiano.ui.chores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.data.AppRepository
import com.example.cotidiano.data.local.Obrigacao
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChoresViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository.get(app)

    val obrigacoes = repo.obrigacoes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _xp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val xpEvents = _xp.asSharedFlow()

    fun adicionar(titulo: String, prazo: Long?) = viewModelScope.launch {
        repo.inserirObrigacao(titulo, prazo)
    }

    fun alternar(o: Obrigacao) = viewModelScope.launch { _xp.emit(repo.alternarObrigacao(o)) }

    fun excluir(o: Obrigacao) = viewModelScope.launch { repo.excluirObrigacao(o) }
}