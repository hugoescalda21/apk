package com.congregation.reports.utils

import android.content.Context
import android.os.Environment
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.Report
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExcelExporter(private val context: Context) {

    fun exportMonthlyReport(
        month: Int,
        year: Int,
        publishers: List<Publisher>,
        reports: List<Report>
    ): File? {
        try {
            val workbook: Workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Informes")

            // Header style
            val headerStyle = workbook.createCellStyle()
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerStyle.setFont(headerFont)
            headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = arrayOf(
                "Publicador", "Tipo", "Horas", "Publicaciones",
                "Videos", "Revisitas", "Estudios", "Comentarios"
            )

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Fill data
            var rowNum = 1
            var totalHours = 0
            var totalPublications = 0
            var totalVideos = 0
            var totalReturnVisits = 0
            var totalStudies = 0

            reports.forEach { report ->
                val publisher = publishers.find { it.id == report.publisherId }
                publisher?.let {
                    val row = sheet.createRow(rowNum++)

                    row.createCell(0).setCellValue(it.name)
                    row.createCell(1).setCellValue(it.type.name.replace("_", " "))
                    row.createCell(2).setCellValue(report.hours.toDouble())
                    row.createCell(3).setCellValue(report.publications.toDouble())
                    row.createCell(4).setCellValue(report.videos.toDouble())
                    row.createCell(5).setCellValue(report.returnVisits.toDouble())
                    row.createCell(6).setCellValue(report.bibleStudies.toDouble())
                    row.createCell(7).setCellValue(report.comments)

                    totalHours += report.hours
                    totalPublications += report.publications
                    totalVideos += report.videos
                    totalReturnVisits += report.returnVisits
                    totalStudies += report.bibleStudies
                }
            }

            // Add totals row
            val totalRow = sheet.createRow(rowNum)
            val totalCellStyle = workbook.createCellStyle()
            val totalFont = workbook.createFont()
            totalFont.bold = true
            totalCellStyle.setFont(totalFont)

            totalRow.createCell(0).apply {
                setCellValue("TOTALES")
                cellStyle = totalCellStyle
            }
            totalRow.createCell(2).apply {
                setCellValue(totalHours.toDouble())
                cellStyle = totalCellStyle
            }
            totalRow.createCell(3).apply {
                setCellValue(totalPublications.toDouble())
                cellStyle = totalCellStyle
            }
            totalRow.createCell(4).apply {
                setCellValue(totalVideos.toDouble())
                cellStyle = totalCellStyle
            }
            totalRow.createCell(5).apply {
                setCellValue(totalReturnVisits.toDouble())
                cellStyle = totalCellStyle
            }
            totalRow.createCell(6).apply {
                setCellValue(totalStudies.toDouble())
                cellStyle = totalCellStyle
            }

            // Auto-size columns
            for (i in 0..7) {
                sheet.autoSizeColumn(i)
            }

            // Save file
            val fileName = "Informe_${getMonthName(month)}_$year.xlsx"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val fileOutputStream = FileOutputStream(file)
            workbook.write(fileOutputStream)
            fileOutputStream.close()
            workbook.close()

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
