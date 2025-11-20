package com.congregation.reports.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.congregation.reports.databinding.ActivitySecuritySettingsBinding

class SecuritySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecuritySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecuritySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Seguridad"

        loadSecuritySettings()
        setupListeners()
    }

    private fun loadSecuritySettings() {
        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("security_enabled", false)

        binding.switchSecurity.isChecked = isEnabled
        binding.cardPinSettings.isVisible = isEnabled
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
