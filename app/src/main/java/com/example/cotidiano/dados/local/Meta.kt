package com.example.cotidiano.dados.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

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
