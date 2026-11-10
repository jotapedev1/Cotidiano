package com.example.cotidiano.ui.metas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.dados.local.Meta
import com.example.cotidiano.dados.local.MetaComSubMetas
import com.example.cotidiano.dados.local.SubMeta
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetasViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio.obter(app)

    val metas = repo.metas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _eventosXp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val eventosXp = _eventosXp.asSharedFlow()

    fun adicionarMeta(titulo: String) = viewModelScope.launch { repo.inserirMeta(titulo) }

    fun adicionarSubMeta(meta: Meta, titulo: String) = viewModelScope.launch {
        _eventosXp.emit(repo.adicionarSubMeta(meta, titulo))
    }

    fun alternarMeta(meta: Meta) = viewModelScope.launch { _eventosXp.emit(repo.alternarMetaSimples(meta)) }

    fun alternarSubMeta(item: MetaComSubMetas, sub: SubMeta) = viewModelScope.launch {
        _eventosXp.emit(repo.alternarSubMeta(item, sub))
    }

    fun excluirMeta(meta: Meta) = viewModelScope.launch { repo.excluirMeta(meta) }
}