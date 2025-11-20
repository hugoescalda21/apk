package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.databinding.ActivityReportListBinding
import com.congregation.reports.ui.adapters.ReportAdapter
import com.congregation.reports.viewmodel.ReportViewModel
import com.congregation.reports.viewmodel.PublisherViewModel
import java.util.Calendar

class ReportListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportListBinding
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var adapter: ReportAdapter

    private var currentMonth: Int = 0
    private var currentYear: Int = 0

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
        loadReports()
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
                adapter.submitList(it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
