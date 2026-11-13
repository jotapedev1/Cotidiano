package com.example.cotidiano.ui.obrigacoes

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.local.Obrigacao
import com.example.cotidiano.ui.comum.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ObrigacoesViewModel(app: Application) : BaseViewModel(app) {

    val obrigacoes = repo.obrigacoes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun adicionar(titulo: String, prazo: Long?) = viewModelScope.launch { repo.inserirObrigacao(titulo, prazo) }

    fun alternar(o: Obrigacao) = ganharXp { repo.alternarObrigacao(o) }

    fun excluir(o: Obrigacao) = viewModelScope.launch { repo.excluirObrigacao(o) }
}
