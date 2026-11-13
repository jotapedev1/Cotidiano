package com.example.cotidiano.ui.inicio

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.local.TarefaDiaria
import com.example.cotidiano.dominio.diaAtual
import com.example.cotidiano.ui.comum.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tarefa + se ela já foi feita HOJE. */
data class TarefaItem(val tarefa: TarefaDiaria, val concluidaHoje: Boolean)

class InicioViewModel(app: Application) : BaseViewModel(app) {

    private val hoje = MutableStateFlow(diaAtual())

    // Junta as tarefas do banco com o dia de hoje: se o dia virou, todas voltam a "pendente"
    val tarefas = combine(repo.tarefas, hoje) { lista, dia ->
        lista.map { TarefaItem(it, it.ultimaConclusaoDia == dia) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Chamado ao voltar para a tela, caso o dia tenha virado com o app aberto. */
    fun atualizarDia() { hoje.value = diaAtual() }

    fun adicionar(titulo: String) = viewModelScope.launch { repo.inserirTarefa(titulo) }

    fun alternar(item: TarefaItem) = ganharXp { repo.alternarTarefa(item.tarefa, diaAtual()) }

    fun excluir(item: TarefaItem) = viewModelScope.launch { repo.excluirTarefa(item.tarefa) }
}
