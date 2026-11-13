package com.example.cotidiano.dados

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Usuário logado (0 = ninguém). Persistido em SharedPreferences. */
class Sessao private constructor(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("cotidiano_prefs", Context.MODE_PRIVATE)

    private val _usuarioId = MutableStateFlow(prefs.getLong(CHAVE, 0L))
    val usuarioId: StateFlow<Long> = _usuarioId

    val logado: Boolean get() = _usuarioId.value != 0L

    fun entrar(id: Long) {
        prefs.edit().putLong(CHAVE, id).apply()
        _usuarioId.value = id
    }

    fun sair() {
        prefs.edit().putLong(CHAVE, 0L).apply()
        _usuarioId.value = 0L
    }

    companion object {
        private const val CHAVE = "usuario_id"
        @Volatile private var instancia: Sessao? = null
        fun obter(context: Context): Sessao =
            instancia ?: synchronized(this) {
                instancia ?: Sessao(context).also { instancia = it }
            }
    }
}