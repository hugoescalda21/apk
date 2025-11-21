package com.congregation.reports.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.congregation.reports.data.Publisher
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generador de formularios oficiales PDF:
 * - S-21: Registro de Publicadores de la Congregación
 * - S-88: Solicitud de Publicador No Bautizado
 * - S-1: Solicitud de Servicio como Precursor
 */
class OfficialFormsGenerator(private val context: Context) {

    /**
     * Genera el formulario S-21 (Registro de Publicadores)
     */
    fun generateS21Form(publishers: List<Publisher>, congregationName: String): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            var yPosition = 50f

            // Título del formulario
            canvas.drawText("S-21", 297f, yPosition, titlePaint)
            yPosition += 20f
            canvas.drawText("REGISTRO DE PUBLICADORES DE LA CONGREGACIÓN", 297f, yPosition, titlePaint)
            yPosition += 30f

            // Nombre de la congregación
            canvas.drawText("Congregación: $congregationName", 50f, yPosition, boldPaint)
            yPosition += 25f

            // Encabezados de tabla
            val headerY = yPosition
            canvas.drawText("Nombre", 50f, headerY, boldPaint)
            canvas.drawText("F. Nac.", 200f, headerY, boldPaint)
            canvas.drawText("F. Bautismo", 270f, headerY, boldPaint)
            canvas.drawText("Tipo", 360f, headerY, boldPaint)
            canvas.drawText("Grupo", 450f, headerY, boldPaint)
            yPosition += 5f

            // Línea horizontal
            canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
            yPosition += 15f

