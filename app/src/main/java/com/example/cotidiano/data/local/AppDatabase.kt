package com.example.cotidiano.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

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
                    // onOpen + OR IGNORE: garante as linhas singleton mesmo em bancos já criados
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            db.execSQL("INSERT OR IGNORE INTO progresso (id, xpTotal) VALUES (1, 0)")
                            db.execSQL(
                                "INSERT OR IGNORE INTO config_sono (id, dormirMin, acordarMin) " +
                                        "VALUES (1, ${22 * 60}, ${6 * 60})"
                            )
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
    }
}