package com.example.cotidiano.dados.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert suspend fun inserir(u: Usuario): Long
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?
}

@Dao
interface TarefaDiariaDao {
    @Query("SELECT * FROM tarefas_diarias WHERE usuarioId = :uid ORDER BY id")
    fun observarTodas(uid: Long): Flow<List<TarefaDiaria>>
    @Insert suspend fun inserir(t: TarefaDiaria): Long
    @Update suspend fun atualizar(t: TarefaDiaria)
    @Delete suspend fun excluir(t: TarefaDiaria)
}

@Dao
interface MetaDao {
    @Transaction
    @Query("SELECT * FROM metas WHERE usuarioId = :uid ORDER BY id DESC")
    fun observarTodas(uid: Long): Flow<List<MetaComSubMetas>>

    @Insert suspend fun inserirMeta(meta: Meta): Long
    @Insert suspend fun inserirSubMeta(sub: SubMeta): Long
    @Update suspend fun atualizarMeta(meta: Meta)
    @Update suspend fun atualizarSubMeta(sub: SubMeta)
    @Delete suspend fun excluirMeta(meta: Meta)
}

@Dao
interface ObrigacaoDao {
    @Query("SELECT * FROM obrigacoes WHERE usuarioId = :uid ORDER BY concluida, prazo IS NULL, prazo")
    fun observarTodas(uid: Long): Flow<List<Obrigacao>>
    @Insert suspend fun inserir(o: Obrigacao): Long
    @Update suspend fun atualizar(o: Obrigacao)
    @Delete suspend fun excluir(o: Obrigacao)
}

@Dao
interface CronogramaDao {
    @Query("SELECT * FROM blocos_cronograma WHERE usuarioId = :uid AND diaSemana = :dia ORDER BY inicioMin")
    fun observarDia(uid: Long, dia: Int): Flow<List<BlocoCronograma>>
    @Insert suspend fun inserir(b: BlocoCronograma): Long
    @Update suspend fun atualizar(b: BlocoCronograma)
    @Delete suspend fun excluir(b: BlocoCronograma)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config_sono WHERE usuarioId = :uid")
    fun observarSono(uid: Long): Flow<ConfigSono?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarSono(c: ConfigSono)

    @Query("SELECT * FROM progresso WHERE usuarioId = :uid")
    fun observarProgresso(uid: Long): Flow<Progresso?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarProgresso(p: Progresso)
    @Query("UPDATE progresso SET xpTotal = MAX(0, xpTotal + :xp) WHERE usuarioId = :uid")
    suspend fun adicionarXp(uid: Long, xp: Int)
}

@Dao
interface PomodoroDao {
    @Insert suspend fun inserir(s: SessaoPomodoro): Long
    @Query("SELECT COUNT(*) FROM sessoes_pomodoro WHERE usuarioId = :uid AND completa = 1 AND inicio >= :desde")
    fun contarCompletasDesde(uid: Long, desde: Long): Flow<Int>
}