package com.congregation.reports.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.congregation.reports.databinding.ActivitySecurityBinding
import java.util.concurrent.Executor

class SecurityActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecurityBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if security is enabled and user is not authenticated
        if (isSecurityEnabled() && !isAuthenticated()) {
            binding = ActivitySecurityBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupBiometric()
            setupPinAuth()
        } else {
            // User is authenticated or security is disabled, go to MainActivity
            startMainActivity()
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@SecurityActivity,
                        "Error de autenticación: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    markAsAuthenticated()
                    startMainActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@SecurityActivity,
                        "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Usa tu huella dactilar para acceder")
            .setNegativeButtonText("Usar PIN")
            .build()

        binding.buttonBiometric.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    biometricPrompt.authenticate(promptInfo)
                else ->
                    Toast.makeText(this, "Biometría no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPinAuth() {
        binding.buttonUnlock.setOnClickListener {
            val pin = binding.editTextPin.text.toString()
            val savedPin = getSavedPin()

            if (pin == savedPin) {
                markAsAuthenticated()
                startMainActivity()
            } else {
                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isSecurityEnabled(): Boolean {
        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("security_enabled", false)
    }

    private fun isAuthenticated(): Boolean {
        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_authenticated", false)
    }

    private fun markAsAuthenticated() {
        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_authenticated", true).apply()
    }

    private fun getSavedPin(): String {
        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        return prefs.getString("pin", "1234") ?: "1234"
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
