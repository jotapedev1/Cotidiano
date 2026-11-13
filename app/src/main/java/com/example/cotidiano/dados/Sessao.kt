package com.example.cotidiano.dados

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Guarda QUEM está logado. O id fica salvo em SharedPreferences,
 * então o login continua após fechar o app. 0 = ninguém logado.
 */
class Sessao private constructor(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("cotidiano_prefs", Context.MODE_PRIVATE)

    // StateFlow: o Repositório observa este valor e troca de usuário automaticamente
    private val _usuarioId = MutableStateFlow(prefs.getLong(CHAVE, 0L))
    val usuarioId: StateFlow<Long> = _usuarioId

    val logado: Boolean get() = _usuarioId.value != 0L

    fun entrar(id: Long) = salvar(id)
    fun sair() = salvar(0L)

    private fun salvar(id: Long) {
        prefs.edit().putLong(CHAVE, id).apply()
        _usuarioId.value = id
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
