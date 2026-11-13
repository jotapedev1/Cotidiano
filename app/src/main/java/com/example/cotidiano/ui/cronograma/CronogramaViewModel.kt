package com.example.cotidiano.ui.cronograma

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.dados.local.BlocoCronograma
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CronogramaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio.obter(app)

    private val _diaSelecionado = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
    val diaSelecionado: StateFlow<Int> = _diaSelecionado

    val blocos = _diaSelecionado
        .flatMapLatest { dia -> repo.observarCronogramaDia(dia) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selecionarDia(dia: Int) {
        _diaSelecionado.value = dia
    }

    fun adicionarBloco(titulo: String, diaSemana: Int, inicioMin: Int, fimMin: Int, cor: Int) = viewModelScope.launch {
        repo.inserirBlocoCronograma(titulo, diaSemana, inicioMin, fimMin, cor)
    }

    fun atualizarBloco(bloco: BlocoCronograma) = viewModelScope.launch {
        repo.atualizarBlocoCronograma(bloco)
    }

    fun excluirBloco(bloco: BlocoCronograma) = viewModelScope.launch {
        repo.excluirBlocoCronograma(bloco)
    }
}
