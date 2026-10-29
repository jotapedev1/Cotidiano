package com.example.cotidiano.domain

object XpCalculator {
    const val XP_SUBMETA = 10
    const val XP_META = 50
    const val XP_OBRIGACAO = 20
    const val XP_POMODORO = 15

    private const val XP_POR_NIVEL = 100

    fun nivel(xpTotal: Int) = xpTotal / XP_POR_NIVEL + 1
    fun progressoNoNivel(xpTotal: Int) = xpTotal % XP_POR_NIVEL
    fun xpParaProximoNivel(xpTotal: Int) = XP_POR_NIVEL - progressoNoNivel(xpTotal)
}

data class ItemExclusivo(val nome: String, val xpNecessario: Int)

val ITENS_EXCLUSIVOS = listOf(
    ItemExclusivo("Tema Noturno", 100),
    ItemExclusivo("Avatar Dourado", 300),
    ItemExclusivo("Animação Especial", 600)
)

fun itensDesbloqueados(xp: Int) = ITENS_EXCLUSIVOS.filter { xp >= it.xpNecessario }