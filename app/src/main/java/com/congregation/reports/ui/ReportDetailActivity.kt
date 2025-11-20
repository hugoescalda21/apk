package com.congregation.reports.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.Report
import com.congregation.reports.databinding.ActivityReportDetailBinding
import com.congregation.reports.viewmodel.PublisherViewModel
import com.congregation.reports.viewmodel.ReportViewModel

class ReportDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportDetailBinding
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var publisherViewModel: PublisherViewModel
    private var reportId: Long = 0
    private var publisherId: Long = 0
    private var month: Int = 0
    private var year: Int = 0
    private var currentReport: Report? = null
    private val publishersList = mutableListOf<Publisher>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        reportId = intent.getLongExtra("REPORT_ID", 0)
        publisherId = intent.getLongExtra("PUBLISHER_ID", 0)
        month = intent.getIntExtra("MONTH", 0)
        year = intent.getIntExtra("YEAR", 0)

        setupPublishersSpinner()
        setupButtons()

        if (reportId > 0) {
            supportActionBar?.title = "Editar Informe"
            loadReport()
        } else {
            supportActionBar?.title = "Nuevo Informe"
        }
    }

    private fun setupPublishersSpinner() {
        publisherViewModel.allActivePublishers.observe(this) { publishers ->
            publishers?.let {
                publishersList.clear()
                publishersList.addAll(it)

                val publisherNames = it.map { pub -> pub.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, publisherNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerPublisher.adapter = adapter

                if (publisherId > 0) {
                    val index = publishersList.indexOfFirst { pub -> pub.id == publisherId }
                    if (index >= 0) {
                        binding.spinnerPublisher.setSelection(index)
                    }
                }
            }
        }
    }

    private fun loadReport() {
        reportViewModel.getReport(publisherId, month, year).observe(this) { report ->
            report?.let {
                currentReport = it
                binding.editTextMonth.setText(it.month.toString())
                binding.editTextYear.setText(it.year.toString())
                binding.editTextHours.setText(it.hours.toString())
                binding.editTextPublications.setText(it.publications.toString())
                binding.editTextVideos.setText(it.videos.toString())
                binding.editTextReturnVisits.setText(it.returnVisits.toString())
                binding.editTextBibleStudies.setText(it.bibleStudies.toString())
                binding.editTextComments.setText(it.comments)
            }
        }
    }

    private fun setupButtons() {
        if (month > 0) binding.editTextMonth.setText(month.toString())
        if (year > 0) binding.editTextYear.setText(year.toString())

        binding.buttonSaveReport.setOnClickListener {
            saveReport()
        }

        if (reportId > 0) {
            binding.buttonDeleteReport.visibility = View.VISIBLE
            binding.buttonDeleteReport.setOnClickListener {
                deleteReport()
            }
        }
    }

    private fun saveReport() {
        if (publishersList.isEmpty()) {
            Toast.makeText(this, "No hay publicadores disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPublisher = publishersList[binding.spinnerPublisher.selectedItemPosition]
        val monthValue = binding.editTextMonth.text.toString().toIntOrNull() ?: 0
        val yearValue = binding.editTextYear.text.toString().toIntOrNull() ?: 0

        if (monthValue < 1 || monthValue > 12) {
            Toast.makeText(this, "Mes inválido (1-12)", Toast.LENGTH_SHORT).show()
            return
        }

        val report = Report(
            id = reportId,
            publisherId = selectedPublisher.id,
            month = monthValue,
            year = yearValue,
            hours = binding.editTextHours.text.toString().toIntOrNull() ?: 0,
            publications = binding.editTextPublications.text.toString().toIntOrNull() ?: 0,
            videos = binding.editTextVideos.text.toString().toIntOrNull() ?: 0,
            returnVisits = binding.editTextReturnVisits.text.toString().toIntOrNull() ?: 0,
            bibleStudies = binding.editTextBibleStudies.text.toString().toIntOrNull() ?: 0,
            comments = binding.editTextComments.text.toString()
        )

        if (reportId > 0) {
            reportViewModel.update(report)
            Toast.makeText(this, "Informe actualizado", Toast.LENGTH_SHORT).show()
        } else {
            reportViewModel.insert(report)
            Toast.makeText(this, "Informe creado", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun deleteReport() {
        currentReport?.let {
            reportViewModel.delete(it)
            Toast.makeText(this, "Informe eliminado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
