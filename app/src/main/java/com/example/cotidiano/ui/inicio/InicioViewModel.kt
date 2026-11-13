package com.example.cotidiano.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.dados.local.TarefaDiaria
import com.example.cotidiano.dominio.diaAtual
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TarefaItem(val tarefa: TarefaDiaria, val concluidaHoje: Boolean)

class InicioViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio.obter(app)
    private val hoje = MutableStateFlow(diaAtual())

    val tarefas = combine(repo.tarefas, hoje) { lista, dia ->
        lista.map { TarefaItem(it, it.ultimaConclusaoDia == dia) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _eventosXp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val eventosXp = _eventosXp.asSharedFlow()

    init {
        // Se o app ficar aberto na virada do dia, a lista zera sozinha.
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                hoje.value = diaAtual()
            }
        }
    }

    fun atualizarDia() { hoje.value = diaAtual() }

    fun adicionar(titulo: String) = viewModelScope.launch { repo.inserirTarefa(titulo) }

    fun alternar(item: TarefaItem) = viewModelScope.launch {
        _eventosXp.emit(repo.alternarTarefa(item.tarefa, diaAtual()))
    }

    fun excluir(item: TarefaItem) = viewModelScope.launch { repo.excluirTarefa(item.tarefa) }
}