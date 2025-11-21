package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.congregation.reports.data.*
import com.congregation.reports.databinding.ActivityOfficialFormsBinding
import com.congregation.reports.utils.OfficialFormsGenerator
import com.congregation.reports.utils.PdfFieldFiller
import com.congregation.reports.utils.PdfFormsManager
import com.congregation.reports.viewmodel.AttendanceViewModel
import com.congregation.reports.viewmodel.MeetingViewModel
import com.congregation.reports.viewmodel.PublisherViewModel
import com.congregation.reports.viewmodel.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class OfficialFormsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOfficialFormsBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var reportViewModel: ReportViewModel
    private lateinit var meetingViewModel: MeetingViewModel
    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var formsGenerator: OfficialFormsGenerator

    private var allPublishers: List<Publisher> = emptyList()
    private var allReports: List<Report> = emptyList()
    private var allMeetings: List<Meeting> = emptyList()
    private var allAttendances: List<Attendance> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficialFormsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Formularios Oficiales"

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        meetingViewModel = ViewModelProvider(this)[MeetingViewModel::class.java]
        attendanceViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]
        formsGenerator = OfficialFormsGenerator(this)

        loadData()
        setupButtons()
        checkPdfsStatus()
    }

    private fun checkPdfsStatus() {
        if (!PdfFormsManager.areAllPdfsLoaded(this)) {
            AlertDialog.Builder(this)
                .setTitle("📄 PDFs No Cargados")
                .setMessage("Para generar formularios oficiales, primero debes cargar los PDFs en:\n\nConfiguración → Seguridad → Formularios PDF Oficiales\n\n¿Deseas ir ahora?")
                .setPositiveButton("Ir a Configuración") { _, _ ->
                    val intent = Intent(this, SecuritySettingsActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun loadData() {
        publisherViewModel.allPublishers.observe(this) { publishers ->
            publishers?.let {
                allPublishers = it
            }
        }

        reportViewModel.allReports.observe(this) { reports ->
            reports?.let {
                allReports = it
            }
        }

        meetingViewModel.allMeetings.observe(this) { meetings ->
            meetings?.let {
                allMeetings = it
            }
        }

        attendanceViewModel.allAttendances.observe(this) { attendances ->
            attendances?.let {
                allAttendances = it
            }
        }
    }

    private fun setupButtons() {
        // S-21: Registro de Publicadores
        binding.cardS21.setOnClickListener {
            if (PdfFormsManager.isS21PdfLoaded(this)) {
                generateS21Form()
            } else {
                showPdfNotLoadedDialog("S-21")
            }
        }

        // S-88: Registro de Asistencia
        binding.cardS88.setOnClickListener {
            if (PdfFormsManager.isS88PdfLoaded(this)) {
                generateS88Form()
            } else {
                showPdfNotLoadedDialog("S-88")
            }
        }

        // S-1: Informe de Predicación
        binding.cardS1.setOnClickListener {
            if (PdfFormsManager.isS1PdfLoaded(this)) {
                generateS1Form()
            } else {
                showPdfNotLoadedDialog("S-1")
            }
        }
    }

    private fun showPdfNotLoadedDialog(formName: String) {
        AlertDialog.Builder(this)
            .setTitle("PDF No Cargado")
            .setMessage("El formulario $formName no ha sido cargado.\n\nVe a Configuración → Seguridad → Formularios PDF para cargarlo.")
            .setPositiveButton("Ir a Configuración") { _, _ ->
                val intent = Intent(this, SecuritySettingsActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generateS21Form() {
        if (allPublishers.isEmpty()) {
            Toast.makeText(this, "No hay publicadores registrados", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar diálogo para seleccionar publicador
        val publisherNames = allPublishers.map { it.name }.toTypedArray()
        var selectedPublisherIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Publicador")
            .setSingleChoiceItems(publisherNames, 0) { _, which ->
                selectedPublisherIndex = which
            }
            .setPositiveButton("Continuar") { _, _ ->
                selectYearForS21(allPublishers[selectedPublisherIndex])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun selectYearForS21(publisher: Publisher) {
        // Obtener años disponibles (últimos 5 años)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear downTo currentYear - 4).map { it.toString() }.toTypedArray()
        var selectedYearIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Año de Servicio")
            .setSingleChoiceItems(years, 0) { _, which ->
                selectedYearIndex = which
            }
            .setPositiveButton("Generar") { _, _ ->
                val year = years[selectedYearIndex].toInt()
                fillAndShareS21(publisher, year)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun fillAndShareS21(publisher: Publisher, serviceYear: Int) {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-21...", Toast.LENGTH_SHORT).show()

            val filledPdf = withContext(Dispatchers.IO) {
                // Obtener informes del publicador para el año de servicio (Sept a Agosto)
                val publisherReports = allReports.filter { report ->
                    report.publisherId == publisher.id &&
                    ((report.year == serviceYear && report.month >= 9) ||
                     (report.year == serviceYear + 1 && report.month <= 8))
                }.sortedWith(compareBy({ it.year }, { it.month }))

                PdfFieldFiller.fillS21Form(
                    this@OfficialFormsActivity,
                    publisher,
                    publisherReports,
                    serviceYear
                )
            }

            if (filledPdf != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-21 generado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                shareFile(filledPdf, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error al generar el formulario. Verifica que el PDF tenga campos rellenables.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun generateS88Form() {
        // Mostrar diálogo para seleccionar año
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear downTo currentYear - 4).map { it.toString() }.toTypedArray()
        var selectedYearIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Año de Servicio")
            .setMessage("Se generará el registro de asistencia para todo el año de servicio (Sept - Agosto)")
            .setSingleChoiceItems(years, 0) { _, which ->
                selectedYearIndex = which
            }
            .setPositiveButton("Generar") { _, _ ->
                val year = years[selectedYearIndex].toInt()
                fillAndShareS88(year)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun fillAndShareS88(serviceYear: Int) {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-88...", Toast.LENGTH_SHORT).show()

            val filledPdf = withContext(Dispatchers.IO) {
                // Filtrar reuniones del año de servicio
                val calendar = Calendar.getInstance()
                val startDate = calendar.apply {
                    set(Calendar.YEAR, serviceYear)
                    set(Calendar.MONTH, Calendar.SEPTEMBER)
                    set(Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis

                val endDate = calendar.apply {
                    set(Calendar.YEAR, serviceYear + 1)
                    set(Calendar.MONTH, Calendar.AUGUST)
                    set(Calendar.DAY_OF_MONTH, 31)
                }.timeInMillis

                val yearMeetings = allMeetings.filter {
                    it.date in startDate..endDate
                }

                // Obtener IDs de reuniones del año
                val yearMeetingIds = yearMeetings.map { it.id }

                // Filtrar asistencias para las reuniones del año
                val yearAttendances = allAttendances.filter {
                    it.meetingId in yearMeetingIds
                }

                PdfFieldFiller.fillS88Form(
                    this@OfficialFormsActivity,
                    serviceYear,
                    allPublishers.filter { it.isActive },
                    yearMeetings,
                    yearAttendances
                )
            }

            if (filledPdf != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-88 generado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                shareFile(filledPdf, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error al generar el formulario. Verifica que el PDF tenga campos rellenables.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun generateS1Form() {
        // Mostrar diálogo para seleccionar mes y año
        val currentCal = Calendar.getInstance()
        val currentYear = currentCal.get(Calendar.YEAR)
        val currentMonth = currentCal.get(Calendar.MONTH)

        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        var selectedMonth = currentMonth
        var selectedYear = currentYear

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Mes")
            .setSingleChoiceItems(months, currentMonth) { _, which ->
                selectedMonth = which
            }
            .setPositiveButton("Continuar") { _, _ ->
                selectYearForS1(selectedMonth)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun selectYearForS1(month: Int) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear downTo currentYear - 2).map { it.toString() }.toTypedArray()
        var selectedYearIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Seleccionar Año")
            .setSingleChoiceItems(years, 0) { _, which ->
                selectedYearIndex = which
            }
            .setPositiveButton("Generar") { _, _ ->
                val year = years[selectedYearIndex].toInt()
                fillAndShareS1(month + 1, year)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun fillAndShareS1(month: Int, year: Int) {
        lifecycleScope.launch {
            Toast.makeText(this@OfficialFormsActivity, "Generando S-1...", Toast.LENGTH_SHORT).show()

            val filledPdf = withContext(Dispatchers.IO) {
                // Filtrar informes del mes
                val monthReports = allReports.filter {
                    it.month == month && it.year == year
                }

                // Filtrar reuniones del mes
                val calendar = Calendar.getInstance()
                val startDate = calendar.apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }.timeInMillis

                val endDate = calendar.apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }.timeInMillis

                val monthMeetings = allMeetings.filter {
                    it.date in startDate..endDate
                }

                PdfFieldFiller.fillS1Form(
                    this@OfficialFormsActivity,
                    month,
                    year,
                    allPublishers.filter { it.isActive },
                    monthReports,
                    monthMeetings
                )
            }

            if (filledPdf != null) {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "S-1 generado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                shareFile(filledPdf, "application/pdf")
            } else {
                Toast.makeText(
                    this@OfficialFormsActivity,
                    "Error al generar el formulario. Verifica que el PDF tenga campos rellenables.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Compartir Formulario"))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
