package com.example.cotidiano.dados.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tarefa da aba "Hoje". Está concluída se ultimaConclusaoDia == dia de hoje. */
@Entity(tableName = "tarefas_diarias", indices = [Index("usuarioId")])
data class TarefaDiaria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val ultimaConclusaoDia: Long? = null
)
