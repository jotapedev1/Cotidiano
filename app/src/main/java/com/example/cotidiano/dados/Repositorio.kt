package com.example.cotidiano.dados

import android.content.Context
import androidx.room.withTransaction
import com.example.cotidiano.dados.local.BancoDeDados
import com.example.cotidiano.dados.local.BlocoCronograma
import com.example.cotidiano.dados.local.Meta
import com.example.cotidiano.dados.local.MetaComSubMetas
import com.example.cotidiano.dados.local.Obrigacao
import com.example.cotidiano.dados.local.SessaoPomodoro
import com.example.cotidiano.dados.local.SubMeta
import com.example.cotidiano.dominio.CalculadoraXp
import kotlinx.coroutines.flow.Flow

/**
 * Fonte única de dados. As funções "alternar*" devolvem o delta de XP aplicado
 * (positivo ao concluir, negativo ao desmarcar) para a UI poder dar feedback.
 */
class Repositorio private constructor(private val db: BancoDeDados) {

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
        val delta = if (concluida) CalculadoraXp.XP_OBRIGACAO else -CalculadoraXp.XP_OBRIGACAO
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
            configDao.adicionarXp(-CalculadoraXp.XP_META)
            -CalculadoraXp.XP_META
        } else 0
    }

    /** Para metas sem submetas: o checkbox da própria meta. */
    suspend fun alternarMetaSimples(meta: Meta): Int {
        val concluida = !meta.concluida
        val delta = if (concluida) CalculadoraXp.XP_META else -CalculadoraXp.XP_META
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
        var delta = if (nova.concluida) CalculadoraXp.XP_SUBMETA else -CalculadoraXp.XP_SUBMETA

        val todasFeitas = item.subMetas.all { if (it.id == sub.id) nova.concluida else it.concluida }
        if (todasFeitas != item.meta.concluida) {
            metaDao.atualizarMeta(item.meta.copy(concluida = todasFeitas))
            delta += if (todasFeitas) CalculadoraXp.XP_META else -CalculadoraXp.XP_META
        }
        configDao.adicionarXp(delta)
        delta
    }

    // ---------- Cronograma ----------
    fun observarCronogramaDia(dia: Int): Flow<List<BlocoCronograma>> = db.cronogramaDao().observarDia(dia)

    suspend fun inserirBlocoCronograma(titulo: String, diaSemana: Int, inicioMin: Int, fimMin: Int, cor: Int) {
        db.cronogramaDao().inserir(BlocoCronograma(titulo = titulo, diaSemana = diaSemana, inicioMin = inicioMin, fimMin = fimMin, cor = cor))
    }

    suspend fun atualizarBlocoCronograma(bloco: BlocoCronograma) {
        db.cronogramaDao().atualizar(bloco)
    }

    suspend fun excluirBlocoCronograma(bloco: BlocoCronograma) {
        db.cronogramaDao().excluir(bloco)
    }

    // ---------- Pomodoro ----------
    suspend fun inserirSessaoPomodoro(minutos: Int) = db.withTransaction {
        db.pomodoroDao().inserir(SessaoPomodoro(inicio = System.currentTimeMillis(), minutosFoco = minutos, completa = true))
        configDao.adicionarXp(CalculadoraXp.XP_POMODORO)
    }

    companion object {
        @Volatile private var instancia: Repositorio? = null

        fun obter(context: Context): Repositorio =
            instancia ?: synchronized(this) {
                instancia ?: Repositorio(BancoDeDados.obter(context)).also { instancia = it }
            }
    }
}
