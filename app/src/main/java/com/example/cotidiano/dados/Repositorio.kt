package com.example.cotidiano.dados

import android.content.Context
import androidx.room.withTransaction
import com.example.cotidiano.dados.local.BancoDeDados
import com.example.cotidiano.dados.local.BlocoCronograma
import com.example.cotidiano.dados.local.ConfigSono
import com.example.cotidiano.dados.local.Meta
import com.example.cotidiano.dados.local.MetaComSubMetas
import com.example.cotidiano.dados.local.Obrigacao
import com.example.cotidiano.dados.local.Progresso
import com.example.cotidiano.dados.local.SessaoPomodoro
import com.example.cotidiano.dados.local.SubMeta
import com.example.cotidiano.dados.local.TarefaDiaria
import com.example.cotidiano.dados.local.Usuario
import com.example.cotidiano.dominio.CalculadoraXp
import com.example.cotidiano.dominio.Seguranca
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext

/**
 * Fonte única de dados. Tudo é filtrado pelo usuário logado (Sessao).
 * As funções "alternar*" devolvem o delta de XP aplicado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Repositorio private constructor(
    private val db: BancoDeDados,
    private val sessao: Sessao
) {
    private val usuarioDao = db.usuarioDao()
    private val tarefaDao = db.tarefaDao()
    private val metaDao = db.metaDao()
    private val obrigacaoDao = db.obrigacaoDao()
    private val configDao = db.configDao()

    private val uid: Long get() = sessao.usuarioId.value
    private val usuario = sessao.usuarioId

    val tarefas: Flow<List<TarefaDiaria>> = usuario.flatMapLatest { tarefaDao.observarTodas(it) }
    val metas: Flow<List<MetaComSubMetas>> = usuario.flatMapLatest { metaDao.observarTodas(it) }
    val obrigacoes: Flow<List<Obrigacao>> = usuario.flatMapLatest { obrigacaoDao.observarTodas(it) }
    val progresso: Flow<Progresso?> = usuario.flatMapLatest { configDao.observarProgresso(it) }

    // ---------- Autenticação ----------
    val logado: Boolean get() = sessao.logado

    /** @return false se o e-mail já estiver cadastrado. */
    suspend fun cadastrar(nome: String, email: String, senha: String): Boolean {
        val emailNorm = email.trim().lowercase()
        val salt = Seguranca.novoSalt()
        val hash = withContext(Dispatchers.Default) { Seguranca.hash(senha, salt) }
        return db.withTransaction {
            if (usuarioDao.buscarPorEmail(emailNorm) != null) return@withTransaction false
            val id = usuarioDao.inserir(
                Usuario(nome = nome, email = emailNorm, senhaHash = hash, salt = salt)
            )
            configDao.salvarProgresso(Progresso(usuarioId = id))
            configDao.salvarSono(ConfigSono(usuarioId = id))
            true
        }
    }

    /** @return true e abre a sessão se e-mail e senha conferem. */
    suspend fun entrar(email: String, senha: String): Boolean {
        val u = usuarioDao.buscarPorEmail(email.trim().lowercase()) ?: return false
        val ok = withContext(Dispatchers.Default) { Seguranca.verificar(senha, u.salt, u.senhaHash) }
        if (ok) sessao.entrar(u.id)
        return ok
    }

    fun sair() = sessao.sair()

    // ---------- Tarefas diárias ----------
    suspend fun inserirTarefa(titulo: String) {
        tarefaDao.inserir(TarefaDiaria(usuarioId = uid, titulo = titulo))
    }

    suspend fun excluirTarefa(t: TarefaDiaria) = tarefaDao.excluir(t)

    /** Concluir dá +10 XP; desmarcar no mesmo dia devolve. [hoje] = dia atual em dias desde 1970. */
    suspend fun alternarTarefa(t: TarefaDiaria, hoje: Long): Int {
        val u = uid
        val jaConcluida = t.ultimaConclusaoDia == hoje
        val delta = if (jaConcluida) -CalculadoraXp.XP_TAREFA else CalculadoraXp.XP_TAREFA
        db.withTransaction {
            tarefaDao.atualizar(t.copy(ultimaConclusaoDia = if (jaConcluida) null else hoje))
            configDao.adicionarXp(u, delta)
        }
        return delta
    }

    // ---------- Obrigações ----------
    suspend fun inserirObrigacao(titulo: String, prazo: Long?) {
        obrigacaoDao.inserir(Obrigacao(usuarioId = uid, titulo = titulo, prazo = prazo))
    }

    suspend fun excluirObrigacao(o: Obrigacao) = obrigacaoDao.excluir(o)

    suspend fun alternarObrigacao(o: Obrigacao): Int {
        val u = uid
        val concluida = !o.concluida
        val delta = if (concluida) CalculadoraXp.XP_OBRIGACAO else -CalculadoraXp.XP_OBRIGACAO
        db.withTransaction {
            obrigacaoDao.atualizar(o.copy(concluida = concluida))
            configDao.adicionarXp(u, delta)
        }
        return delta
    }

    // ---------- Metas e submetas ----------
    suspend fun inserirMeta(titulo: String) {
        metaDao.inserirMeta(Meta(usuarioId = uid, titulo = titulo))
    }

    suspend fun excluirMeta(meta: Meta) = metaDao.excluirMeta(meta)

    /** Adicionar submeta a uma meta já concluída reabre a meta (e devolve o XP dela). */
    suspend fun adicionarSubMeta(meta: Meta, titulo: String): Int {
        val u = uid
        return db.withTransaction {
            metaDao.inserirSubMeta(SubMeta(metaId = meta.id, titulo = titulo))
            if (meta.concluida) {
                metaDao.atualizarMeta(meta.copy(concluida = false))
                configDao.adicionarXp(u, -CalculadoraXp.XP_META)
                -CalculadoraXp.XP_META
            } else 0
        }
    }

    suspend fun alternarMetaSimples(meta: Meta): Int {
        val u = uid
        val concluida = !meta.concluida
        val delta = if (concluida) CalculadoraXp.XP_META else -CalculadoraXp.XP_META
        db.withTransaction {
            metaDao.atualizarMeta(meta.copy(concluida = concluida))
            configDao.adicionarXp(u, delta)
        }
        return delta
    }

    suspend fun alternarSubMeta(item: MetaComSubMetas, sub: SubMeta): Int {
        val u = uid
        return db.withTransaction {
            val nova = sub.copy(concluida = !sub.concluida)
            metaDao.atualizarSubMeta(nova)
            var delta = if (nova.concluida) CalculadoraXp.XP_SUBMETA else -CalculadoraXp.XP_SUBMETA

            val todasFeitas = item.subMetas.all { if (it.id == sub.id) nova.concluida else it.concluida }
            if (todasFeitas != item.meta.concluida) {
                metaDao.atualizarMeta(item.meta.copy(concluida = todasFeitas))
                delta += if (todasFeitas) CalculadoraXp.XP_META else -CalculadoraXp.XP_META
            }
            configDao.adicionarXp(u, delta)
            delta
        }
    }

    // ---------- Cronograma ----------
    fun observarCronogramaDia(dia: Int): Flow<List<BlocoCronograma>> =
        usuario.flatMapLatest { db.cronogramaDao().observarDia(it, dia) }

    suspend fun inserirBlocoCronograma(titulo: String, diaSemana: Int, inicioMin: Int, fimMin: Int, cor: Int) {
        db.cronogramaDao().inserir(
            BlocoCronograma(
                usuarioId = uid, titulo = titulo, diaSemana = diaSemana,
                inicioMin = inicioMin, fimMin = fimMin, cor = cor
            )
        )
    }

    suspend fun atualizarBlocoCronograma(bloco: BlocoCronograma) = db.cronogramaDao().atualizar(bloco)

    suspend fun excluirBlocoCronograma(bloco: BlocoCronograma) = db.cronogramaDao().excluir(bloco)

    // ---------- Pomodoro ----------
    suspend fun inserirSessaoPomodoro(minutos: Int) {
        val u = uid
        db.withTransaction {
            db.pomodoroDao().inserir(
                SessaoPomodoro(usuarioId = u, inicio = System.currentTimeMillis(), minutosFoco = minutos, completa = true)
            )
            configDao.adicionarXp(u, CalculadoraXp.XP_POMODORO)
        }
    }

    companion object {
        @Volatile private var instancia: Repositorio? = null

        fun obter(context: Context): Repositorio =
            instancia ?: synchronized(this) {
                instancia ?: Repositorio(
                    BancoDeDados.obter(context), Sessao.obter(context)
                ).also { instancia = it }
            }
    }
}