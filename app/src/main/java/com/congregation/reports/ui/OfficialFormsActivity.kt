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
import com.congregation.reports.utils.PdfFormsManager
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
        checkPdfsStatus()
    }

    private fun checkPdfsStatus() {
        if (!PdfFormsManager.areAllPdfsLoaded(this)) {
            AlertDialog.Builder(this)
                .setTitle("📄 PDFs No Cargados")
                .setMessage("Para generar formularios oficiales, primero debes cargar los PDFs en:\n\nConfiguración → Seguridad → Formularios PDF Oficiales\n\n¿Deseas ir ahora?")
                .setPositiveButton("Ir a Configuración") { _, _ ->
                    val intent = Intent(this, SecuritySettingsActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
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
            if (PdfFormsManager.isS21PdfLoaded(this)) {
                generateS21Form()
            } else {
                showPdfNotLoadedDialog("S-21")
            }
        }

        // S-88: Registro de Asistencia
        binding.cardS88.setOnClickListener {
            if (PdfFormsManager.isS88PdfLoaded(this)) {
                generateS88Form()
            } else {
                showPdfNotLoadedDialog("S-88")
            }
        }

        // S-1: Informe de Predicación
        binding.cardS1.setOnClickListener {
            if (PdfFormsManager.isS1PdfLoaded(this)) {
                generateS1Form()
            } else {
                showPdfNotLoadedDialog("S-1")
            }
        }
    }

    private fun showPdfNotLoadedDialog(formName: String) {
        AlertDialog.Builder(this)
            .setTitle("PDF No Cargado")
            .setMessage("El formulario $formName no ha sido cargado.\n\nVe a Configuración → Seguridad → Formularios PDF para cargarlo.")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                val intent = Intent(this, SecuritySettingsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generateS21Form() {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-21...", Toast.LENGTH_SHORT).show()

            // TODO: Implementar llenado de PDF S-21 con datos reales
            // Por ahora solo comparte el PDF original
            val pdfFile = PdfFormsManager.getS21PdfFile(this@OfficialFormsActivity)
            if (pdfFile != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-21 listo (próximamente se rellenará automáticamente)",
                    Toast.LENGTH_LONG
                ).show()
                shareFile(pdfFile, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error: PDF no encontrado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateS88Form() {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-88...", Toast.LENGTH_SHORT).show()

            // TODO: Implementar llenado de PDF S-88 con datos reales
            // Por ahora solo comparte el PDF original
            val pdfFile = PdfFormsManager.getS88PdfFile(this@OfficialFormsActivity)
            if (pdfFile != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-88 listo (próximamente se rellenará automáticamente)",
                    Toast.LENGTH_LONG
                ).show()
                shareFile(pdfFile, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error: PDF no encontrado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateS1Form() {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-1...", Toast.LENGTH_SHORT).show()

            // TODO: Implementar llenado de PDF S-1 con datos reales
            // Por ahora solo comparte el PDF original
            val pdfFile = PdfFormsManager.getS1PdfFile(this@OfficialFormsActivity)
            if (pdfFile != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-1 listo (próximamente se rellenará automáticamente)",
                    Toast.LENGTH_LONG
                ).show()
                shareFile(pdfFile, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error: PDF no encontrado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareFile(file: File, mimeType: String) {
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
