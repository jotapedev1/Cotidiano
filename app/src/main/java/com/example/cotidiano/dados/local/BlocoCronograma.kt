package com.example.cotidiano.dados.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Bloco do cronograma. Horários em minutos desde 00:00 (ex.: 08:30 = 510). */
@Entity(tableName = "blocos_cronograma", indices = [Index("usuarioId")])
data class BlocoCronograma(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val diaSemana: Int, // 1 = domingo ... 7 = sábado (padrão do Calendar)
    val inicioMin: Int,
    val fimMin: Int,
    val cor: Int
)
