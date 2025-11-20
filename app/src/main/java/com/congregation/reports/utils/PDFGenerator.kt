package com.congregation.reports.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.Report
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PDFGenerator(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 50f
        private const val LINE_HEIGHT = 20f
    }

    /**
     * Genera un PDF con el formato oficial S-4 (Informe de la Congregación)
     */
    fun generateMonthlyReport(
        month: Int,
        year: Int,
        reports: List<Pair<Publisher, Report?>>,
        totalHours: Int,
        averageHours: Float
    ): File? {
        try {
            val document = PdfDocument()

            // Create page 1
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Configure paint for text
            val titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val headerPaint = Paint().apply {
                textSize = 14f
                isFakeBoldText = true
            }

            val normalPaint = Paint().apply {
                textSize = 12f
            }

            val smallPaint = Paint().apply {
                textSize = 10f
            }

            var yPosition = MARGIN + 40f

            // Title
            val monthName = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                .format(Calendar.getInstance().apply {
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.YEAR, year)
                }.time)
                .replaceFirstChar { it.uppercase() }

            canvas.drawText(
                "INFORME DE SERVICIO DEL CAMPO",
                PAGE_WIDTH / 2f,
                yPosition,
                titlePaint
            )

            yPosition += LINE_HEIGHT * 1.5f
            canvas.drawText(
                monthName,
                PAGE_WIDTH / 2f,
                yPosition,
                titlePaint
            )

            yPosition += LINE_HEIGHT * 2f

            // Summary section
            canvas.drawText("RESUMEN DEL MES", MARGIN, yPosition, headerPaint)
            yPosition += LINE_HEIGHT * 1.5f

            canvas.drawText("Total de Publicadores Activos: ${reports.size}", MARGIN, yPosition, normalPaint)
            yPosition += LINE_HEIGHT

            val reportsSubmitted = reports.count { it.second != null }
            canvas.drawText("Informes Recibidos: $reportsSubmitted", MARGIN, yPosition, normalPaint)
            yPosition += LINE_HEIGHT

            canvas.drawText("Horas Totales: $totalHours hrs", MARGIN, yPosition, normalPaint)
            yPosition += LINE_HEIGHT

            canvas.drawText(
                "Promedio por Publicador: ${String.format("%.1f", averageHours)} hrs",
                MARGIN,
                yPosition,
                normalPaint
            )

            yPosition += LINE_HEIGHT * 2f

            // Table header
            canvas.drawText("DETALLE DE PUBLICADORES", MARGIN, yPosition, headerPaint)
            yPosition += LINE_HEIGHT * 1.5f

            // Column headers
            canvas.drawText("Nombre", MARGIN, yPosition, smallPaint)
            canvas.drawText("Tipo", MARGIN + 150f, yPosition, smallPaint)
            canvas.drawText("Hrs", MARGIN + 300f, yPosition, smallPaint)
            canvas.drawText("Pub", MARGIN + 350f, yPosition, smallPaint)
            canvas.drawText("Vid", MARGIN + 400f, yPosition, smallPaint)
            canvas.drawText("Rev", MARGIN + 450f, yPosition, smallPaint)
            canvas.drawText("Est", MARGIN + 500f, yPosition, smallPaint)

            yPosition += LINE_HEIGHT

            // Draw line
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, normalPaint)
            yPosition += LINE_HEIGHT

            // Publisher data
            for ((publisher, report) in reports.take(25)) { // Limit to 25 per page
                if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                    document.finishPage(page)
                    // Would create new page here for longer lists
                    break
                }

                val publisherType = when (publisher.type) {
                    com.congregation.reports.data.PublisherType.PUBLICADOR -> "Pub"
                    com.congregation.reports.data.PublisherType.PRECURSOR_AUXILIAR -> "P.Aux"
                    com.congregation.reports.data.PublisherType.PRECURSOR_REGULAR -> "P.Reg"
                    com.congregation.reports.data.PublisherType.PRECURSOR_ESPECIAL -> "P.Esp"
                }

                val name = if (publisher.name.length > 20) {
                    publisher.name.substring(0, 17) + "..."
                } else {
                    publisher.name
                }

                canvas.drawText(name, MARGIN, yPosition, smallPaint)
                canvas.drawText(publisherType, MARGIN + 150f, yPosition, smallPaint)

                if (report != null) {
                    canvas.drawText(report.hours.toString(), MARGIN + 300f, yPosition, smallPaint)
                    canvas.drawText(report.publications.toString(), MARGIN + 350f, yPosition, smallPaint)
                    canvas.drawText(report.videos.toString(), MARGIN + 400f, yPosition, smallPaint)
                    canvas.drawText(report.returnVisits.toString(), MARGIN + 450f, yPosition, smallPaint)
                    canvas.drawText(report.bibleStudies.toString(), MARGIN + 500f, yPosition, smallPaint)
                } else {
                    canvas.drawText("-", MARGIN + 300f, yPosition, smallPaint)
                    canvas.drawText("-", MARGIN + 350f, yPosition, smallPaint)
                    canvas.drawText("-", MARGIN + 400f, yPosition, smallPaint)
                    canvas.drawText("-", MARGIN + 450f, yPosition, smallPaint)
                    canvas.drawText("-", MARGIN + 500f, yPosition, smallPaint)
                }

                yPosition += LINE_HEIGHT
            }

            // Footer
            yPosition = PAGE_HEIGHT - MARGIN
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
            canvas.drawText(
                "Generado: ${dateFormat.format(Date())}",
                MARGIN,
                yPosition,
                smallPaint
            )

            document.finishPage(page)

            // Save the document
            val fileNameBase = monthName.replace(" ", "_")
            val fileName = "Informe_$fileNameBase.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            document.writeTo(FileOutputStream(file))
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Genera un PDF con la lista de publicadores irregulares
     */
    fun generateIrregularPublishersReport(
        publishers: List<Publisher>,
        month: Int,
        year: Int
    ): File? {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val headerPaint = Paint().apply {
                textSize = 14f
                isFakeBoldText = true
            }

            val normalPaint = Paint().apply {
                textSize = 12f
            }

            var yPosition = MARGIN + 40f

            // Title
            canvas.drawText(
                "PUBLICADORES IRREGULARES",
                PAGE_WIDTH / 2f,
                yPosition,
                titlePaint
            )

            yPosition += LINE_HEIGHT * 2f

            val monthName = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                .format(Calendar.getInstance().apply {
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.YEAR, year)
                }.time)
                .replaceFirstChar { it.uppercase() }

            canvas.drawText(
                "Período: $monthName",
                MARGIN,
                yPosition,
                headerPaint
            )

            yPosition += LINE_HEIGHT * 1.5f

            canvas.drawText(
                "Total de Publicadores Irregulares: ${publishers.size}",
                MARGIN,
                yPosition,
                normalPaint
            )

            yPosition += LINE_HEIGHT * 2f

            // List publishers
            for ((index, publisher) in publishers.withIndex()) {
                if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                    break
                }

                canvas.drawText(
                    "${index + 1}. ${publisher.name}",
                    MARGIN,
                    yPosition,
                    normalPaint
                )

                yPosition += LINE_HEIGHT
            }

            // Footer
            yPosition = PAGE_HEIGHT - MARGIN
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
            canvas.drawText(
                "Generado: ${dateFormat.format(Date())}",
                MARGIN,
                yPosition,
                normalPaint
            )

            document.finishPage(page)

            // Save
            val fileNameBase = monthName.replace(" ", "_")
            val fileName = "Publicadores_Irregulares_$fileNameBase.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            document.writeTo(FileOutputStream(file))
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
