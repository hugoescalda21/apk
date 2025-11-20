package com.congregation.reports.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.databinding.ActivityStatisticsBinding
import com.congregation.reports.viewmodel.ReportViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar

class StatisticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var reportViewModel: ReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Estadísticas"

        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        setupCharts()
        loadStatistics()
    }

    private fun setupCharts() {
        // Bar Chart configuration
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }

        // Line Chart configuration
        binding.lineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setPinchZoom(true)
            isDoubleTapToZoomEnabled = true

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }

    private fun loadStatistics() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // Load data for last 12 months
        val months = mutableListOf<String>()
        val hoursData = mutableListOf<BarEntry>()
        val publicationsData = mutableListOf<Entry>()

        for (i in 11 downTo 0) {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - i)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            months.add(getMonthName(month))

            reportViewModel.getTotalHoursForMonth(month, year).observe(this) { hours ->
                if (hours != null) {
                    hoursData.add(BarEntry((11 - i).toFloat(), hours.toFloat()))
                    if (hoursData.size == 12) {
                        updateBarChart(months, hoursData)
                    }
                }
            }

            // Reset calendar for next iteration
            calendar.set(Calendar.YEAR, currentYear)
        }

        updateLineChart(months, publicationsData)
    }

    private fun updateBarChart(labels: List<String>, data: List<BarEntry>) {
        val dataSet = BarDataSet(data, "Horas Totales").apply {
            color = Color.parseColor("#2196F3")
            valueTextSize = 10f
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        binding.barChart.apply {
            this.data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            animateY(1000)
            invalidate()
        }
    }

    private fun updateLineChart(labels: List<String>, data: List<Entry>) {
        if (data.isEmpty()) {
            // Add dummy data for visualization
            for (i in labels.indices) {
                data.add(Entry(i.toFloat(), (Math.random() * 100).toFloat()))
            }
        }

        val dataSet = LineDataSet(data, "Publicaciones").apply {
            color = Color.parseColor("#FF9800")
            lineWidth = 2f
            setCircleColor(Color.parseColor("#FF9800"))
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)

        binding.lineChart.apply {
            this.data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            animateX(1000)
            invalidate()
        }
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        )
        return months[month - 1]
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
