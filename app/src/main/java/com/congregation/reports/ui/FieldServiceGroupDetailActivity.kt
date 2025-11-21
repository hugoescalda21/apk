package com.congregation.reports.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.data.FieldServiceGroup
import com.congregation.reports.data.Publisher
import com.congregation.reports.databinding.ActivityFieldServiceGroupDetailBinding
import com.congregation.reports.viewmodel.FieldServiceGroupViewModel
import com.congregation.reports.viewmodel.PublisherViewModel

class FieldServiceGroupDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFieldServiceGroupDetailBinding
    private lateinit var groupViewModel: FieldServiceGroupViewModel
    private lateinit var publisherViewModel: PublisherViewModel

    private var groupId: Long = 0
    private var currentGroup: FieldServiceGroup? = null
    private var publishers: List<Publisher> = emptyList()
    private var selectedOverseerId: Long? = null
    private var selectedAssistantId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFieldServiceGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupViewModel = ViewModelProvider(this)[FieldServiceGroupViewModel::class.java]
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        groupId = intent.getLongExtra("GROUP_ID", 0)

        if (groupId > 0) {
            binding.textTitle.text = "Editar Grupo"
            loadGroup()
        } else {
            binding.textTitle.text = "Nuevo Grupo"
        }

        setupPublisherSpinners()
        setupButtons()
    }

    private fun loadGroup() {
        groupViewModel.getGroupById(groupId).observe(this) { group ->
            group?.let {
                currentGroup = it
                binding.editTextGroupName.setText(it.name)
                selectedOverseerId = it.overseerPublisherId
                selectedAssistantId = it.assistantPublisherId
                updateSpinnerSelections()
            }
        }
    }

    private fun setupPublisherSpinners() {
        publisherViewModel.allActivePublishers.observe(this) { publisherList ->
            publisherList?.let {
                publishers = it
                val publisherNames = mutableListOf("No asignado")
                publisherNames.addAll(it.map { pub -> pub.name })

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    publisherNames
                )

                binding.spinnerOverseer.setAdapter(adapter)
                binding.spinnerAssistant.setAdapter(adapter)

                binding.spinnerOverseer.setOnItemClickListener { _, _, position, _ ->
                    selectedOverseerId = if (position == 0) null else publishers[position - 1].id
                }

                binding.spinnerAssistant.setOnItemClickListener { _, _, position, _ ->
                    selectedAssistantId = if (position == 0) null else publishers[position - 1].id
                }

                updateSpinnerSelections()
            }
        }
    }

    private fun updateSpinnerSelections() {
        if (publishers.isEmpty()) return

        val overseer = publishers.find { it.id == selectedOverseerId }
        binding.spinnerOverseer.setText(overseer?.name ?: "No asignado", false)

        val assistant = publishers.find { it.id == selectedAssistantId }
        binding.spinnerAssistant.setText(assistant?.name ?: "No asignado", false)
    }

    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            saveGroup()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveGroup() {
        val name = binding.editTextGroupName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show()
            return
        }

        val group = FieldServiceGroup(
            id = groupId,
            name = name,
            overseerPublisherId = selectedOverseerId,
            assistantPublisherId = selectedAssistantId,
            isActive = true
        )

        if (groupId > 0) {
            groupViewModel.update(group) {
                Toast.makeText(this, "Grupo actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            groupViewModel.insert(group) {
                Toast.makeText(this, "Grupo creado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
