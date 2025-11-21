package com.congregation.reports.utils

import android.content.Context
import java.io.File

object PdfFormsManager {

    /**
     * Obtiene la ruta del PDF S-1 cargado por el usuario
     */
    fun getS1PdfPath(context: Context): String? {
        val prefs = context.getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)
        val path = prefs.getString("S-1_path", null)
        return if (path != null && File(path).exists()) path else null
    }

    /**
     * Obtiene la ruta del PDF S-21 cargado por el usuario
     */
    fun getS21PdfPath(context: Context): String? {
        val prefs = context.getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)
        val path = prefs.getString("S-21_path", null)
        return if (path != null && File(path).exists()) path else null
    }

    /**
     * Obtiene la ruta del PDF S-88 cargado por el usuario
     */
    fun getS88PdfPath(context: Context): String? {
        val prefs = context.getSharedPreferences("pdf_prefs", Context.MODE_PRIVATE)
        val path = prefs.getString("S-88_path", null)
        return if (path != null && File(path).exists()) path else null
    }

    /**
     * Verifica si el PDF S-1 está cargado
     */
    fun isS1PdfLoaded(context: Context): Boolean {
        return getS1PdfPath(context) != null
    }

    /**
     * Verifica si el PDF S-21 está cargado
     */
    fun isS21PdfLoaded(context: Context): Boolean {
        return getS21PdfPath(context) != null
    }

    /**
     * Verifica si el PDF S-88 está cargado
     */
    fun isS88PdfLoaded(context: Context): Boolean {
        return getS88PdfPath(context) != null
    }

    /**
     * Verifica si todos los PDFs están cargados
     */
    fun areAllPdfsLoaded(context: Context): Boolean {
        return isS1PdfLoaded(context) && isS21PdfLoaded(context) && isS88PdfLoaded(context)
    }

    /**
     * Obtiene un File del PDF S-1
     */
    fun getS1PdfFile(context: Context): File? {
        val path = getS1PdfPath(context)
        return if (path != null) File(path) else null
    }

    /**
     * Obtiene un File del PDF S-21
     */
    fun getS21PdfFile(context: Context): File? {
        val path = getS21PdfPath(context)
        return if (path != null) File(path) else null
    }

    /**
     * Obtiene un File del PDF S-88
     */
    fun getS88PdfFile(context: Context): File? {
        val path = getS88PdfPath(context)
        return if (path != null) File(path) else null
    }
}
