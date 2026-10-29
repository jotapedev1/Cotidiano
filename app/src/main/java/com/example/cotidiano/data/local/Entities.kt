package com.example.cotidiano.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "metas")
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "obrigacoes")
data class Obrigacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val prazo: Long? = null,
    val concluida: Boolean = false
)

@Entity(tableName = "blocos_cronograma")
data class BlocoCronograma(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val diaSemana: Int,
    val inicioMin: Int,
    val fimMin: Int
)

@Entity(tableName = "config_sono")
data class ConfigSono(
    @PrimaryKey val id: Int = 1,
    val dormirMin: Int = 22 * 60,
    val acordarMin: Int = 6 * 60
)

@Entity(tableName = "sessoes_pomodoro")
data class SessaoPomodoro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inicio: Long,
    val minutosFoco: Int,
    val completa: Boolean
)

@Entity(tableName = "progresso")
data class Progresso(
    @PrimaryKey val id: Int = 1,
    val xpTotal: Int = 0
)