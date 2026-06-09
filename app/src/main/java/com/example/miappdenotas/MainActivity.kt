package com.example.miappdenotas

import androidx.core.content.FileProvider
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.example.miappdenotas.NotaDatabase
import com.example.miappdenotas.model.Nota
import com.example.miappdenotas.repository.NotaRepository
import com.example.miappdenotas.viewmodel.NotaViewModel
import com.example.miappdenotas.viewmodel.NotaViewModelFactory
import com.example.miappdenotas.viewmodel.SortOrder // 🛑 IMPORTACIÓN NECESARIA PARA ORDENAR
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.regex.Pattern
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator

class MainActivity : AppCompatActivity(), NotaAdapter.OnItemClickListener {
    private lateinit var emptyView: View

    // ********** CONSTANTES **********
    companion object {
        const val EXTRA_ID = "com.example.miappdenotas.EXTRA_ID"
        const val EXTRA_TITLE = "com.example.miappdenotas.EXTRA_TITLE"
        const val EXTRA_CONTENT = "com.example.miappdenotas.EXTRA_CONTENT"

        private const val EXPORT_REQUEST_CODE = 100
        private const val IMPORT_REQUEST_CODE = 101
        private const val FILE_PROVIDER_AUTHORITY = "com.example.miappdenotas.fileprovider"

        private const val PREFS_NAME = "NotaPrefs"
        private const val KEY_LAST_SAVE_URI = "last_save_uri"
    }

    // ********** PROPIEDADES **********
    private lateinit var notaViewModel: NotaViewModel
    private lateinit var adapter: NotaAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    // ********** ACTIVITY RESULT LAUNCHER (MODERNO) **********
    private val editNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            val title = data?.getStringExtra(EXTRA_TITLE)
            val content = data?.getStringExtra(EXTRA_CONTENT)
            val id = data?.getIntExtra(EXTRA_ID, -1) ?: -1

            if (title == null || content == null) return@registerForActivityResult

            val nota = Nota(titulo = title, contenido = content)

