package com.congregation.reports.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.R
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.PublisherType
import com.congregation.reports.databinding.ActivityPublisherDetailBinding
import com.congregation.reports.viewmodel.PublisherViewModel

class PublisherDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPublisherDetailBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private var publisherId: Long = 0
    private var currentPublisher: Publisher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublisherDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        publisherId = intent.getLongExtra("PUBLISHER_ID", 0)

        setupSpinner()
        setupButtons()

        if (publisherId > 0) {
            supportActionBar?.title = "Editar Publicador"
            loadPublisher()
        } else {
            supportActionBar?.title = "Nuevo Publicador"
        }
    }

    private fun setupSpinner() {
        val types = PublisherType.values().map { it.name.replace("_", " ") }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPublisherType.adapter = adapter
    }

    private fun loadPublisher() {
        publisherViewModel.getPublisherById(publisherId).observe(this) { publisher ->
            publisher?.let {
                currentPublisher = it
                binding.editTextName.setText(it.name)
                binding.editTextPhone.setText(it.phoneNumber)
                binding.editTextEmail.setText(it.email)
                binding.spinnerPublisherType.setSelection(it.type.ordinal)
                binding.checkBoxActive.isChecked = it.isActive
            }
        }
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            savePublisher()
        }

        if (publisherId > 0) {
            binding.buttonDelete.visibility = android.view.View.VISIBLE
            binding.buttonDelete.setOnClickListener {
                deletePublisher()
            }
        }
    }

    private fun savePublisher() {
        val name = binding.editTextName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val phone = binding.editTextPhone.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val type = PublisherType.values()[binding.spinnerPublisherType.selectedItemPosition]
        val isActive = binding.checkBoxActive.isChecked

        val publisher = Publisher(
            id = publisherId,
            name = name,
            phoneNumber = phone,
            email = email,
            type = type,
            isActive = isActive
        )

        if (publisherId > 0) {
            publisherViewModel.update(publisher)
            Toast.makeText(this, "Publicador actualizado", Toast.LENGTH_SHORT).show()
        } else {
            publisherViewModel.insert(publisher)
            Toast.makeText(this, "Publicador creado", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun deletePublisher() {
        currentPublisher?.let {
            publisherViewModel.delete(it)
            Toast.makeText(this, "Publicador eliminado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
