package com.example.cotidiano.dominio

import java.util.TimeZone

/** Dias desde 01/01/1970 no fuso local. Muda exatamente à meia-noite local. */
fun diaAtual(agora: Long = System.currentTimeMillis()): Long =
    (agora + TimeZone.getDefault().getOffset(agora)) / 86_400_000L