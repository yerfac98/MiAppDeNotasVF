package com.example.miappdenotas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.repository.NotaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SortOrder {
    DATE_DESC,
    DATE_ASC
}

class NotaViewModel(private val repository: NotaRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>()
    private val _sortOrder = MutableLiveData<SortOrder>()
    private val _showOnlyFavorites = MutableLiveData<Boolean>()

    val notasFiltradas = MediatorLiveData<List<Nota>>()

    private var currentSource: LiveData<List<Nota>>? = null

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        _searchQuery.value = ""
        _sortOrder.value = SortOrder.DATE_DESC
        _showOnlyFavorites.value = false

        notasFiltradas.addSource(_searchQuery) {
            updateNotesSource()
        }

        notasFiltradas.addSource(_sortOrder) {
            updateNotesSource()
        }

        notasFiltradas.addSource(_showOnlyFavorites) {
            updateNotesSource()
        }
    }

    private fun updateNotesSource() {
        val query = _searchQuery.value
        val order = _sortOrder.value ?: SortOrder.DATE_DESC
        val showOnlyFavorites = _showOnlyFavorites.value ?: false
        val currentUserId = userId

        val newSource = if (!query.isNullOrEmpty() && query != "%%") {
            if (showOnlyFavorites) {
                repository.buscarNotasFavoritas(query, currentUserId)
            } else {
                repository.buscarNotas(query, currentUserId)
            }
        } else {
            if (showOnlyFavorites) {
                when (order) {
                    SortOrder.DATE_DESC -> repository.obtenerFavoritasPorFechaDesc(currentUserId)
                    SortOrder.DATE_ASC -> repository.obtenerFavoritasPorFechaAsc(currentUserId)
                }
            } else {
                when (order) {
                    SortOrder.DATE_DESC -> repository.obtenerNotasPorFechaDesc(currentUserId)
                    SortOrder.DATE_ASC -> repository.obtenerNotasPorFechaAsc(currentUserId)
                }
            }
        }

        if (newSource == currentSource) return

        currentSource?.let {
            notasFiltradas.removeSource(it)
        }

        notasFiltradas.addSource(newSource) { notes ->
            notasFiltradas.value = notes
        }

        currentSource = newSource
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        if (_sortOrder.value != order) {
            _sortOrder.value = order
        }
    }

    fun toggleFavoritesFilter(): Boolean {
        val newValue = !(_showOnlyFavorites.value ?: false)
        _showOnlyFavorites.value = newValue
        return newValue
    }

    fun isShowingOnlyFavorites(): Boolean {
        return _showOnlyFavorites.value ?: false
    }

    fun insertar(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        val notaConUsuario = nota.copy(userId = userId)
        repository.insertar(notaConUsuario)
    }

    fun insertarLista(notas: List<Nota>) = viewModelScope.launch(Dispatchers.IO) {
        val notasConUsuario = notas.map { it.copy(userId = userId) }
        repository.insertarLista(notasConUsuario)
    }

    fun actualizar(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        val notaConUsuario = nota.copy(userId = userId)
        repository.actualizar(notaConUsuario)
    }

    fun eliminar(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        repository.eliminar(nota)
    }

    suspend fun obtenerTodasLasNotasList(): List<Nota> {
        return withContext(Dispatchers.IO) {
            repository.obtenerTodasLasNotasList(userId)
        }
    }

    fun reemplazarTodasLasNotas(notas: List<Nota>) = viewModelScope.launch(Dispatchers.IO) {
        val notasConUsuario = notas.map { it.copy(userId = userId) }
        repository.reemplazarTodasLasNotas(notasConUsuario, userId)
    }

    fun obtenerNotasEliminadas(): LiveData<List<Nota>> {
        return repository.obtenerNotasEliminadas(userId)
    }
    fun asignarNotasLocalesAlUsuario() = viewModelScope.launch(Dispatchers.IO) {
        val currentUserId = userId
        if (currentUserId.isNotEmpty()) {
            repository.asignarNotasLocalesAlUsuario(currentUserId)
        }
    }

    companion object {
        class NotaViewModelFactory(
            private val repository: NotaRepository
        ) : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NotaViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return NotaViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}