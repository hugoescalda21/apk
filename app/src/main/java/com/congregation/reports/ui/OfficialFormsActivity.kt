package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.congregation.reports.data.Publisher
import com.congregation.reports.databinding.ActivityOfficialFormsBinding
import com.congregation.reports.utils.OfficialFormsGenerator
import com.congregation.reports.viewmodel.PublisherViewModel
import kotlinx.coroutines.launch

class OfficialFormsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOfficialFormsBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var formsGenerator: OfficialFormsGenerator

    private var allPublishers: List<Publisher> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficialFormsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Formularios Oficiales"

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        formsGenerator = OfficialFormsGenerator(this)

        loadPublishers()
        setupButtons()
    }

    private fun loadPublishers() {
        publisherViewModel.allPublishers.observe(this) { publishers ->
            publishers?.let {
                allPublishers = it
            }
        }
    }

    private fun setupButtons() {
        // S-21: Registro de Publicadores
        binding.cardS21.setOnClickListener {
            generateS21Form()
        }

        // S-88: Solicitud de Publicador No Bautizado
        binding.cardS88.setOnClickListener {
            showPublisherSelectorDialog { publisher ->
                generateS88Form(publisher)
            }
        }

        // S-1: Solicitud de Precursor
        binding.cardS1.setOnClickListener {
            showPioneerTypeDialog()
        }
    }

    private fun generateS21Form() {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-21...", Toast.LENGTH_SHORT).show()

            val file = formsGenerator.generateS21Form(
                allPublishers,
                "Congregación Central" // Puedes hacerlo configurable
            )

            if (file != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-21 generado exitosamente",
                    Toast.LENGTH_LONG
                ).show()

                shareFile(file, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error al generar S-21",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateS88Form(publisher: Publisher) {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-88...", Toast.LENGTH_SHORT).show()

            val file = formsGenerator.generateS88Form(
                publisher,
                "Congregación Central"
            )

            if (file != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-88 generado exitosamente",
                    Toast.LENGTH_LONG
                ).show()

                shareFile(file, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error al generar S-88",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateS1Form(publisher: Publisher, pioneerType: String) {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-1...", Toast.LENGTH_SHORT).show()

            val file = formsGenerator.generateS1Form(
                publisher,
                "Congregación Central",
                pioneerType
            )

            if (file != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-1 generado exitosamente",
                    Toast.LENGTH_LONG
                ).show()

                shareFile(file, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error al generar S-1",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showPublisherSelectorDialog(onSelected: (Publisher) -> Unit) {
        if (allPublishers.isEmpty()) {
            Toast.makeText(this, "No hay publicadores registrados", Toast.LENGTH_SHORT).show()
            return
        }

        val publisherNames = allPublishers.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Publicador")
            .setItems(publisherNames) { _, which ->
                onSelected(allPublishers[which])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPioneerTypeDialog() {
        val types = arrayOf(
            "Precursor Regular",
            "Precursor Auxiliar"
        )

        AlertDialog.Builder(this)
            .setTitle("Tipo de Precursor")
            .setItems(types) { _, which ->
                val pioneerType = types[which]
                showPublisherSelectorDialog { publisher ->
                    generateS1Form(publisher, pioneerType)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun shareFile(file: java.io.File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Compartir Formulario"))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
