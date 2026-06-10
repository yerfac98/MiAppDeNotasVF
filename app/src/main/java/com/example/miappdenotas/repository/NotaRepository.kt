package com.example.miappdenotas.repository

import androidx.lifecycle.LiveData
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.model.NotaDao

class NotaRepository(private val notaDao: NotaDao) {

    suspend fun insertar(nota: Nota) {
        notaDao.insertar(nota)
    }

    suspend fun insertarLista(notas: List<Nota>) {
        notaDao.insertarLista(notas)
    }

    suspend fun actualizar(nota: Nota) {
        notaDao.actualizar(nota)
    }

    suspend fun eliminar(nota: Nota) {
        notaDao.eliminar(nota)
    }

    suspend fun obtenerTodasLasNotasList(userId: String): List<Nota> {
        return notaDao.obtenerTodasLasNotasList(userId)
    }

    suspend fun reemplazarTodasLasNotas(notas: List<Nota>, userId: String) {
        notaDao.reemplazarTodasLasNotas(notas, userId)
    }

    fun buscarNotas(searchQuery: String, userId: String): LiveData<List<Nota>> {
        return notaDao.buscarNotas(searchQuery, userId)
    }

    fun buscarNotasFavoritas(searchQuery: String, userId: String): LiveData<List<Nota>> {
        return notaDao.buscarNotasFavoritas(searchQuery, userId)
    }

    fun obtenerNotasPorFechaDesc(userId: String): LiveData<List<Nota>> {
        return notaDao.obtenerNotasPorFechaDesc(userId)
    }

    fun obtenerNotasPorFechaAsc(userId: String): LiveData<List<Nota>> {
        return notaDao.obtenerNotasPorFechaAsc(userId)
    }

    fun obtenerFavoritasPorFechaDesc(userId: String): LiveData<List<Nota>> {
        return notaDao.obtenerFavoritasPorFechaDesc(userId)
    }

    fun obtenerFavoritasPorFechaAsc(userId: String): LiveData<List<Nota>> {
        return notaDao.obtenerFavoritasPorFechaAsc(userId)
    }

    fun obtenerNotasEliminadas(userId: String): LiveData<List<Nota>> {
        return notaDao.obtenerNotasEliminadas(userId)
    }
    suspend fun asignarNotasLocalesAlUsuario(userId: String) {
        notaDao.asignarNotasLocalesAlUsuario(userId)
    }
}