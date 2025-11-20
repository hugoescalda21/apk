package com.congregation.reports.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.databinding.ActivityReportStatusBinding
import com.congregation.reports.ui.adapters.ReportStatusAdapter
import com.congregation.reports.viewmodel.PublisherViewModel
import com.congregation.reports.viewmodel.ReportViewModel
import java.util.Calendar

class ReportStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportStatusBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var adapter: ReportStatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Estado de Informes"

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        setupRecyclerView()
        loadStatus()
    }

    private fun setupRecyclerView() {
        adapter = ReportStatusAdapter()

        binding.recyclerViewStatus.apply {
            layoutManager = LinearLayoutManager(this@ReportStatusActivity)
            adapter = this@ReportStatusActivity.adapter
        }
    }

    private fun loadStatus() {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        binding.textCurrentMonth.text = "${getMonthName(month)} $year"

        publisherViewModel.allActivePublishers.observe(this) { publishers ->
            if (publishers != null) {
                reportViewModel.getReportsByMonth(month, year).observe(this) { reports ->
                    val statusList = publishers.map { publisher ->
                        val hasReport = reports?.any { it.publisherId == publisher.id } ?: false
                        ReportStatusAdapter.PublisherStatus(
                            publisherName = publisher.name,
                            hasSubmitted = hasReport,
                            publisherType = publisher.type.name.replace("_", " ")
                        )
                    }

                    adapter.submitList(statusList)

                    val submitted = statusList.count { it.hasSubmitted }
                    val total = statusList.size
                    val percentage = if (total > 0) (submitted * 100) / total else 0

                    binding.textStats.text = "$submitted de $total informes ($percentage%)"
                }
            }
        }
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return months[month - 1]
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