            if (id != -1) {
                // Actualizar nota
                nota.id = id
                notaViewModel.actualizar(nota)
                Toast.makeText(this, "Nota actualizada!", Toast.LENGTH_SHORT).show()
            } else {
                // Guardar nueva nota
                notaViewModel.insertar(nota)
                Toast.makeText(this, "Nota guardada!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Operación cancelada.", Toast.LENGTH_SHORT).show()
        }
    }
    // **********************************************************

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbarAndDrawer()
        initViewModel()
        setupRecyclerView()

        emptyView = findViewById(R.id.empty_view)
        setupFab()
        setupBackPressHandler()
    }


    private fun setupToolbarAndDrawer() {
        val toolbar: Toolbar = findViewById(R.id.mi_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_export -> exportNotes()
                R.id.nav_import -> chooseImportFile()
                R.id.nav_save_general -> quickSaveToLastFile()
                R.id.nav_share_notes -> shareLastExportedFile()
                R.id.nav_share_app -> shareApp()
                R.id.nav_about -> showAbout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun initViewModel() {
        val notaDao = NotaDatabase.obtenerInstancia(application).obtenerNotaDao()
        val repository = NotaRepository(notaDao)
        val factory = NotaViewModelFactory(repository)

        notaViewModel = ViewModelProvider(this, factory)[NotaViewModel::class.java]

        // Observa la LiveData que maneja la lista completa o los resultados de la búsqueda/ordenamiento
        notaViewModel.notasFiltradas.observe(this, Observer { notas ->
            adapter.submitList(notas)
            emptyView.visibility = if (notas.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.apply {
            queryHint = "Buscar notas..."
            setIconifiedByDefault(true)

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    filterNotes(query)
                    searchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterNotes(newText)
                    return true
                }
            })

            setOnCloseListener {
                filterNotes("")
                false
            }
        }
        return true
    }

    // 🛑 MANEJADOR DE CLIC EN EL MENÚ (Para el ordenamiento)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_by_date_desc -> {
                notaViewModel.setSortOrder(SortOrder.DATE_DESC)
                Toast.makeText(this, "Ordenado por: Más Reciente", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.sort_by_date_asc -> {
                notaViewModel.setSortOrder(SortOrder.DATE_ASC)
                Toast.makeText(this, "Ordenado por: Más Antigua", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        // Esto es necesario para manejar la acción de abrir/cerrar el Drawer
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun filterNotes(query: String?) {
        val searchQuery = if (query.isNullOrBlank()) "" else "%$query%"
        notaViewModel.setSearchQuery(searchQuery)
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 250
            removeDuration = 250
            moveDuration = 250
            changeDuration = 250
        }

        adapter = NotaAdapter()
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)

        setupSwipeToDelete(recyclerView)
    }

    private fun setupFab() {
        val buttonAddNote: FloatingActionButton = findViewById(R.id.button_add_note)
        buttonAddNote.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            editNoteLauncher.launch(intent)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // ********** MÉTODOS DEL ADAPTER **********

    override fun onItemClick(nota: Nota) {
        val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java).apply {
            putExtra(EXTRA_ID, nota.id)
            putExtra(EXTRA_TITLE, nota.titulo)
            putExtra(EXTRA_CONTENT, nota.contenido)
        }
        editNoteLauncher.launch(intent)
    }

    override fun onItemLongClick(nota: Nota) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar esta nota: \"${nota.titulo}\"?")
            .setPositiveButton("Sí, Eliminar") { _, _ ->
                setMenuEnabledState(false)

                lifecycleScope.launch {
                    try {
                        notaViewModel.eliminar(nota)
                        Toast.makeText(this@MainActivity, "Nota eliminada.", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error durante la eliminación", e)
                        Toast.makeText(
                            this@MainActivity,
                            "Error al eliminar la nota.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        setMenuEnabledState(true)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onFavoriteClick(nota: Nota) {
        val notaActualizada = nota.copy(favorita = !nota.favorita)
        notaViewModel.actualizar(notaActualizada)

        val mensaje = if (notaActualizada.favorita) {
            "Agregada a favoritos"
        } else {
            "Quitada de favoritos"
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
    private fun setupSwipeToDelete(recyclerView: RecyclerView) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteToDelete = adapter.getNoteAt(position)

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Eliminar nota")
                    .setMessage("¿Deseas eliminar \"${noteToDelete.titulo}\"?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        notaViewModel.eliminar(noteToDelete)
                        Toast.makeText(
                            this@MainActivity,
                            "Nota eliminada: ${noteToDelete.titulo}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    .setOnCancelListener {
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    // ********** LÓGICA DE ARCHIVOS Y OTROS **********

    private fun exportNotes() {
        lifecycleScope.launch {
            val notes = notaViewModel.obtenerTodasLasNotasList()
            val exportContent = notes.joinToString(separator = "\n---\n") {
                "Título: ${it.titulo}\nContenido: ${it.contenido}"
            }

            if (exportContent.isEmpty()) {
                Toast.makeText(this@MainActivity, "No hay notas para exportar.", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }

            // 1. Guardar temporalmente en el caché para uso interno
            val fileNameCache = "notas_exportadas_${System.currentTimeMillis()}.gfa"
            val cacheFile = File(cacheDir, fileNameCache)
            try {
                cacheFile.writeText(exportContent)
                Log.d(
                    "MainActivity",
                    "Archivo de exportación guardado en caché: ${cacheFile.absolutePath}"
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al escribir el archivo de caché", e)
                Toast.makeText(
                    this@MainActivity,
                    "Error al preparar el archivo.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            // 2. Pedir al usuario dónde guardar el archivo de forma externa
            val fileNameExternal = "notas_backup_${System.currentTimeMillis()}.gfa"
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/gfa"
                putExtra(Intent.EXTRA_TITLE, fileNameExternal)
            }
            startActivityForResult(intent, EXPORT_REQUEST_CODE)

            ExportContentHolder.content = exportContent
        }
    }

    private fun shareLastExportedFile() {
        val exportedFiles =
            cacheDir.listFiles { _, name -> name.startsWith("notas_exportadas_") && name.endsWith(".gfa") }
                ?.toList()

        if (exportedFiles.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Primero debes exportar tus notas usando la opción 'Exportar Notas (.gfa)'.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val lastExportedFile = exportedFiles.maxByOrNull { it.lastModified() }

        if (lastExportedFile == null) {
            Toast.makeText(
                this,
                "No se encontró un archivo .gfa reciente para compartir.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val uri: Uri = FileProvider.getUriForFile(
            this,
            FILE_PROVIDER_AUTHORITY,
            lastExportedFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gfa"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup de Notas (${lastExportedFile.name})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Compartir archivo de Notas"))
    }

    private fun chooseImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Mi App de Notas")
            putExtra(
                Intent.EXTRA_TEXT,
                "Mira mi app de notas en GitHub: https://github.com/yerfac98/MiAppDeNotasVF"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir app usando"))
    }

    private fun showAbout() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acerca de la Aplicación")
        builder.setMessage("Aplicación de Notas\n\nDesarrollado por: Gerardo Facundo")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            EXPORT_REQUEST_CODE -> {
                data.data?.let { uri ->
                    val exportContent = ExportContentHolder.content ?: ""

                    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    prefs.edit().putString(KEY_LAST_SAVE_URI, uri.toString()).apply()
                    val flags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, flags)

                    writeExportedContent(uri, exportContent)
                }
            }

            IMPORT_REQUEST_CODE -> {
                data.data?.let { uri ->
                    readAndImportContent(uri)
                }
            }
        }
    }

    private fun writeExportedContent(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                outputStream.write(content.toByteArray())
                Toast.makeText(this, "Notas exportadas a .gfa con éxito.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al escribir el archivo", e)
            Toast.makeText(this, "Error al escribir el archivo: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun readAndImportContent(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val importedText = reader.readText()
                importNotesFromText(importedText)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al leer el archivo", e)
            Toast.makeText(this, "Error al leer el archivo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun importNotesFromText(text: String) {
        val noteEntries = text.split("\n---\n")
        val notesToInsert = mutableListOf<Nota>()

        val titlePattern = Pattern.compile("Título:\\s*(.*)\\s*\\n")
        val contentPattern = Pattern.compile("Contenido:\\s*(.*)", Pattern.DOTALL)

        for (entry in noteEntries) {
            val titleMatcher = titlePattern.matcher(entry)
            val contentMatcher = contentPattern.matcher(entry)

            var title = ""
            var content = ""

            if (titleMatcher.find()) {
                title = titleMatcher.group(1)?.trim() ?: ""
            }

            if (contentMatcher.find()) {
                content = contentMatcher.group(1)?.trim() ?: ""
            }

            if (title.isNotEmpty() && content.isNotEmpty()) {
                notesToInsert.add(Nota(titulo = title, contenido = content))
            }
        }

        if (notesToInsert.isNotEmpty()) {
            notaViewModel.reemplazarTodasLasNotas(notesToInsert)
            Toast.makeText(
                this@MainActivity,
                "Se importaron ${notesToInsert.size} notas, reemplazando las anteriores.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "El archivo no contiene notas válidas para importar.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun quickSaveToLastFile() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastUriString = prefs.getString(KEY_LAST_SAVE_URI, null)

        if (lastUriString == null) {
            Toast.makeText(
                this,
                "No se ha guardado una ubicación. Por favor, use 'Guardar Notas Generales' primero.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val lastUri = Uri.parse(lastUriString)

        lifecycleScope.launch {
            val notes = notaViewModel.obtenerTodasLasNotasList()
            val exportContent = notes.joinToString(separator = "\n---\n") {
                "Título: ${it.titulo}\nContenido: ${it.contenido}"
            }

            writeExportedContent(lastUri, exportContent)

            Toast.makeText(
                this@MainActivity,
                "Cambios guardados exitosamente en la ubicación anterior.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setMenuEnabledState(enabled: Boolean) {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_export)?.isEnabled = enabled
        menu.findItem(R.id.nav_save_general)?.isEnabled = enabled
        menu.findItem(R.id.nav_share_notes)?.isEnabled = enabled

        if (!enabled) {
            Toast.makeText(this, "Procesando operación en DB...", Toast.LENGTH_SHORT).show()
        }
    }
}


// Clase auxiliar temporal para la transferencia de contenido
object ExportContentHolder {
    var content: String? = null
}

