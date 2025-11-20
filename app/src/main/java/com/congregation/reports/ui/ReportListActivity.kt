package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.Report
import com.congregation.reports.databinding.ActivityReportListBinding
import com.congregation.reports.ui.adapters.ReportAdapter
import com.congregation.reports.utils.ExcelExporter
import com.congregation.reports.utils.PdfExporter
import com.congregation.reports.viewmodel.ReportViewModel
import com.congregation.reports.viewmodel.PublisherViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportListBinding
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var adapter: ReportAdapter

    private var currentMonth: Int = 0
    private var currentYear: Int = 0
    private var currentReports: List<Report> = emptyList()
    private var currentPublishers: List<Publisher> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Informes"

        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        val calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH) + 1
        currentYear = calendar.get(Calendar.YEAR)

        setupMonthYearSpinners()
        setupRecyclerView()
        setupFab()
        setupExportButtons()
        loadReports()
        loadPublishers()
    }

    private fun setupMonthYearSpinners() {
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(currentMonth - 1)

        val years = (2020..2030).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
        binding.spinnerYear.setSelection(years.indexOf(currentYear.toString()))

        binding.buttonLoadReports.setOnClickListener {
            currentMonth = binding.spinnerMonth.selectedItemPosition + 1
            currentYear = binding.spinnerYear.selectedItem.toString().toInt()
            loadReports()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter(publisherViewModel) { report ->
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("REPORT_ID", report.id)
            intent.putExtra("PUBLISHER_ID", report.publisherId)
            intent.putExtra("MONTH", report.month)
            intent.putExtra("YEAR", report.year)
            startActivity(intent)
        }

        binding.recyclerViewReports.apply {
            layoutManager = LinearLayoutManager(this@ReportListActivity)
            adapter = this@ReportListActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddReport.setOnClickListener {
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("MONTH", currentMonth)
            intent.putExtra("YEAR", currentYear)
            startActivity(intent)
        }
    }

    private fun loadReports() {
        reportViewModel.getReportsByMonth(currentMonth, currentYear).observe(this) { reports ->
            reports?.let {
                currentReports = it
                adapter.submitList(it)
            }
        }
    }

    private fun loadPublishers() {
        publisherViewModel.allPublishers.observe(this) { publishers ->
            publishers?.let {
                currentPublishers = it
            }
        }
    }

    private fun setupExportButtons() {
        binding.buttonExportPdf.setOnClickListener {
            exportToPdf()
        }

        binding.buttonExportExcel.setOnClickListener {
            exportToExcel()
        }
    }

    private fun exportToPdf() {
        lifecycleScope.launch {
            val pdfExporter = PdfExporter(this@ReportListActivity)
            val file = pdfExporter.exportMonthlyReport(
                currentMonth,
                currentYear,
                currentPublishers,
                currentReports
            )

            if (file != null) {
                Toast.makeText(
                    this@ReportListActivity,
                    "PDF guardado en: ${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()

                // Share file
                val uri = FileProvider.getUriForFile(
                    this@ReportListActivity,
                    "${packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
            } else {
                Toast.makeText(
                    this@ReportListActivity,
                    "Error al generar PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun exportToExcel() {
        lifecycleScope.launch {
            val excelExporter = ExcelExporter(this@ReportListActivity)
            val file = excelExporter.exportMonthlyReport(
                currentMonth,
                currentYear,
                currentPublishers,
                currentReports
            )

            if (file != null) {
                Toast.makeText(
                    this@ReportListActivity,
                    "Excel guardado en: ${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()

                // Share file
                val uri = FileProvider.getUriForFile(
                    this@ReportListActivity,
                    "${packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Compartir Excel"))
            } else {
                Toast.makeText(
                    this@ReportListActivity,
                    "Error al generar Excel",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
