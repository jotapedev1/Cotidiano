package com.example.cotidiano.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Meta::class, SubMeta::class, Obrigacao::class, BlocoCronograma::class,
        ConfigSono::class, SessaoPomodoro::class, Progresso::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
    abstract fun obrigacaoDao(): ObrigacaoDao
    abstract fun cronogramaDao(): CronogramaDao
    abstract fun configDao(): ConfigDao
    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "cotidiano.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { instancia = it }
            }
    }
}