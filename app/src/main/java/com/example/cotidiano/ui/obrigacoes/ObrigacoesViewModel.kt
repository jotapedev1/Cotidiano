package com.example.cotidiano.ui.obrigacoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.dados.local.Obrigacao
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ObrigacoesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio.obter(app)

    val obrigacoes = repo.obrigacoes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _eventosXp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val eventosXp = _eventosXp.asSharedFlow()

    fun adicionar(titulo: String, prazo: Long?) = viewModelScope.launch {
        repo.inserirObrigacao(titulo, prazo)
    }

    fun alternar(o: Obrigacao) = viewModelScope.launch { _eventosXp.emit(repo.alternarObrigacao(o)) }

    fun excluir(o: Obrigacao) = viewModelScope.launch { repo.excluirObrigacao(o) }
}