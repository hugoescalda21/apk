package com.congregation.reports.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.congregation.reports.R
import com.congregation.reports.databinding.ActivitySecuritySettingsBinding
import com.congregation.reports.utils.NotificationScheduler
import java.io.File
import java.io.FileOutputStream

class SecuritySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecuritySettingsBinding

    // Activity Result Launchers para selección de PDFs
    private val pickS1Pdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                savePdfToInternalStorage(uri, "S-1.pdf")
                updatePdfStatus()
                Toast.makeText(this, "S-1 cargado exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickS21Pdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                savePdfToInternalStorage(uri, "S-21.pdf")
                updatePdfStatus()
                Toast.makeText(this, "S-21 cargado exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickS88Pdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                savePdfToInternalStorage(uri, "S-88.pdf")
                updatePdfStatus()
                Toast.makeText(this, "S-88 cargado exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecuritySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Seguridad"

        loadSecuritySettings()
        setupListeners()
        updatePdfStatus()
    }

    private fun loadSecuritySettings() {
        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("security_enabled", false)

        binding.switchSecurity.isChecked = isEnabled
        binding.cardPinSettings.isVisible = isEnabled

        // Load notification settings
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.switchNotifications.isChecked = notificationsEnabled
        binding.layoutNotificationOptions.visibility = if (notificationsEnabled) View.VISIBLE else View.GONE

        binding.checkMonthlyReminder.isChecked = prefs.getBoolean("notify_monthly_reminder", true)
        binding.checkPendingReports.isChecked = prefs.getBoolean("notify_pending_reports", true)
        binding.checkInactivePublishers.isChecked = prefs.getBoolean("notify_inactive_publishers", true)
    }

    private fun setupListeners() {
        binding.switchSecurity.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("security_enabled", isChecked).apply()

            binding.cardPinSettings.isVisible = isChecked

            if (isChecked) {
                Toast.makeText(this,
                    "Seguridad activada. PIN predeterminado: 1234",
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Seguridad desactivada", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonChangePin.setOnClickListener {
            changePin()
        }

        // Notification settings listeners
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            binding.layoutNotificationOptions.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (isChecked) {
                NotificationScheduler.scheduleAllNotifications(this)
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
            } else {
                NotificationScheduler.cancelAllNotifications(this)
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
        }

        binding.checkMonthlyReminder.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notify_monthly_reminder", isChecked).apply()
            if (binding.switchNotifications.isChecked) {
                NotificationScheduler.scheduleAllNotifications(this)
            }
        }

        binding.checkPendingReports.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notify_pending_reports", isChecked).apply()
            if (binding.switchNotifications.isChecked) {
                NotificationScheduler.scheduleAllNotifications(this)
            }
        }

        binding.checkInactivePublishers.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notify_inactive_publishers", isChecked).apply()
            if (binding.switchNotifications.isChecked) {
                NotificationScheduler.scheduleAllNotifications(this)
            }
        }

        // PDF Upload Listeners
        binding.buttonUploadS1.setOnClickListener {
            openPdfPicker(pickS1Pdf)
        }

        binding.buttonUploadS21.setOnClickListener {
            openPdfPicker(pickS21Pdf)
        }

        binding.buttonUploadS88.setOnClickListener {
            openPdfPicker(pickS88Pdf)
        }
    }

    private fun openPdfPicker(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        launcher.launch(intent)
    }

    private fun savePdfToInternalStorage(uri: Uri, fileName: String) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val outputFile = File(filesDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Guardar ruta en SharedPreferences
            val prefs = getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("${fileName.replace(".pdf", "")}_path", outputFile.absolutePath).apply()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePdfStatus() {
        val prefs = getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)

        // S-1
        val s1Path = prefs.getString("S-1_path", null)
        if (s1Path != null && File(s1Path).exists()) {
            binding.textS1Status.text = "✓ Cargado"
            binding.textS1Status.setTextColor(ContextCompat.getColor(this, R.color.success))
            binding.buttonUploadS1.text = "Cambiar"
        } else {
            binding.textS1Status.text = "No cargado"
            binding.textS1Status.setTextColor(ContextCompat.getColor(this, R.color.warning))
            binding.buttonUploadS1.text = "Cargar"
        }

        // S-21
        val s21Path = prefs.getString("S-21_path", null)
        if (s21Path != null && File(s21Path).exists()) {
            binding.textS21Status.text = "✓ Cargado"
            binding.textS21Status.setTextColor(ContextCompat.getColor(this, R.color.success))
            binding.buttonUploadS21.text = "Cambiar"
        } else {
            binding.textS21Status.text = "No cargado"
            binding.textS21Status.setTextColor(ContextCompat.getColor(this, R.color.warning))
            binding.buttonUploadS21.text = "Cargar"
        }

        // S-88
        val s88Path = prefs.getString("S-88_path", null)
        if (s88Path != null && File(s88Path).exists()) {
            binding.textS88Status.text = "✓ Cargado"
            binding.textS88Status.setTextColor(ContextCompat.getColor(this, R.color.success))
            binding.buttonUploadS88.text = "Cambiar"
        } else {
            binding.textS88Status.text = "No cargado"
            binding.textS88Status.setTextColor(ContextCompat.getColor(this, R.color.warning))
            binding.buttonUploadS88.text = "Cargar"
        }
    }

    private fun changePin() {
        val currentPin = binding.editTextCurrentPin.text.toString()
        val newPin = binding.editTextNewPin.text.toString()
        val confirmPin = binding.editTextConfirmPin.text.toString()

        // Validaciones
        if (currentPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("pin", "1234") ?: "1234"

        if (currentPin != savedPin) {
            Toast.makeText(this, "PIN actual incorrecto", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPin.length < 4) {
            Toast.makeText(this, "El PIN debe tener al menos 4 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPin != confirmPin) {
            Toast.makeText(this, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar nuevo PIN
        prefs.edit().putString("pin", newPin).apply()

        Toast.makeText(this, "PIN cambiado exitosamente", Toast.LENGTH_SHORT).show()

        // Limpiar campos
        binding.editTextCurrentPin.text?.clear()
        binding.editTextNewPin.text?.clear()
        binding.editTextConfirmPin.text?.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
