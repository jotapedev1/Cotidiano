package com.example.cotidiano.dados.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Todas as consultas do banco ficam aqui.
 * - Funções que retornam Flow avisam a tela sozinhas quando os dados mudam.
 * - Funções "suspend" rodam fora da thread principal (corrotinas).
 */
@Dao
interface AppDao {

    // ---------- Usuários e XP ----------
    @Insert suspend fun inserirUsuario(u: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT xp FROM usuarios WHERE id = :uid")
    fun observarXp(uid: Long): Flow<Int?>

    // MAX(0, ...) impede o XP de ficar negativo ao desmarcar algo
    @Query("UPDATE usuarios SET xp = MAX(0, xp + :delta) WHERE id = :uid")
    suspend fun somarXp(uid: Long, delta: Int)

    // ---------- Tarefas diárias ----------
    @Query("SELECT * FROM tarefas_diarias WHERE usuarioId = :uid ORDER BY id")
    fun observarTarefas(uid: Long): Flow<List<TarefaDiaria>>

    @Insert suspend fun inserirTarefa(t: TarefaDiaria)
    @Update suspend fun atualizarTarefa(t: TarefaDiaria)
    @Delete suspend fun excluirTarefa(t: TarefaDiaria)

    // ---------- Metas e submetas ----------
    // @Transaction: busca meta + submetas de forma consistente
    @Transaction
    @Query("SELECT * FROM metas WHERE usuarioId = :uid ORDER BY id DESC")
    fun observarMetas(uid: Long): Flow<List<MetaComSubMetas>>

    @Insert suspend fun inserirMeta(m: Meta)
    @Insert suspend fun inserirSubMeta(s: SubMeta)
    @Update suspend fun atualizarMeta(m: Meta)
    @Update suspend fun atualizarSubMeta(s: SubMeta)
    @Delete suspend fun excluirMeta(m: Meta)

    // ---------- Obrigações ----------
    // Ordem: pendentes primeiro; dentro delas, as com prazo mais próximo; sem prazo por último
    @Query("SELECT * FROM obrigacoes WHERE usuarioId = :uid ORDER BY concluida, prazo IS NULL, prazo")
    fun observarObrigacoes(uid: Long): Flow<List<Obrigacao>>

    @Insert suspend fun inserirObrigacao(o: Obrigacao)
    @Update suspend fun atualizarObrigacao(o: Obrigacao)
    @Delete suspend fun excluirObrigacao(o: Obrigacao)

    // ---------- Cronograma ----------
    @Query("SELECT * FROM blocos_cronograma WHERE usuarioId = :uid AND diaSemana = :dia ORDER BY inicioMin")
    fun observarBlocos(uid: Long, dia: Int): Flow<List<BlocoCronograma>>

    @Insert suspend fun inserirBloco(b: BlocoCronograma)
    @Update suspend fun atualizarBloco(b: BlocoCronograma)
    @Delete suspend fun excluirBloco(b: BlocoCronograma)

    // ---------- Pomodoro ----------
    @Insert suspend fun inserirSessao(s: SessaoPomodoro)
}
