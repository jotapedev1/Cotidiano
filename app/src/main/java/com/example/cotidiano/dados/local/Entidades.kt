package com.example.cotidiano.dados.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val salt: String
)

/** Tarefa da aba Hoje: repete todo dia. Concluída = ultimaConclusaoDia == hoje. */
@Entity(tableName = "tarefas_diarias", indices = [Index("usuarioId")])
data class TarefaDiaria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val ultimaConclusaoDia: Long? = null
)

@Entity(tableName = "metas", indices = [Index("usuarioId")])
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val concluida: Boolean = false
)

@Entity(
    tableName = "submetas",
    foreignKeys = [ForeignKey(
        entity = Meta::class, parentColumns = ["id"],
        childColumns = ["metaId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("metaId")]
)
data class SubMeta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metaId: Long,
    val titulo: String,
    val concluida: Boolean = false
)

data class MetaComSubMetas(
    @Embedded val meta: Meta,
    @Relation(parentColumn = "id", entityColumn = "metaId")
    val subMetas: List<SubMeta>
)

@Entity(tableName = "obrigacoes", indices = [Index("usuarioId")])
data class Obrigacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val prazo: Long? = null,
    val concluida: Boolean = false
)

@Entity(tableName = "blocos_cronograma", indices = [Index("usuarioId")])
data class BlocoCronograma(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val diaSemana: Int,
    val inicioMin: Int,
    val fimMin: Int,
    val cor: Int = android.graphics.Color.parseColor("#6750A4")
)

@Entity(tableName = "config_sono")
data class ConfigSono(
    @PrimaryKey val usuarioId: Long,
    val dormirMin: Int = 22 * 60,
    val acordarMin: Int = 6 * 60
)

@Entity(tableName = "sessoes_pomodoro", indices = [Index("usuarioId")])
data class SessaoPomodoro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val inicio: Long,
    val minutosFoco: Int,
    val completa: Boolean
)

@Entity(tableName = "progresso")
data class Progresso(
    @PrimaryKey val usuarioId: Long,
    val xpTotal: Int = 0
)