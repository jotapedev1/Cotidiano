package com.example.cotidiano.dominio

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraXpTest {

    @Test
    fun nivel_comeca_em_1_e_sobe_a_cada_100_xp() {
        assertEquals(1, CalculadoraXp.nivel(0))
        assertEquals(1, CalculadoraXp.nivel(99))
        assertEquals(2, CalculadoraXp.nivel(100))
    }

    @Test
    fun progresso_e_xp_restante_no_nivel() {
        assertEquals(35, CalculadoraXp.progressoNoNivel(235))
        assertEquals(65, CalculadoraXp.xpParaProximoNivel(235))
    }

    @Test
    fun itens_exclusivos_sao_liberados_por_xp() {
        assertEquals(emptyList<ItemExclusivo>(), itensDesbloqueados(99))
        assertEquals(listOf("Tema Noturno"), itensDesbloqueados(100).map { it.nome })
        assertEquals(3, itensDesbloqueados(600).size)
    }
}
