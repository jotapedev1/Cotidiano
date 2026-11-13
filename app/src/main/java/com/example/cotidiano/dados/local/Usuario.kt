package com.example.cotidiano.dados.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Cada @Entity vira uma tabela. Quase todas as outras têm "usuarioId":
// é ele que separa os dados de cada conta.
@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val xp: Int = 0 // XP total da conta
)
