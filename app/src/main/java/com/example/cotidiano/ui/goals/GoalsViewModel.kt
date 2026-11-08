package com.example.cotidiano.ui.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.data.AppRepository
import com.example.cotidiano.data.local.Meta
import com.example.cotidiano.data.local.MetaComSubMetas
import com.example.cotidiano.data.local.SubMeta
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository.get(app)

    val metas = repo.metas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _xp = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val xpEvents = _xp.asSharedFlow()

    fun adicionarMeta(titulo: String) = viewModelScope.launch { repo.inserirMeta(titulo) }

    fun adicionarSubMeta(meta: Meta, titulo: String) = viewModelScope.launch {
        _xp.emit(repo.adicionarSubMeta(meta, titulo))
    }

    fun alternarMeta(meta: Meta) = viewModelScope.launch { _xp.emit(repo.alternarMetaSimples(meta)) }

    fun alternarSubMeta(item: MetaComSubMetas, sub: SubMeta) = viewModelScope.launch {
        _xp.emit(repo.alternarSubMeta(item, sub))
    }

    fun excluirMeta(meta: Meta) = viewModelScope.launch { repo.excluirMeta(meta) }
}