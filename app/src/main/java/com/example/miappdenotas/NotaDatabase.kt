package com.example.miappdenotas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.model.NotaDao

@Database(
    entities = [Nota::class],
    version = 1,
    exportSchema = false
)
abstract class NotaDatabase : RoomDatabase() {

    abstract fun obtenerNotaDao(): NotaDao

    companion object {
        @Volatile
        private var INSTANCIA: NotaDatabase? = null

        fun obtenerInstancia(context: Context): NotaDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    NotaDatabase::class.java,
                    "nota_db"
                ).build()

                INSTANCIA = instancia
                instancia
            }
        }
    }
}