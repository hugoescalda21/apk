package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.databinding.ActivityBackupBinding
import com.congregation.reports.ui.adapters.BackupAdapter
import com.congregation.reports.utils.BackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BackupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackupBinding
    private lateinit var backupManager: BackupManager
    private lateinit var adapter: BackupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Respaldo de Datos"

        backupManager = BackupManager(this)

        setupRecyclerView()
        setupButtons()
        loadBackups()
    }

    private fun setupRecyclerView() {
        adapter = BackupAdapter(
            onRestore = { file ->
                showRestoreConfirmation(file)
            },
            onShare = { file ->
                shareBackup(file)
            },
            onDelete = { file ->
                showDeleteConfirmation(file)
            }
        )

        binding.recyclerViewBackups.apply {
            layoutManager = LinearLayoutManager(this@BackupActivity)
            adapter = this@BackupActivity.adapter
        }
    }

    private fun setupButtons() {
        binding.buttonCreateBackup.setOnClickListener {
            createBackup()
        }
    }

    private fun createBackup() {
        lifecycleScope.launch {
            binding.buttonCreateBackup.isEnabled = false
            binding.buttonCreateBackup.text = "Creando respaldo..."

            val backupFile = backupManager.createBackup()

            binding.buttonCreateBackup.isEnabled = true
            binding.buttonCreateBackup.text = "Crear Respaldo"

            if (backupFile != null) {
                Toast.makeText(
                    this@BackupActivity,
                    "Respaldo creado: ${backupFile.name}",
                    Toast.LENGTH_LONG
                ).show()
                loadBackups()
            } else {
                Toast.makeText(
                    this@BackupActivity,
                    "Error al crear respaldo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showRestoreConfirmation(file: java.io.File) {
        AlertDialog.Builder(this)
            .setTitle("Restaurar Respaldo")
            .setMessage("¿Estás seguro de que deseas restaurar este respaldo? Se perderán todos los datos actuales.")
            .setPositiveButton("Restaurar") { _, _ ->
                restoreBackup(file)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restoreBackup(file: java.io.File) {
        lifecycleScope.launch {
            val success = backupManager.restoreBackup(file)

            if (success) {
                AlertDialog.Builder(this@BackupActivity)
                    .setTitle("Restauración Exitosa")
                    .setMessage("La base de datos ha sido restaurada. La aplicación se reiniciará.")
                    .setPositiveButton("OK") { _, _ ->
                        // Restart app
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                Toast.makeText(
                    this@BackupActivity,
                    "Error al restaurar respaldo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun shareBackup(file: java.io.File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Respaldo - Informes Congregación")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Compartir Respaldo"))
    }

    private fun showDeleteConfirmation(file: java.io.File) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Respaldo")
            .setMessage("¿Estás seguro de que deseas eliminar este respaldo?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteBackup(file)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteBackup(file: java.io.File) {
        if (file.delete()) {
            Toast.makeText(this, "Respaldo eliminado", Toast.LENGTH_SHORT).show()
            loadBackups()
        } else {
            Toast.makeText(this, "Error al eliminar respaldo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBackups() {
        val backups = backupManager.getBackupFiles()
        adapter.submitList(backups)

        binding.textBackupCount.text = if (backups.isEmpty()) {
            "No hay respaldos disponibles"
        } else {
            "${backups.size} respaldo(s) disponible(s)"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
