package com.example.cotidiano.dados.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// Cada @Entity vira uma tabela. Quase todas têm "usuarioId":
// é ele que separa os dados de cada conta.

@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val xp: Int = 0 // XP total da conta
)

/** Tarefa da aba "Hoje". Está concluída se ultimaConclusaoDia == dia de hoje. */
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

// CASCADE: ao excluir a meta, o banco exclui as submetas dela junto.
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

/** Não é tabela: só junta uma meta com suas submetas numa consulta. */
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

/** Histórico de pomodoros concluídos. */
@Entity(tableName = "sessoes_pomodoro", indices = [Index("usuarioId")])
data class SessaoPomodoro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val inicio: Long,
    val minutosFoco: Int
)
