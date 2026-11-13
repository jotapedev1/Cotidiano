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

    // Dia exibido (1 = domingo ... 7 = sábado). Começa no dia de hoje.
    private val _dia = MutableStateFlow(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
    val diaSelecionado: StateFlow<Int> = _dia

    // Ao trocar o dia, a consulta ao banco é refeita automaticamente
    val blocos = _dia
        .flatMapLatest { repo.observarBlocos(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selecionarDia(dia: Int) { _dia.value = dia }

    fun adicionar(titulo: String, dia: Int, inicioMin: Int, fimMin: Int, cor: Int) =
        viewModelScope.launch { repo.inserirBloco(titulo, dia, inicioMin, fimMin, cor) }

    fun atualizar(bloco: BlocoCronograma) = viewModelScope.launch { repo.atualizarBloco(bloco) }

    fun excluir(bloco: BlocoCronograma) = viewModelScope.launch { repo.excluirBloco(bloco) }
}
