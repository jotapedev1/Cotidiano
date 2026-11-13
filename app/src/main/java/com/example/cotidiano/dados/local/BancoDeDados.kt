package com.example.cotidiano.dados.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Banco SQLite do app (via Room). Sempre que mudar uma tabela, aumente "version".
 * Com fallbackToDestructiveMigration, o banco antigo é apagado e recriado
 * (ok para desenvolvimento; em produção seria preciso uma migração).
 */
@Database(
    entities = [Usuario::class, TarefaDiaria::class, Meta::class, SubMeta::class,
        Obrigacao::class, BlocoCronograma::class, SessaoPomodoro::class],
    version = 4,
    exportSchema = false
)
abstract class BancoDeDados : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        // Singleton: existe um único banco em todo o app
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
