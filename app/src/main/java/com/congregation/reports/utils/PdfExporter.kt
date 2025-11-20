package com.congregation.reports.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.Report
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {

    fun exportMonthlyReport(
        month: Int,
        year: Int,
        publishers: List<Publisher>,
        reports: List<Report>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            paint.textSize = 16f

            var yPosition = 50f

            // Title
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("Informe de Servicio del Campo", 50f, yPosition, paint)
            yPosition += 40f

            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Mes: ${getMonthName(month)} $year", 50f, yPosition, paint)
            yPosition += 30f

            // Headers
            paint.isFakeBoldText = true
            canvas.drawText("Publicador", 50f, yPosition, paint)
            canvas.drawText("Horas", 250f, yPosition, paint)
            canvas.drawText("Pub.", 320f, yPosition, paint)
            canvas.drawText("Videos", 380f, yPosition, paint)
            canvas.drawText("Revis.", 450f, yPosition, paint)
            canvas.drawText("Est.", 510f, yPosition, paint)
            yPosition += 25f
            paint.isFakeBoldText = false

            // Draw line
            canvas.drawLine(50f, yPosition, 550f, yPosition, paint)
            yPosition += 20f

            // Reports
            var totalHours = 0
            var totalPublications = 0
            var totalVideos = 0
            var totalReturnVisits = 0
            var totalStudies = 0

            reports.forEach { report ->
                val publisher = publishers.find { it.id == report.publisherId }
                publisher?.let {
                    canvas.drawText(it.name.take(25), 50f, yPosition, paint)
                    canvas.drawText(report.hours.toString(), 250f, yPosition, paint)
                    canvas.drawText(report.publications.toString(), 320f, yPosition, paint)
                    canvas.drawText(report.videos.toString(), 380f, yPosition, paint)
                    canvas.drawText(report.returnVisits.toString(), 450f, yPosition, paint)
                    canvas.drawText(report.bibleStudies.toString(), 510f, yPosition, paint)

                    totalHours += report.hours
                    totalPublications += report.publications
                    totalVideos += report.videos
                    totalReturnVisits += report.returnVisits
                    totalStudies += report.bibleStudies

                    yPosition += 20f

                    // New page if needed
                    if (yPosition > 800f) {
                        pdfDocument.finishPage(page)
                        val newPage = pdfDocument.startPage(pageInfo)
                        yPosition = 50f
                    }
                }
            }

            // Totals
            yPosition += 10f
            canvas.drawLine(50f, yPosition, 550f, yPosition, paint)
            yPosition += 20f
            paint.isFakeBoldText = true
            canvas.drawText("TOTALES:", 50f, yPosition, paint)
            canvas.drawText(totalHours.toString(), 250f, yPosition, paint)
            canvas.drawText(totalPublications.toString(), 320f, yPosition, paint)
            canvas.drawText(totalVideos.toString(), 380f, yPosition, paint)
            canvas.drawText(totalReturnVisits.toString(), 450f, yPosition, paint)
            canvas.drawText(totalStudies.toString(), 510f, yPosition, paint)

            pdfDocument.finishPage(page)

            // Save file
            val fileName = "Informe_${getMonthName(month)}_$year.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return months[month - 1]
    }
}
