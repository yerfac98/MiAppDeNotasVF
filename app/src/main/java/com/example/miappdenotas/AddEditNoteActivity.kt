package com.example.miappdenotas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private var noteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        // ********** CORRECCIÓN CLAVE: INICIALIZAR VISTAS PRIMERO **********
        editTextTitle = findViewById(R.id.edit_text_title)
        editTextContent = findViewById(R.id.edit_text_content)
        // *******************************************************************

        // 1. ENCUENTRA LA TOOLBAR CUSTOMIZADA
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_edit_note)

        // 2. ESTABLECE LA TOOLBAR COMO LA BARRA DE ACCIÓN OFICIAL DE LA ACTIVIDAD
        setSupportActionBar(toolbar)

        // 3. ACTUALIZA EL TÍTULO DE LA BARRA Y CARGA DATOS
        if (intent.hasExtra(MainActivity.EXTRA_ID)) {
            toolbar.title = "Editar Nota"
            noteId = intent.getIntExtra(MainActivity.EXTRA_ID, -1)
            // Esto ahora accede a las vistas que ya fueron inicializadas.
            editTextTitle.setText(intent.getStringExtra(MainActivity.EXTRA_TITLE))
            editTextContent.setText(intent.getStringExtra(MainActivity.EXTRA_CONTENT))
        } else {
            toolbar.title = "Añadir Nota"
        }

        // 4. HABILITA EL ÍCONO DE RETROCESO (FLECHA)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Crea el menú de la barra de acción (Ícono Guardar)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return true
    }

    // Maneja los clics en los elementos del menú (Guardar) y el botón de retroceso
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_note -> {
                saveNote()
                return true // Sale inmediatamente si guardó
            }
            android.R.id.home -> {
                finish()
                return true // Sale inmediatamente si cerró
            }
        }
        return super.onOptionsItemSelected(item) // Manejo predeterminado
    }

    private fun saveNote() {
        val title = editTextTitle.text.toString().trim()
        val content = editTextContent.text.toString().trim()

        if (title.isEmpty()) {
            editTextTitle.error = "El título no puede estar vacío"
            editTextTitle.requestFocus()
            return
        }

        if (content.isEmpty()) {
            editTextContent.error = "El contenido no puede estar vacío"
            editTextContent.requestFocus()
            return
        }

        val data = Intent().apply {
            putExtra(MainActivity.EXTRA_TITLE, title)
            putExtra(MainActivity.EXTRA_CONTENT, content)

            if (noteId != -1) {
                putExtra(MainActivity.EXTRA_ID, noteId)
            }
        }

        setResult(Activity.RESULT_OK, data)
        finish()
    }
}