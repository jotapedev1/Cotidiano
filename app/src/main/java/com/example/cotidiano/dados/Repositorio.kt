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
import com.example.cotidiano.dados.local.TarefaDiaria
import com.example.cotidiano.dados.local.Usuario
import com.example.cotidiano.dominio.CalculadoraXp
import com.example.cotidiano.dominio.Seguranca
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Ponto único de acesso aos dados. As telas (ViewModels) só falam com ele.
 * Tudo é filtrado pelo usuário logado.
 * As funções "alternar..." devolvem o XP ganho (+) ou perdido (-) para a tela mostrar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Repositorio private constructor(
    private val db: BancoDeDados,
    private val sessao: Sessao
) {
    private val dao = db.dao()
    private val usuario = sessao.usuarioId
    private val uid: Long get() = usuario.value

    // flatMapLatest: quando o usuário muda (login/logout), a consulta é refeita para o novo id
    val tarefas: Flow<List<TarefaDiaria>> = usuario.flatMapLatest { dao.observarTarefas(it) }
    val metas: Flow<List<MetaComSubMetas>> = usuario.flatMapLatest { dao.observarMetas(it) }
    val obrigacoes: Flow<List<Obrigacao>> = usuario.flatMapLatest { dao.observarObrigacoes(it) }
    val xp: Flow<Int?> = usuario.flatMapLatest { dao.observarXp(it) }

    fun observarBlocos(dia: Int): Flow<List<BlocoCronograma>> =
        usuario.flatMapLatest { dao.observarBlocos(it, dia) }

    /** Faz a alteração e soma o XP na MESMA transação: ou grava tudo, ou nada. */
    private suspend fun comXp(delta: Int, alteracao: suspend () -> Unit): Int {
        db.withTransaction {
            alteracao()
            dao.somarXp(uid, delta)
        }
        return delta
    }

    // ---------- Login ----------
    val logado: Boolean get() = sessao.logado

    /** Retorna false se o e-mail já estiver cadastrado. */
    suspend fun cadastrar(nome: String, email: String, senha: String): Boolean {
        val emailNorm = email.trim().lowercase()
        if (dao.buscarPorEmail(emailNorm) != null) return false
        dao.inserirUsuario(Usuario(nome = nome, email = emailNorm, senhaHash = Seguranca.hash(senha)))
        return true
    }

    /** Retorna true (e abre a sessão) se e-mail e senha conferem. */
    suspend fun entrar(email: String, senha: String): Boolean {
        val u = dao.buscarPorEmail(email.trim().lowercase()) ?: return false
        if (u.senhaHash != Seguranca.hash(senha)) return false
        sessao.entrar(u.id)
        return true
    }

    fun sair() = sessao.sair()

    // ---------- Tarefas diárias ----------
    suspend fun inserirTarefa(titulo: String) =
        dao.inserirTarefa(TarefaDiaria(usuarioId = uid, titulo = titulo))

    suspend fun excluirTarefa(t: TarefaDiaria) = dao.excluirTarefa(t)

    /** Marca/desmarca a tarefa de hoje. Concluída = guardamos o dia de hoje. */
    suspend fun alternarTarefa(t: TarefaDiaria, hoje: Long): Int {
        val jaFeita = t.ultimaConclusaoDia == hoje
        val delta = if (jaFeita) -CalculadoraXp.XP_TAREFA else CalculadoraXp.XP_TAREFA
        return comXp(delta) {
            dao.atualizarTarefa(t.copy(ultimaConclusaoDia = if (jaFeita) null else hoje))
        }
    }

    // ---------- Obrigações ----------
    suspend fun inserirObrigacao(titulo: String, prazo: Long?) =
        dao.inserirObrigacao(Obrigacao(usuarioId = uid, titulo = titulo, prazo = prazo))

    suspend fun excluirObrigacao(o: Obrigacao) = dao.excluirObrigacao(o)

    suspend fun alternarObrigacao(o: Obrigacao): Int {
        val delta = if (o.concluida) -CalculadoraXp.XP_OBRIGACAO else CalculadoraXp.XP_OBRIGACAO
        return comXp(delta) { dao.atualizarObrigacao(o.copy(concluida = !o.concluida)) }
    }

    // ---------- Metas e submetas ----------
    suspend fun inserirMeta(titulo: String) = dao.inserirMeta(Meta(usuarioId = uid, titulo = titulo))

    suspend fun excluirMeta(meta: Meta) = dao.excluirMeta(meta)

    /** Meta sem submetas: o checkbox da própria meta vale. */
    suspend fun alternarMetaSimples(meta: Meta): Int {
        val delta = if (meta.concluida) -CalculadoraXp.XP_META else CalculadoraXp.XP_META
        return comXp(delta) { dao.atualizarMeta(meta.copy(concluida = !meta.concluida)) }
    }

    /** Nova submeta numa meta concluída reabre a meta (e devolve o XP dela). */
    suspend fun adicionarSubMeta(meta: Meta, titulo: String): Int {
        val delta = if (meta.concluida) -CalculadoraXp.XP_META else 0
        return comXp(delta) {
            dao.inserirSubMeta(SubMeta(metaId = meta.id, titulo = titulo))
            if (meta.concluida) dao.atualizarMeta(meta.copy(concluida = false))
        }
    }

    /** Marca/desmarca a submeta; a meta conclui sozinha quando todas as submetas estão feitas. */
    suspend fun alternarSubMeta(item: MetaComSubMetas, sub: SubMeta): Int {
        val nova = sub.copy(concluida = !sub.concluida)
        var delta = if (nova.concluida) CalculadoraXp.XP_SUBMETA else -CalculadoraXp.XP_SUBMETA

        // Como ficam as submetas depois desta mudança? Todas feitas => meta concluída
        val todasFeitas = item.subMetas.all { if (it.id == sub.id) nova.concluida else it.concluida }
        val metaMudou = todasFeitas != item.meta.concluida
        if (metaMudou) delta += if (todasFeitas) CalculadoraXp.XP_META else -CalculadoraXp.XP_META

        return comXp(delta) {
            dao.atualizarSubMeta(nova)
            if (metaMudou) dao.atualizarMeta(item.meta.copy(concluida = todasFeitas))
        }
    }

    // ---------- Cronograma ----------
    suspend fun inserirBloco(titulo: String, dia: Int, inicioMin: Int, fimMin: Int, cor: Int) =
        dao.inserirBloco(
            BlocoCronograma(usuarioId = uid, titulo = titulo, diaSemana = dia,
                inicioMin = inicioMin, fimMin = fimMin, cor = cor)
        )

    suspend fun atualizarBloco(b: BlocoCronograma) = dao.atualizarBloco(b)

    suspend fun excluirBloco(b: BlocoCronograma) = dao.excluirBloco(b)

    // ---------- Pomodoro ----------
    suspend fun registrarPomodoro(minutos: Int): Int =
        comXp(CalculadoraXp.XP_POMODORO) {
            dao.inserirSessao(
                SessaoPomodoro(usuarioId = uid, inicio = System.currentTimeMillis(), minutosFoco = minutos)
            )
        }

    companion object {
        // Singleton: um único repositório em todo o app
        @Volatile private var instancia: Repositorio? = null

        fun obter(context: Context): Repositorio =
            instancia ?: synchronized(this) {
                instancia ?: Repositorio(
                    BancoDeDados.obter(context), Sessao.obter(context)
                ).also { instancia = it }
            }
    }
}
