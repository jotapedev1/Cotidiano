package com.example.cotidiano.dominio

import java.util.TimeZone

/**
 * Número do dia atual (dias desde 01/01/1970, no fuso do aparelho).
 * Muda exatamente à meia-noite local: é com ele que as tarefas diárias "reiniciam".
 */
fun diaAtual(agora: Long = System.currentTimeMillis()): Long =
    (agora + TimeZone.getDefault().getOffset(agora)) / 86_400_000L
