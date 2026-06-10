package com.example.miappdenotas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.repository.NotaRepository
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

        val newSource = if (!query.isNullOrEmpty() && query != "%%") {
            if (showOnlyFavorites) {
                repository.buscarNotasFavoritas(query)
            } else {
                repository.buscarNotas(query)
            }
        } else {
            if (showOnlyFavorites) {
                when (order) {
                    SortOrder.DATE_DESC -> repository.obtenerFavoritasPorFechaDesc()
                    SortOrder.DATE_ASC -> repository.obtenerFavoritasPorFechaAsc()
                }
            } else {
                when (order) {
                    SortOrder.DATE_DESC -> repository.obtenerNotasPorFechaDesc()
                    SortOrder.DATE_ASC -> repository.obtenerNotasPorFechaAsc()
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

    fun insertar(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertar(nota)
    }

    fun insertarLista(notas: List<Nota>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertarLista(notas)
    }

    fun actualizar(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        repository.actualizar(nota)
    }

    fun eliminar(nota: Nota) = viewModelScope.launch(Dispatchers.IO) {
        repository.eliminar(nota)
    }

    suspend fun obtenerTodasLasNotasList(): List<Nota> {
        return withContext(Dispatchers.IO) {
            repository.obtenerTodasLasNotasList()
        }
    }

    fun reemplazarTodasLasNotas(notas: List<Nota>) = viewModelScope.launch(Dispatchers.IO) {
        repository.reemplazarTodasLasNotas(notas)
    }

    fun obtenerNotasEliminadas(): LiveData<List<Nota>> {
        return repository.obtenerNotasEliminadas()
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