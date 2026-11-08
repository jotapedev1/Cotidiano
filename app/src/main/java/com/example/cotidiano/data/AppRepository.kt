package com.example.cotidiano.data

import android.content.Context
import androidx.room.withTransaction
import com.example.cotidiano.data.local.AppDatabase
import com.example.cotidiano.data.local.Meta
import com.example.cotidiano.data.local.MetaComSubMetas
import com.example.cotidiano.data.local.Obrigacao
import com.example.cotidiano.data.local.SubMeta
import com.example.cotidiano.domain.XpCalculator
import kotlinx.coroutines.flow.Flow

/**
 * Fonte única de dados. As funções "alternar*" devolvem o delta de XP aplicado
 * (positivo ao concluir, negativo ao desmarcar) para a UI poder dar feedback.
 */
class AppRepository private constructor(private val db: AppDatabase) {

    private val metaDao = db.metaDao()
    private val obrigacaoDao = db.obrigacaoDao()
    private val configDao = db.configDao()

    val metas: Flow<List<MetaComSubMetas>> = metaDao.observarTodas()
    val obrigacoes: Flow<List<Obrigacao>> = obrigacaoDao.observarTodas()
    val progresso = configDao.observarProgresso()

    // ---------- Obrigações ----------
    suspend fun inserirObrigacao(titulo: String, prazo: Long?) {
        obrigacaoDao.inserir(Obrigacao(titulo = titulo, prazo = prazo))
    }

    suspend fun excluirObrigacao(o: Obrigacao) = obrigacaoDao.excluir(o)

    suspend fun alternarObrigacao(o: Obrigacao): Int {
        val concluida = !o.concluida
        val delta = if (concluida) XpCalculator.XP_OBRIGACAO else -XpCalculator.XP_OBRIGACAO
        db.withTransaction {
            obrigacaoDao.atualizar(o.copy(concluida = concluida))
            configDao.adicionarXp(delta)
        }
        return delta
    }

    // ---------- Metas e submetas ----------
    suspend fun inserirMeta(titulo: String) {
        metaDao.inserirMeta(Meta(titulo = titulo))
    }

    suspend fun excluirMeta(meta: Meta) = metaDao.excluirMeta(meta)

    /** Adicionar submeta a uma meta já concluída reabre a meta (e devolve o XP dela). */
    suspend fun adicionarSubMeta(meta: Meta, titulo: String): Int = db.withTransaction {
        metaDao.inserirSubMeta(SubMeta(metaId = meta.id, titulo = titulo))
        if (meta.concluida) {
            metaDao.atualizarMeta(meta.copy(concluida = false))
            configDao.adicionarXp(-XpCalculator.XP_META)
            -XpCalculator.XP_META
        } else 0
    }

    /** Para metas sem submetas: o checkbox da própria meta. */
    suspend fun alternarMetaSimples(meta: Meta): Int {
        val concluida = !meta.concluida
        val delta = if (concluida) XpCalculator.XP_META else -XpCalculator.XP_META
        db.withTransaction {
            metaDao.atualizarMeta(meta.copy(concluida = concluida))
            configDao.adicionarXp(delta)
        }
        return delta
    }

    /** Marca/desmarca a submeta; a meta conclui sozinha quando todas as submetas estão feitas. */
    suspend fun alternarSubMeta(item: MetaComSubMetas, sub: SubMeta): Int = db.withTransaction {
        val nova = sub.copy(concluida = !sub.concluida)
        metaDao.atualizarSubMeta(nova)
        var delta = if (nova.concluida) XpCalculator.XP_SUBMETA else -XpCalculator.XP_SUBMETA

        val todasFeitas = item.subMetas.all { if (it.id == sub.id) nova.concluida else it.concluida }
        if (todasFeitas != item.meta.concluida) {
            metaDao.atualizarMeta(item.meta.copy(concluida = todasFeitas))
            delta += if (todasFeitas) XpCalculator.XP_META else -XpCalculator.XP_META
        }
        configDao.adicionarXp(delta)
        delta
    }

    companion object {
        @Volatile private var instancia: AppRepository? = null

        fun get(context: Context): AppRepository =
            instancia ?: synchronized(this) {
                instancia ?: AppRepository(AppDatabase.get(context)).also { instancia = it }
            }
    }
}