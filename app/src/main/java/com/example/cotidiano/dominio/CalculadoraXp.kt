package com.example.cotidiano.dominio

import kotlin.math.pow

/** Quanto XP cada ação vale e como o XP vira nível. */
object CalculadoraXp {
    const val XP_TAREFA = 10
    const val XP_SUBMETA = 10
    const val XP_META = 50
    const val XP_OBRIGACAO = 20
    const val XP_POMODORO = 15

    // XP total necessário para ALCANÇAR um nível: 100 * (nível - 1)^1,5
    // Ex.: nível 2 = 100 XP, nível 3 = 282 XP (cada nível custa mais que o anterior).
    private fun xpParaAlcancar(nivel: Int): Int =
        if (nivel <= 1) 0 else (100 * (nivel - 1).toDouble().pow(1.5)).toInt()

    fun nivel(xpTotal: Int): Int {
        var n = 1
        while (xpTotal >= xpParaAlcancar(n + 1)) n++
        return n
    }

    /** XP acumulado dentro do nível atual. */
    fun xpAtualNoNivel(xpTotal: Int): Int = xpTotal - xpParaAlcancar(nivel(xpTotal))

    /** Tamanho do nível atual (quanto falta do início ao fim dele). */
    fun xpNecessarioParaProximoNivel(xpTotal: Int): Int {
        val n = nivel(xpTotal)
        return xpParaAlcancar(n + 1) - xpParaAlcancar(n)
    }

    /** Porcentagem (0 a 100) da barra de XP. */
    fun progressoNoNivel(xpTotal: Int): Int =
        xpAtualNoNivel(xpTotal) * 100 / xpNecessarioParaProximoNivel(xpTotal)

    fun xpParaProximoNivel(xpTotal: Int): Int =
        xpNecessarioParaProximoNivel(xpTotal) - xpAtualNoNivel(xpTotal)
}

/** Itens que o usuário desbloqueia ao atingir certa quantidade de XP. */
data class ItemExclusivo(val nome: String, val xpNecessario: Int)

val ITENS_EXCLUSIVOS = listOf(
    ItemExclusivo("Tema Noturno", 100),
    ItemExclusivo("Avatar Dourado", 300),
    ItemExclusivo("Animação Especial", 600)
)

fun itensDesbloqueados(xp: Int) = ITENS_EXCLUSIVOS.filter { xp >= it.xpNecessario }