            // Datos de publicadores
            publishers.sortedBy { it.name }.forEachIndexed { index, publisher ->
                if (yPosition > 800f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                canvas.drawText(publisher.name, 50f, yPosition, paint)
                canvas.drawText(publisher.dateOfBirth.ifEmpty { "—" }, 200f, yPosition, paint)
                canvas.drawText(publisher.dateOfBaptism.ifEmpty { "—" }, 270f, yPosition, paint)

                val type = when (publisher.type.name) {
                    "PRECURSOR_REGULAR" -> "P.R."
                    "PRECURSOR_AUXILIAR" -> "P.A."
                    "PRECURSOR_ESPECIAL" -> "P.E."
                    else -> "Pub."
                }
                canvas.drawText(type, 360f, yPosition, paint)
                canvas.drawText(publisher.groupId?.toString() ?: "—", 450f, yPosition, paint)

                yPosition += 15f

                // Línea divisoria cada 5 publicadores
                if ((index + 1) % 5 == 0) {
                    canvas.drawLine(50f, yPosition - 3f, 545f, yPosition - 3f, paint.apply {
                        strokeWidth = 0.5f
                    })
                }
            }

            // Información al pie
            yPosition = 800f
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            paint.textSize = 9f
            canvas.drawText("Total de publicadores: ${publishers.size}", 50f, yPosition, paint)
            canvas.drawText("Fecha de generación: $currentDate", 350f, yPosition, paint)

            pdfDocument.finishPage(page)

            // Guardar archivo
            val fileName = "S-21_Registro_Publicadores_$currentDate.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Genera el formulario S-88 (Solicitud de Publicador No Bautizado)
     */
    fun generateS88Form(publisher: Publisher, congregationName: String): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            var yPosition = 50f

            // Título
            canvas.drawText("S-88", 297f, yPosition, titlePaint)
            yPosition += 25f
            titlePaint.textSize = 13f
            canvas.drawText("SOLICITUD DE PUBLICADOR NO BAUTIZADO", 297f, yPosition, titlePaint)
            yPosition += 40f

            // Información de la congregación
            canvas.drawText("Congregación: $congregationName", 50f, yPosition, boldPaint)
            yPosition += 30f

            // Datos del solicitante
            canvas.drawText("DATOS PERSONALES", 50f, yPosition, boldPaint)
            yPosition += 20f

            canvas.drawText("Nombre completo:", 50f, yPosition, paint)
            canvas.drawText(publisher.name, 200f, yPosition, boldPaint)
            yPosition += 20f

            canvas.drawText("Fecha de nacimiento:", 50f, yPosition, paint)
            canvas.drawText(publisher.dateOfBirth.ifEmpty { "________________" }, 200f, yPosition, boldPaint)
            yPosition += 20f

            canvas.drawText("Dirección:", 50f, yPosition, paint)
            canvas.drawText(publisher.address.ifEmpty { "________________________________________________" }, 200f, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Teléfono:", 50f, yPosition, paint)
            canvas.drawText(publisher.phoneNumber.ifEmpty { "________________" }, 200f, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Correo electrónico:", 50f, yPosition, paint)
            canvas.drawText(publisher.email.ifEmpty { "________________" }, 200f, yPosition, paint)
            yPosition += 40f

            // Declaración
            canvas.drawText("DECLARACIÓN", 50f, yPosition, boldPaint)
            yPosition += 20f

            paint.textSize = 10f
            val declaration = "Deseo ser publicador no bautizado de las buenas nuevas del Reino de Dios.\n" +
                    "Creo que la Biblia es la Palabra de Dios y que sus enseñanzas son la verdad.\n" +
                    "Acepto estas enseñanzas y deseo vivir en armonía con ellas."

            declaration.split("\n").forEach { line ->
                canvas.drawText(line, 50f, yPosition, paint)
                yPosition += 15f
            }
            yPosition += 20f

            // Firmas
            canvas.drawText("Firma del solicitante: ___________________________", 50f, yPosition, paint)
            yPosition += 10f
            canvas.drawText("Fecha: _______________", 50f, yPosition, paint)
            yPosition += 40f

            canvas.drawText("Firma del anciano: ___________________________", 50f, yPosition, paint)
            yPosition += 10f
            canvas.drawText("Fecha: _______________", 50f, yPosition, paint)

            pdfDocument.finishPage(page)

            // Guardar archivo
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val fileName = "S-88_${publisher.name.replace(" ", "_")}_$currentDate.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Genera el formulario S-1 (Solicitud de Servicio como Precursor)
     */
    fun generateS1Form(publisher: Publisher, congregationName: String, pioneerType: String): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            var yPosition = 50f

            // Título
            canvas.drawText("S-1", 297f, yPosition, titlePaint)
            yPosition += 25f
            titlePaint.textSize = 13f
            canvas.drawText("SOLICITUD DE SERVICIO COMO PRECURSOR", 297f, yPosition, titlePaint)
            yPosition += 40f

            // Tipo de precursor
            canvas.drawText("Tipo de servicio: $pioneerType", 50f, yPosition, boldPaint)
            yPosition += 25f

            // Información de la congregación
            canvas.drawText("Congregación: $congregationName", 50f, yPosition, boldPaint)
            yPosition += 30f

            // Datos del solicitante
            canvas.drawText("DATOS PERSONALES", 50f, yPosition, boldPaint)
            yPosition += 20f

            canvas.drawText("Nombre completo:", 50f, yPosition, paint)
            canvas.drawText(publisher.name, 200f, yPosition, boldPaint)
            yPosition += 20f

            canvas.drawText("Fecha de nacimiento:", 50f, yPosition, paint)
            canvas.drawText(publisher.dateOfBirth.ifEmpty { "________________" }, 200f, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Fecha de bautismo:", 50f, yPosition, paint)
            canvas.drawText(publisher.dateOfBaptism.ifEmpty { "________________" }, 200f, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Dirección:", 50f, yPosition, paint)
            canvas.drawText(publisher.address.ifEmpty { "________________________________________________" }, 200f, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Teléfono:", 50f, yPosition, paint)
            canvas.drawText(publisher.phoneNumber.ifEmpty { "________________" }, 200f, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Correo electrónico:", 50f, yPosition, paint)
            canvas.drawText(publisher.email.ifEmpty { "________________" }, 200f, yPosition, paint)
            yPosition += 30f

            // Contacto de emergencia
            canvas.drawText("Contacto de emergencia:", 50f, yPosition, paint)
            canvas.drawText(publisher.emergencyContact.ifEmpty { "________________________________________________" }, 200f, yPosition, paint)
            yPosition += 40f

            // Declaración
            canvas.drawText("DECLARACIÓN", 50f, yPosition, boldPaint)
            yPosition += 20f

            paint.textSize = 10f
            val declaration = "Solicito ser nombrado ${pioneerType.lowercase()}.\n" +
                    "Entiendo que para calificar debo cumplir con los requisitos bíblicos y\n" +
                    "alcanzar la meta de horas establecida para este servicio.\n" +
                    "Estoy decidido a dar lo mejor de mí en el ministerio cristiano."

            declaration.split("\n").forEach { line ->
                canvas.drawText(line, 50f, yPosition, paint)
                yPosition += 15f
            }
            yPosition += 30f

            // Firmas
            canvas.drawText("Firma del solicitante: ___________________________", 50f, yPosition, paint)
            yPosition += 10f
            canvas.drawText("Fecha: _______________", 50f, yPosition, paint)
            yPosition += 40f

            canvas.drawText("Firma del cuerpo de ancianos: ___________________________", 50f, yPosition, paint)
            yPosition += 10f
            canvas.drawText("Fecha: _______________", 50f, yPosition, paint)

            pdfDocument.finishPage(page)

            // Guardar archivo
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val fileName = "S-1_${publisher.name.replace(" ", "_")}_$currentDate.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName
            )

            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
