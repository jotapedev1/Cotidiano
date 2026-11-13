package com.example.cotidiano.dados.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "obrigacoes", indices = [Index("usuarioId")])
data class Obrigacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val prazo: Long? = null,
    val concluida: Boolean = false
)
