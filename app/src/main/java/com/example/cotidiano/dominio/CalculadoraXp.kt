package com.example.cotidiano.dominio

object CalculadoraXp {
    const val XP_SUBMETA = 10
    const val XP_META = 50
    const val XP_OBRIGACAO = 20
    const val XP_POMODORO = 15

    // Função potência (power curve): XP acumulado necessário para atingir o nível L (L >= 1)
    // Nível 1 começa em 0 XP.
    fun xpAcumuladoParaNivel(nivel: Int): Int {
        if (nivel <= 1) return 0
        // Power curve: 100 * (nivel - 1)^1.5
        return (100 * Math.pow((nivel - 1).toDouble(), 1.5)).toInt()
    }

    fun nivel(xpTotal: Int): Int {
        var l = 1
        while (xpTotal >= xpAcumuladoParaNivel(l + 1)) {
            l++
        }
        return l
    }

    fun xpAtualNoNivel(xpTotal: Int): Int {
        val n = nivel(xpTotal)
        val xpAtualNivelStart = xpAcumuladoParaNivel(n)
        return xpTotal - xpAtualNivelStart
    }

    fun xpNecessarioParaProximoNivel(xpTotal: Int): Int {
        val n = nivel(xpTotal)
        val xpNextLevel = xpAcumuladoParaNivel(n + 1)
        val xpCurrentLevel = xpAcumuladoParaNivel(n)
        return xpNextLevel - xpCurrentLevel
    }

    fun progressoNoNivel(xpTotal: Int): Int {
        val atual = xpAtualNoNivel(xpTotal)
        val necessario = xpNecessarioParaProximoNivel(xpTotal)
        if (necessario <= 0) return 100
        return (atual * 100) / necessario
    }

    fun xpParaProximoNivel(xpTotal: Int): Int {
        val atual = xpAtualNoNivel(xpTotal)
        val necessario = xpNecessarioParaProximoNivel(xpTotal)
        return necessario - atual
    }
}

data class ItemExclusivo(val nome: String, val xpNecessario: Int)

val ITENS_EXCLUSIVOS = listOf(
    ItemExclusivo("Tema Noturno", 100),
    ItemExclusivo("Avatar Dourado", 300),
    ItemExclusivo("Animação Especial", 600)
)

fun itensDesbloqueados(xp: Int) = ITENS_EXCLUSIVOS.filter { xp >= it.xpNecessario }
