package com.congregation.reports.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.R
import com.congregation.reports.databinding.ActivityComparativeReportsBinding
import com.congregation.reports.utils.PDFGenerator
import com.congregation.reports.viewmodel.ReportViewModel
import com.congregation.reports.viewmodel.PublisherViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ComparativeReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityComparativeReportsBinding
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var irregularPublishersAdapter: IrregularPublishersAdapter

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComparativeReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        setupRecyclerView()
        setupMonthNavigation()
        loadData()
    }

    private fun setupRecyclerView() {
        irregularPublishersAdapter = IrregularPublishersAdapter()
        binding.recyclerIrregularPublishers.apply {
            layoutManager = LinearLayoutManager(this@ComparativeReportsActivity)
            adapter = irregularPublishersAdapter
        }
    }

    private fun setupMonthNavigation() {
        binding.buttonPreviousMonth.setOnClickListener {
            navigateMonth(-1)
        }

        binding.buttonNextMonth.setOnClickListener {
            navigateMonth(1)
        }

        updateMonthDisplay()
    }

    private fun navigateMonth(offset: Int) {
        currentMonth += offset
        if (currentMonth > 12) {
            currentMonth = 1
            currentYear++
        } else if (currentMonth < 1) {
            currentMonth = 12
            currentYear--
        }

        updateMonthDisplay()
        loadData()
    }

    private fun updateMonthDisplay() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth - 1)
        calendar.set(Calendar.YEAR, currentYear)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        binding.textCurrentMonth.text = monthFormat.format(calendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    private fun loadData() {
        // Calculate previous month
        var prevMonth = currentMonth - 1
        var prevYear = currentYear
        if (prevMonth < 1) {
            prevMonth = 12
            prevYear--
        }

        // Load current month data
        reportViewModel.getTotalHoursForMonth(currentMonth, currentYear).observe(this) { currentHours ->
            val hours = currentHours ?: 0

            // Load previous month data for comparison
            reportViewModel.getTotalHoursForMonth(prevMonth, prevYear).observe(this) { prevHours ->
                val previousHours = prevHours ?: 0
                updateHoursComparison(hours, previousHours)
            }
        }

        reportViewModel.getReportsCountForMonth(currentMonth, currentYear).observe(this) { currentCount ->
            val count = currentCount ?: 0

            reportViewModel.getReportsCountForMonth(prevMonth, prevYear).observe(this) { prevCount ->
                val previousCount = prevCount ?: 0
                updateReportsComparison(count, previousCount)
            }
        }

        // Calculate average hours per publisher
        publisherViewModel.activePublishersCount.observe(this) { publisherCount ->
            reportViewModel.getTotalHoursForMonth(currentMonth, currentYear).observe(this) { hours ->
                val avgHours = if (publisherCount != null && publisherCount > 0 && hours != null) {
                    String.format("%.1f hrs", hours.toFloat() / publisherCount)
                } else {
                    "0 hrs"
                }
                binding.textAverageHours.text = avgHours
            }
        }

        // Load annual data
        loadAnnualData()

        // Load irregular publishers
        loadIrregularPublishers()
    }

    private fun updateHoursComparison(current: Int, previous: Int) {
        binding.textHoursComparison.text = "$current hrs"

        if (previous > 0) {
            val percentChange = ((current - previous).toFloat() / previous * 100).toInt()
            val trend = when {
                percentChange > 0 -> {
                    binding.textHoursTrend.setTextColor(getColor(com.congregation.reports.R.color.success))
                    "↑ $percentChange%"
                }
                percentChange < 0 -> {
                    binding.textHoursTrend.setTextColor(getColor(com.congregation.reports.R.color.error))
                    "↓ ${-percentChange}%"
                }
                else -> {
                    binding.textHoursTrend.setTextColor(getColor(com.congregation.reports.R.color.text_secondary))
                    "— 0%"
                }
            }
            binding.textHoursTrend.text = trend
        } else {
            binding.textHoursTrend.text = "—"
            binding.textHoursTrend.setTextColor(getColor(com.congregation.reports.R.color.text_secondary))
        }
    }

    private fun updateReportsComparison(current: Int, previous: Int) {
        binding.textReportsComparison.text = current.toString()

        if (previous > 0) {
            val percentChange = ((current - previous).toFloat() / previous * 100).toInt()
            val trend = when {
                percentChange > 0 -> {
                    binding.textReportsTrend.setTextColor(getColor(com.congregation.reports.R.color.success))
                    "↑ $percentChange%"
                }
                percentChange < 0 -> {
                    binding.textReportsTrend.setTextColor(getColor(com.congregation.reports.R.color.error))
                    "↓ ${-percentChange}%"
                }
                else -> {
                    binding.textReportsTrend.setTextColor(getColor(com.congregation.reports.R.color.text_secondary))
                    "— 0%"
                }
            }
            binding.textReportsTrend.text = trend
        } else {
            binding.textReportsTrend.text = "—"
            binding.textReportsTrend.setTextColor(getColor(com.congregation.reports.R.color.text_secondary))
        }
    }

    private fun loadAnnualData() {
        // Calculate total hours for the year
        var totalHours = 0
        var monthsProcessed = 0
        val targetMonths = if (currentMonth == 12) 12 else currentMonth

        for (month in 1..targetMonths) {
            reportViewModel.getTotalHoursForMonth(month, currentYear).observe(this) { hours ->
                totalHours += hours ?: 0
                monthsProcessed++

                if (monthsProcessed == targetMonths) {
                    binding.textAnnualSummary.text = "Total del año: $totalHours horas"
                    val avgMonthly = if (targetMonths > 0) totalHours / targetMonths else 0
                    binding.textMonthlyAverage.text = "Promedio mensual: $avgMonthly horas"
                }
            }
        }
    }

    private fun loadIrregularPublishers() {
        // Calculate two months ago
        var twoMonthsAgo = currentMonth - 2
        var yearTwoMonthsAgo = currentYear
        if (twoMonthsAgo < 1) {
            twoMonthsAgo += 12
            yearTwoMonthsAgo--
        }

        var oneMonthAgo = currentMonth - 1
        var yearOneMonthAgo = currentYear
        if (oneMonthAgo < 1) {
            oneMonthAgo = 12
            yearOneMonthAgo--
        }

        // This is a simplified version - in a real implementation,
        // you would query the database for publishers without reports in the last 2 months
        // For now, just show placeholder
        irregularPublishersAdapter.submitList(emptyList())
        binding.textNoIrregular.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.comparative_reports_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_generate_pdf -> {
                generatePDF()
                true
            }
            R.id.action_share_pdf -> {
                generateAndSharePDF()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun generatePDF() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    100
                )
                return
            }
        }

        lifecycleScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    val pdfGenerator = PDFGenerator(this@ComparativeReportsActivity)

                    // Fetch all publishers and their reports
                    val publishers = publisherViewModel.allPublishers.value ?: emptyList()
                    val reportsData = mutableListOf<Pair<com.congregation.reports.data.Publisher, com.congregation.reports.data.Report?>>()

                    var totalHours = 0
                    for (publisher in publishers) {
                        val report = reportViewModel.getReport(publisher.id, currentMonth, currentYear).value
                        reportsData.add(Pair(publisher, report))
                        totalHours += report?.hours ?: 0
                    }

                    val avgHours = if (publishers.isNotEmpty()) totalHours.toFloat() / publishers.size else 0f

                    pdfGenerator.generateMonthlyReport(
                        currentMonth,
                        currentYear,
                        reportsData,
                        totalHours,
                        avgHours
                    )
                }

                if (pdfFile != null) {
                    Snackbar.make(
                        binding.root,
                        "PDF generado exitosamente en Descargas",
                        Snackbar.LENGTH_LONG
                    ).setAction("Abrir") {
                        openPDF(pdfFile)
                    }.show()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Error al generar PDF",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "Error: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun generateAndSharePDF() {
        lifecycleScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    val pdfGenerator = PDFGenerator(this@ComparativeReportsActivity)

                    val publishers = publisherViewModel.allPublishers.value ?: emptyList()
                    val reportsData = mutableListOf<Pair<com.congregation.reports.data.Publisher, com.congregation.reports.data.Report?>>()

                    var totalHours = 0
                    for (publisher in publishers) {
                        val report = reportViewModel.getReport(publisher.id, currentMonth, currentYear).value
                        reportsData.add(Pair(publisher, report))
                        totalHours += report?.hours ?: 0
                    }

                    val avgHours = if (publishers.isNotEmpty()) totalHours.toFloat() / publishers.size else 0f

                    pdfGenerator.generateMonthlyReport(
                        currentMonth,
                        currentYear,
                        reportsData,
                        totalHours,
                        avgHours
                    )
                }

                if (pdfFile != null) {
                    sharePDF(pdfFile)
                } else {
                    Snackbar.make(
                        binding.root,
                        "Error al generar PDF",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "Error: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openPDF(file: java.io.File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "No se encontró una aplicación para abrir PDF",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun sharePDF(file: java.io.File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Informe de la Congregación")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el informe mensual de servicio del campo")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "Error al compartir PDF: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
