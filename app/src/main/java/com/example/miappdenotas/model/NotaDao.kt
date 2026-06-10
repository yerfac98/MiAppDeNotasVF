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

    @Query("SELECT * FROM notas_table WHERE eliminada = 0 AND userId = :userId ORDER BY fecha DESC")
    fun obtenerNotasPorFechaDesc(userId: String): LiveData<List<Nota>>

    @Query("SELECT * FROM notas_table WHERE eliminada = 0 AND userId = :userId ORDER BY fecha ASC")
    fun obtenerNotasPorFechaAsc(userId: String): LiveData<List<Nota>>

    @Query("SELECT * FROM notas_table WHERE favorita = 1 AND eliminada = 0 AND userId = :userId ORDER BY fecha DESC")
    fun obtenerFavoritasPorFechaDesc(userId: String): LiveData<List<Nota>>

    @Query("SELECT * FROM notas_table WHERE favorita = 1 AND eliminada = 0 AND userId = :userId ORDER BY fecha ASC")
    fun obtenerFavoritasPorFechaAsc(userId: String): LiveData<List<Nota>>

    @Query("""
        SELECT * FROM notas_table 
        WHERE eliminada = 0
        AND userId = :userId
        AND (titulo LIKE :searchQuery OR contenido LIKE :searchQuery) 
        ORDER BY fecha DESC
    """)
    fun buscarNotas(searchQuery: String, userId: String): LiveData<List<Nota>>

    @Query("""
        SELECT * FROM notas_table 
        WHERE eliminada = 0
        AND favorita = 1 
        AND userId = :userId
        AND (titulo LIKE :searchQuery OR contenido LIKE :searchQuery) 
        ORDER BY fecha DESC
    """)
    fun buscarNotasFavoritas(searchQuery: String, userId: String): LiveData<List<Nota>>

    @Query("SELECT * FROM notas_table WHERE eliminada = 0 AND userId = :userId ORDER BY fecha DESC")
    suspend fun obtenerTodasLasNotasList(userId: String): List<Nota>

    @Query("SELECT * FROM notas_table WHERE eliminada = 1 AND userId = :userId ORDER BY fecha DESC")
    fun obtenerNotasEliminadas(userId: String): LiveData<List<Nota>>

    @Query("DELETE FROM notas_table WHERE userId = :userId")
    suspend fun eliminarTodasLasNotas(userId: String)

    @Query("UPDATE notas_table SET userId = :userId WHERE userId = ''")
    suspend fun asignarNotasLocalesAlUsuario(userId: String)
    @Transaction
    suspend fun reemplazarTodasLasNotas(notas: List<Nota>, userId: String) {
        eliminarTodasLasNotas(userId)
        insertarLista(notas)
    }
}