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
    version = 3,
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

        fun obtenerInstancia(context: Context): NotaDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    NotaDatabase::class.java,
                    "nota_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()

                INSTANCIA = instancia
                instancia
            }
        }
    }
}