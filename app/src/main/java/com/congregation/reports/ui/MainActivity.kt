package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.databinding.ActivityMainBinding
import com.congregation.reports.viewmodel.PublisherViewModel
import com.congregation.reports.viewmodel.ReportViewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var reportViewModel: ReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.cardPublishers.setOnClickListener {
            startActivity(Intent(this, PublisherListActivity::class.java))
        }

        binding.cardReports.setOnClickListener {
            startActivity(Intent(this, ReportListActivity::class.java))
        }

        binding.cardMeetings.setOnClickListener {
            startActivity(Intent(this, MeetingListActivity::class.java))
        }

        binding.cardBackup.setOnClickListener {
            startActivity(Intent(this, BackupActivity::class.java))
        }

        binding.cardStatus.setOnClickListener {
            startActivity(Intent(this, ReportStatusActivity::class.java))
        }

        binding.cardStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }

    private fun observeData() {
        publisherViewModel.activePublishersCount.observe(this) { count ->
            binding.textPublishersCount.text = count?.toString() ?: "0"
        }

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        reportViewModel.getReportsCountForMonth(month, year).observe(this) { count ->
            binding.textReportsCount.text = count?.toString() ?: "0"
        }

        reportViewModel.getTotalHoursForMonth(month, year).observe(this) { hours ->
            binding.textTotalHours.text = hours?.toString() ?: "0"
        }
    }
}
