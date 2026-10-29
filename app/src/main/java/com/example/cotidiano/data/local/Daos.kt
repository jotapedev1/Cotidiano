package com.example.cotidiano.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaDao {
    @Transaction
    @Query("SELECT * FROM metas ORDER BY id DESC")
    fun observarTodas(): Flow<List<MetaComSubMetas>>

    @Insert suspend fun inserirMeta(meta: Meta): Long
    @Insert suspend fun inserirSubMeta(sub: SubMeta): Long
    @Update suspend fun atualizarMeta(meta: Meta)
    @Update suspend fun atualizarSubMeta(sub: SubMeta)
    @Delete suspend fun excluirMeta(meta: Meta)
}

@Dao
interface ObrigacaoDao {
    @Query("SELECT * FROM obrigacoes ORDER BY concluida, prazo")
    fun observarTodas(): Flow<List<Obrigacao>>
    @Insert suspend fun inserir(o: Obrigacao): Long
    @Update suspend fun atualizar(o: Obrigacao)
    @Delete suspend fun excluir(o: Obrigacao)
}

@Dao
interface CronogramaDao {
    @Query("SELECT * FROM blocos_cronograma WHERE diaSemana = :dia ORDER BY inicioMin")
    fun observarDia(dia: Int): Flow<List<BlocoCronograma>>
    @Insert suspend fun inserir(b: BlocoCronograma): Long
    @Update suspend fun atualizar(b: BlocoCronograma)
    @Delete suspend fun excluir(b: BlocoCronograma)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config_sono WHERE id = 1")
    fun observarSono(): Flow<ConfigSono?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarSono(c: ConfigSono)

    @Query("SELECT * FROM progresso WHERE id = 1")
    fun observarProgresso(): Flow<Progresso?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarProgresso(p: Progresso)
    @Query("UPDATE progresso SET xpTotal = xpTotal + :xp WHERE id = 1")
    suspend fun adicionarXp(xp: Int)
}

@Dao
interface PomodoroDao {
    @Insert suspend fun inserir(s: SessaoPomodoro): Long
    @Query("SELECT COUNT(*) FROM sessoes_pomodoro WHERE completa = 1 AND inicio >= :desde")
    fun contarCompletasDesde(desde: Long): Flow<Int>
}