package com.example.miappdenotas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.model.NotaDao

@Database(
    entities = [Nota::class],
    version = 5,
    exportSchema = false
)
abstract class NotaDatabase : RoomDatabase() {

    abstract fun obtenerNotaDao(): NotaDao

    companion object {
        @Volatile
        private var INSTANCIA: NotaDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notas_table ADD COLUMN favorita INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notas_table ADD COLUMN eliminada INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notas_table ADD COLUMN fechaModificacion INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "UPDATE notas_table SET fechaModificacion = fecha WHERE fechaModificacion = 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notas_table ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun obtenerInstancia(context: Context): NotaDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    NotaDatabase::class.java,
                    "nota_db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()

                INSTANCIA = instancia
                instancia
            }
        }
    }
}