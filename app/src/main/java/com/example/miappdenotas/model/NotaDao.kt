package com.example.miappdenotas.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface NotaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(nota: Nota)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(notas: List<Nota>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun actualizar(nota: Nota)

    @Delete
    suspend fun eliminar(nota: Nota)

    // Ordena mostrando primero las notas más recientes
    @Query("SELECT * FROM notas_table ORDER BY fecha DESC")
    fun obtenerNotasPorFechaDesc(): LiveData<List<Nota>>

    // Ordena mostrando primero las notas más antiguas
    @Query("SELECT * FROM notas_table ORDER BY fecha ASC")
    fun obtenerNotasPorFechaAsc(): LiveData<List<Nota>>

    // Busca en título o contenido, mostrando primero lo más reciente
    @Query("SELECT * FROM notas_table WHERE titulo LIKE :searchQuery OR contenido LIKE :searchQuery ORDER BY fecha DESC")
    fun buscarNotas(searchQuery: String): LiveData<List<Nota>>

    @Query("SELECT * FROM notas_table ORDER BY fecha DESC")
    suspend fun obtenerTodasLasNotasList(): List<Nota>

    @Query("DELETE FROM notas_table")
    suspend fun eliminarTodasLasNotas()

    @Transaction
    suspend fun reemplazarTodasLasNotas(notas: List<Nota>) {
        eliminarTodasLasNotas()
        insertarLista(notas)
    }
}