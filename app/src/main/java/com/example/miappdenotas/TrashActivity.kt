package com.example.miappdenotas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.repository.NotaRepository
import com.example.miappdenotas.viewmodel.NotaViewModel
import com.example.miappdenotas.viewmodel.NotaViewModelFactory

class TrashActivity : AppCompatActivity(), TrashAdapter.OnTrashItemClickListener {

    private lateinit var notaViewModel: NotaViewModel
    private lateinit var adapter: TrashAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        observeDeletedNotes()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_trash)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Papelera"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupViewModel() {
        val notaDao = NotaDatabase.obtenerInstancia(application).obtenerNotaDao()
        val repository = NotaRepository(notaDao)
        val factory = NotaViewModelFactory(repository)

        notaViewModel = ViewModelProvider(this, factory)[NotaViewModel::class.java]
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_trash)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter = TrashAdapter()
        recyclerView.adapter = adapter
        adapter.setOnTrashItemClickListener(this)
    }

    private fun observeDeletedNotes() {
        notaViewModel.obtenerNotasEliminadas().observe(this) { notas ->
            adapter.submitList(notas)
        }
    }

    override fun onTrashItemClick(nota: Nota) {
        val opciones = arrayOf("Restaurar", "Eliminar definitivamente", "Cancelar")

        AlertDialog.Builder(this)
            .setTitle(nota.titulo)
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> restaurarNota(nota)
                    1 -> confirmarEliminacionDefinitiva(nota)
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun restaurarNota(nota: Nota) {
        val notaRestaurada = nota.copy(eliminada = false)
        notaViewModel.actualizar(notaRestaurada)
        Toast.makeText(this, "Nota restaurada", Toast.LENGTH_SHORT).show()
    }

    private fun confirmarEliminacionDefinitiva(nota: Nota) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar definitivamente")
            .setMessage("Esta acción no se puede deshacer. ¿Deseas eliminar \"${nota.titulo}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                notaViewModel.eliminar(nota)
                Toast.makeText(this, "Nota eliminada definitivamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}