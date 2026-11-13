package com.example.cotidiano.dados.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Histórico de pomodoros concluídos. */
@Entity(tableName = "sessoes_pomodoro", indices = [Index("usuarioId")])
data class SessaoPomodoro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val inicio: Long,
    val minutosFoco: Int
)
