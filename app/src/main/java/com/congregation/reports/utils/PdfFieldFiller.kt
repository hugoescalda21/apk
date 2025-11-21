package com.congregation.reports.utils

import android.content.Context
import com.congregation.reports.data.*
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.forms.fields.PdfFormField
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para rellenar automáticamente los formularios PDF oficiales
 * con datos de la aplicación
 */
object PdfFieldFiller {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthNames = arrayOf(
        "Septiembre", "Octubre", "Noviembre", "Diciembre",
        "Enero", "Febrero", "Marzo", "Abril", "Mayo",
        "Junio", "Julio", "Agosto"
    )

    /**
     * Rellena el formulario S-21 (Registro de Publicador)
     * @param context Contexto de la aplicación
     * @param publisher Publicador con sus datos personales
     * @param reports Lista de informes del publicador (12 meses de Sept a Agosto)
     * @param serviceYear Año de servicio (ejemplo: 2024 para Sept 2024 - Agosto 2025)
     * @return File del PDF rellenado
     */
    fun fillS21Form(
        context: Context,
        publisher: Publisher,
        reports: List<Report>,
        serviceYear: Int
    ): File? {
        val templateFile = PdfFormsManager.getS21PdfFile(context) ?: return null
        val outputFile = File(context.cacheDir, "S-21_${publisher.name}_$serviceYear.pdf")

        try {
            val pdfReader = PdfReader(templateFile)
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfReader, pdfWriter)
            val form = PdfAcroForm.getAcroForm(pdfDocument, false)

            if (form != null) {
                // Datos personales del publicador
                setFieldValue(form, "nombre", publisher.name)
                setFieldValue(form, "fechaNacimiento", publisher.dateOfBirth)
                setFieldValue(form, "fechaBautismo", publisher.dateOfBaptism)
                setFieldValue(form, "direccion", publisher.address)
                setFieldValue(form, "contactoEmergencia", publisher.emergencyContact)
                setFieldValue(form, "telefono", publisher.phoneNumber)
                setFieldValue(form, "email", publisher.email)

                // Año de servicio
                setFieldValue(form, "anoServicio", "$serviceYear-${serviceYear + 1}")

                // Rellenar informes mensuales (Sept a Agosto)
                reports.forEachIndexed { index, report ->
                    val month = monthNames.getOrNull(index) ?: ""

                    // Los nombres de campos pueden variar según el PDF oficial
                    // Estos son nombres comunes que podrían estar en el PDF
                    setFieldValue(form, "horas_$month", report.hours.toString())
                    setFieldValue(form, "publicaciones_$month", report.publications.toString())
                    setFieldValue(form, "videos_$month", report.videos.toString())
                    setFieldValue(form, "revisitas_$month", report.returnVisits.toString())
                    setFieldValue(form, "estudios_$month", report.bibleStudies.toString())

                    // También intentar con índice numérico
                    setFieldValue(form, "horas_${index + 1}", report.hours.toString())
                    setFieldValue(form, "publicaciones_${index + 1}", report.publications.toString())
                    setFieldValue(form, "videos_${index + 1}", report.videos.toString())
                    setFieldValue(form, "revisitas_${index + 1}", report.returnVisits.toString())
                    setFieldValue(form, "estudios_${index + 1}", report.bibleStudies.toString())
                }

                // Calcular totales
                val totalHours = reports.sumOf { it.hours }
                val totalPublications = reports.sumOf { it.publications }
                val totalVideos = reports.sumOf { it.videos }
                val totalReturnVisits = reports.sumOf { it.returnVisits }
                val totalBibleStudies = reports.sumOf { it.bibleStudies }

                setFieldValue(form, "totalHoras", totalHours.toString())
                setFieldValue(form, "totalPublicaciones", totalPublications.toString())
                setFieldValue(form, "totalVideos", totalVideos.toString())
                setFieldValue(form, "totalRevisitas", totalReturnVisits.toString())
                setFieldValue(form, "totalEstudios", totalBibleStudies.toString())

                // Flatten form to make it read-only (optional)
                // form.flattenFields()
            }

            pdfDocument.close()
            return outputFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Rellena el formulario S-1 (Informe de Predicación y Asistencia)
     * @param context Contexto de la aplicación
     * @param month Mes del informe (1-12)
     * @param year Año del informe
     * @param publishers Lista de todos los publicadores activos
     * @param reports Lista de informes del mes
     * @param meetings Lista de reuniones del mes
     * @return File del PDF rellenado
     */
    fun fillS1Form(
        context: Context,
        month: Int,
        year: Int,
        publishers: List<Publisher>,
        reports: List<Report>,
        meetings: List<Meeting>
    ): File? {
        val templateFile = PdfFormsManager.getS1PdfFile(context) ?: return null
        val monthName = SimpleDateFormat("MMMM", Locale("es", "ES")).format(
            Calendar.getInstance().apply {
                set(Calendar.MONTH, month - 1)
            }.time
        )
        val outputFile = File(context.cacheDir, "S-1_${monthName}_$year.pdf")

        try {
            val pdfReader = PdfReader(templateFile)
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfReader, pdfWriter)
            val form = PdfAcroForm.getAcroForm(pdfDocument, false)

            if (form != null) {
                // Información del mes
                setFieldValue(form, "mes", monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                setFieldValue(form, "ano", year.toString())

                // Número de publicadores por tipo
                val regularPublishers = publishers.count { it.type == PublisherType.PUBLICADOR && it.isActive }
                val auxiliaryPioneers = publishers.count { it.type == PublisherType.PRECURSOR_AUXILIAR && it.isActive }
                val regularPioneers = publishers.count { it.type == PublisherType.PRECURSOR_REGULAR && it.isActive }
                val specialPioneers = publishers.count { it.type == PublisherType.PRECURSOR_ESPECIAL && it.isActive }

                setFieldValue(form, "publicadores", regularPublishers.toString())
                setFieldValue(form, "precursoresAuxiliares", auxiliaryPioneers.toString())
                setFieldValue(form, "precursoresRegulares", regularPioneers.toString())
                setFieldValue(form, "precursoresEspeciales", specialPioneers.toString())

                // Totales de actividad de predicación
                val totalHours = reports.sumOf { it.hours }
                val totalPublications = reports.sumOf { it.publications }
                val totalVideos = reports.sumOf { it.videos }
                val totalReturnVisits = reports.sumOf { it.returnVisits }
                val totalBibleStudies = reports.sumOf { it.bibleStudies }

                setFieldValue(form, "totalHoras", totalHours.toString())
                setFieldValue(form, "totalPublicaciones", totalPublications.toString())
                setFieldValue(form, "totalVideos", totalVideos.toString())
                setFieldValue(form, "totalRevisitas", totalReturnVisits.toString())
                setFieldValue(form, "totalEstudios", totalBibleStudies.toString())

                // Asistencia a reuniones
                val weekendMeetings = meetings.filter { it.type == MeetingType.FIN_DE_SEMANA }
                val midweekMeetings = meetings.filter { it.type == MeetingType.ENTRE_SEMANA }

                if (weekendMeetings.isNotEmpty()) {
                    val avgWeekend = weekendMeetings.map { it.totalAttendance }.average().toInt()
                    setFieldValue(form, "asistenciaFinSemana", avgWeekend.toString())
                }

                if (midweekMeetings.isNotEmpty()) {
                    val avgMidweek = midweekMeetings.map { it.totalAttendance }.average().toInt()
                    setFieldValue(form, "asistenciaEntreSemana", avgMidweek.toString())
                }

                // Flatten form to make it read-only (optional)
                // form.flattenFields()
            }

            pdfDocument.close()
            return outputFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Rellena el formulario S-88 (Registro de Asistencia a Reuniones)
     * @param context Contexto de la aplicación
     * @param serviceYear Año de servicio
     * @param publishers Lista de publicadores
     * @param meetings Lista de reuniones del año
     * @param attendances Lista de asistencias
     * @return File del PDF rellenado
     */
    fun fillS88Form(
        context: Context,
        serviceYear: Int,
        publishers: List<Publisher>,
        meetings: List<Meeting>,
        attendances: List<Attendance>
    ): File? {
        val templateFile = PdfFormsManager.getS88PdfFile(context) ?: return null
        val outputFile = File(context.cacheDir, "S-88_$serviceYear.pdf")

        try {
            val pdfReader = PdfReader(templateFile)
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfReader, pdfWriter)
            val form = PdfAcroForm.getAcroForm(pdfDocument, false)

            if (form != null) {
                // Año de servicio
                setFieldValue(form, "anoServicio", "$serviceYear-${serviceYear + 1}")

                // Rellenar nombres de publicadores
                publishers.take(50).forEachIndexed { index, publisher ->
                    setFieldValue(form, "nombre_${index + 1}", publisher.name)
                }

                // Separar reuniones por tipo
                val weekendMeetings = meetings.filter { it.type == MeetingType.FIN_DE_SEMANA }
                val midweekMeetings = meetings.filter { it.type == MeetingType.ENTRE_SEMANA }

                // Rellenar asistencia para reuniones de fin de semana
                weekendMeetings.take(52).forEachIndexed { meetingIndex, meeting ->
                    val date = Date(meeting.date)
                    val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
                    setFieldValue(form, "fechaFinSemana_${meetingIndex + 1}", dateStr)

                    publishers.take(50).forEachIndexed { pubIndex, publisher ->
                        val wasPresent = attendances.any {
                            it.publisherId == publisher.id &&
                            it.meetingId == meeting.id &&
                            it.wasPresent
                        }
                        if (wasPresent) {
                            setFieldValue(form, "asistenciaFS_${pubIndex + 1}_${meetingIndex + 1}", "✓")
                        }
                    }
                }

                // Rellenar asistencia para reuniones entre semana
                midweekMeetings.take(52).forEachIndexed { meetingIndex, meeting ->
                    val date = Date(meeting.date)
                    val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
                    setFieldValue(form, "fechaEntreSemana_${meetingIndex + 1}", dateStr)

                    publishers.take(50).forEachIndexed { pubIndex, publisher ->
                        val wasPresent = attendances.any {
                            it.publisherId == publisher.id &&
                            it.meetingId == meeting.id &&
                            it.wasPresent
                        }
                        if (wasPresent) {
                            setFieldValue(form, "asistenciaES_${pubIndex + 1}_${meetingIndex + 1}", "✓")
                        }
                    }
                }

                // Flatten form to make it read-only (optional)
                // form.flattenFields()
            }

            pdfDocument.close()
            return outputFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Helper para establecer el valor de un campo del formulario
     */
    private fun setFieldValue(form: PdfAcroForm, fieldName: String, value: String) {
        try {
            val field = form.getField(fieldName)
            field?.setValue(value)
        } catch (e: Exception) {
            // Campo no existe en el PDF, ignorar silenciosamente
        }
    }

    /**
     * Obtiene todos los nombres de campos de un PDF (útil para debugging)
     */
    fun getFieldNames(context: Context, formType: String): List<String> {
        val pdfFile = when (formType) {
            "S-1" -> PdfFormsManager.getS1PdfFile(context)
            "S-21" -> PdfFormsManager.getS21PdfFile(context)
            "S-88" -> PdfFormsManager.getS88PdfFile(context)
            else -> null
        } ?: return emptyList()

        return try {
            val pdfReader = PdfReader(pdfFile)
            val pdfDocument = PdfDocument(pdfReader)
            val form = PdfAcroForm.getAcroForm(pdfDocument, false)

            val fieldNames = mutableListOf<String>()
            form?.formFields?.keys?.forEach { name ->
                fieldNames.add(name)
            }

            pdfDocument.close()
            fieldNames
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
