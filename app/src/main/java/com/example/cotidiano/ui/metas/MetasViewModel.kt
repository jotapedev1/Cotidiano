package com.example.cotidiano.ui.metas

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.cotidiano.dados.local.Meta
import com.example.cotidiano.dados.local.MetaComSubMetas
import com.example.cotidiano.dados.local.SubMeta
import com.example.cotidiano.ui.comum.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MetasViewModel(app: Application) : BaseViewModel(app) {

    // stateIn: transforma o Flow do banco em um estado que a tela pode observar
    val metas = repo.metas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun adicionarMeta(titulo: String) = viewModelScope.launch { repo.inserirMeta(titulo) }

    fun adicionarSubMeta(meta: Meta, titulo: String) = ganharXp { repo.adicionarSubMeta(meta, titulo) }

    fun alternarMeta(meta: Meta) = ganharXp { repo.alternarMetaSimples(meta) }

    fun alternarSubMeta(item: MetaComSubMetas, sub: SubMeta) = ganharXp { repo.alternarSubMeta(item, sub) }

    fun excluirMeta(meta: Meta) = viewModelScope.launch { repo.excluirMeta(meta) }
}
