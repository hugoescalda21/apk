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

        try {
            binding = ActivityComparativeReportsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener { finish() }

            reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
            publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

            setupRecyclerView()
            setupMonthNavigation()
            setupInitialData()
            loadData()
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                findViewById(android.R.id.content),
                "Error al cargar la pantalla: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
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

    private fun setupInitialData() {
        // Set default values to prevent crashes
        binding.textHoursComparison.text = "0 hrs"
        binding.textHoursTrend.text = "—"
        binding.textReportsComparison.text = "0"
        binding.textReportsTrend.text = "—"
        binding.textAverageHours.text = "0 hrs"
        binding.textAnnualSummary.text = "Total del año: 0 horas"
        binding.textMonthlyAverage.text = "Promedio mensual: 0 horas"
        binding.textNoIrregular.visibility = View.VISIBLE
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
        try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, currentMonth - 1)
            calendar.set(Calendar.YEAR, currentYear)

            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            binding.textCurrentMonth.text = monthFormat.format(calendar.time)
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            binding.textCurrentMonth.text = "Mes $currentMonth - $currentYear"
        }
    }

    private fun loadData() {
        try {
            // Calculate previous month
            var prevMonth = currentMonth - 1
            var prevYear = currentYear
            if (prevMonth < 1) {
                prevMonth = 12
                prevYear--
            }

            // Load hours for current month
            reportViewModel.getTotalHoursForMonth(currentMonth, currentYear).observe(this) { currentHours ->
                val hours = currentHours ?: 0
                binding.textHoursComparison.text = "$hours hrs"

                // Load hours for previous month to compare
                reportViewModel.getTotalHoursForMonth(prevMonth, prevYear).observe(this) { prevHours ->
                    val previous = prevHours ?: 0
                    updateHoursTrend(hours, previous)
                }
            }

            // Load reports count for current month
            reportViewModel.getReportsCountForMonth(currentMonth, currentYear).observe(this) { currentCount ->
                val count = currentCount ?: 0
                binding.textReportsComparison.text = count.toString()

                // Load reports count for previous month to compare
                reportViewModel.getReportsCountForMonth(prevMonth, prevYear).observe(this) { prevCount ->
                    val previous = prevCount ?: 0
                    updateReportsTrend(count, previous)
                }
            }

            // Calculate average hours per publisher
            var publisherCount = 1
            var totalHours = 0

            publisherViewModel.activePublishersCount.observe(this) { count ->
                publisherCount = count ?: 1
                val avgHours = if (publisherCount > 0 && totalHours > 0) {
                    String.format("%.1f hrs", totalHours.toFloat() / publisherCount)
                } else {
                    "0 hrs"
                }
                binding.textAverageHours.text = avgHours
            }

            reportViewModel.getTotalHoursForMonth(currentMonth, currentYear).observe(this) { hours ->
                totalHours = hours ?: 0
                val avgHours = if (publisherCount > 0 && totalHours > 0) {
                    String.format("%.1f hrs", totalHours.toFloat() / publisherCount)
                } else {
                    "0 hrs"
                }
                binding.textAverageHours.text = avgHours
            }

            // Show no irregular publishers message
            irregularPublishersAdapter.submitList(emptyList())
            binding.textNoIrregular.visibility = View.VISIBLE

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateHoursTrend(current: Int, previous: Int) {
        if (previous > 0) {
            val percentChange = ((current - previous).toFloat() / previous * 100).toInt()
            when {
                percentChange > 0 -> {
                    binding.textHoursTrend.setTextColor(getColor(R.color.success))
                    binding.textHoursTrend.text = "↑ $percentChange%"
                }
                percentChange < 0 -> {
                    binding.textHoursTrend.setTextColor(getColor(R.color.error))
                    binding.textHoursTrend.text = "↓ ${-percentChange}%"
                }
                else -> {
                    binding.textHoursTrend.setTextColor(getColor(R.color.text_secondary))
                    binding.textHoursTrend.text = "— 0%"
                }
            }
        } else {
            binding.textHoursTrend.text = "—"
            binding.textHoursTrend.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun updateReportsTrend(current: Int, previous: Int) {
        if (previous > 0) {
            val percentChange = ((current - previous).toFloat() / previous * 100).toInt()
            when {
                percentChange > 0 -> {
                    binding.textReportsTrend.setTextColor(getColor(R.color.success))
                    binding.textReportsTrend.text = "↑ $percentChange%"
                }
                percentChange < 0 -> {
                    binding.textReportsTrend.setTextColor(getColor(R.color.error))
                    binding.textReportsTrend.text = "↓ ${-percentChange}%"
                }
                else -> {
                    binding.textReportsTrend.setTextColor(getColor(R.color.text_secondary))
                    binding.textReportsTrend.text = "— 0%"
                }
            }
        } else {
            binding.textReportsTrend.text = "—"
            binding.textReportsTrend.setTextColor(getColor(R.color.text_secondary))
        }
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

        Snackbar.make(binding.root, "Generando PDF...", Snackbar.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    val pdfGenerator = PDFGenerator(this@ComparativeReportsActivity)

                    val database = com.congregation.reports.data.AppDatabase.getDatabase(this@ComparativeReportsActivity)
                    val publishers = database.publisherDao().getActivePublishersSync()
                    val reports = database.reportDao().getReportsForMonthSync(currentMonth, currentYear)

                    val reportsMap = reports.associateBy { it.publisherId }
                    val reportsData = publishers.map { publisher ->
                        Pair(publisher, reportsMap[publisher.id])
                    }

                    val totalHours = reports.sumOf { it.hours }
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
                        "✓ PDF generado en Descargas",
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
                e.printStackTrace()
                Snackbar.make(
                    binding.root,
                    "Error: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun generateAndSharePDF() {
        Snackbar.make(binding.root, "Generando PDF...", Snackbar.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    val pdfGenerator = PDFGenerator(this@ComparativeReportsActivity)

                    val database = com.congregation.reports.data.AppDatabase.getDatabase(this@ComparativeReportsActivity)
                    val publishers = database.publisherDao().getActivePublishersSync()
                    val reports = database.reportDao().getReportsForMonthSync(currentMonth, currentYear)

                    val reportsMap = reports.associateBy { it.publisherId }
                    val reportsData = publishers.map { publisher ->
                        Pair(publisher, reportsMap[publisher.id])
                    }

                    val totalHours = reports.sumOf { it.hours }
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
                e.printStackTrace()
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
