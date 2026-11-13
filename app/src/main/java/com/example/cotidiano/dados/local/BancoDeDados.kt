package com.example.cotidiano.dados.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Usuario::class, TarefaDiaria::class, Meta::class, SubMeta::class, Obrigacao::class,
        BlocoCronograma::class, ConfigSono::class, SessaoPomodoro::class, Progresso::class],
    version = 3,
    exportSchema = false
)
abstract class BancoDeDados : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun tarefaDao(): TarefaDiariaDao
    abstract fun metaDao(): MetaDao
    abstract fun obrigacaoDao(): ObrigacaoDao
    abstract fun cronogramaDao(): CronogramaDao
    abstract fun configDao(): ConfigDao
    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile private var instancia: BancoDeDados? = null

        fun obter(context: Context): BancoDeDados =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext, BancoDeDados::class.java, "cotidiano.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instancia = it }
            }
    }
}