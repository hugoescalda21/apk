package com.congregation.reports.utils

import android.content.Context
import android.os.Environment
import com.congregation.reports.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {

    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            // Close database
            val database = AppDatabase.getDatabase(context)
            database.close()

            // Source database file
            val currentDBPath = context.getDatabasePath("congregation_reports_database")

            // Destination backup file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "congregation_backup_$timestamp.db"
            val backupFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                backupFileName
            )

            // Copy database
            if (currentDBPath.exists()) {
                FileInputStream(currentDBPath).use { input ->
                    FileOutputStream(backupFile).use { output ->
                        input.copyTo(output)
                    }
                }
                backupFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Close database
            val database = AppDatabase.getDatabase(context)
            database.close()

            // Destination database file
            val currentDBPath = context.getDatabasePath("congregation_reports_database")

            // Copy backup to database location
            FileInputStream(backupFile).use { input ->
                FileOutputStream(currentDBPath).use { output ->
                    input.copyTo(output)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getBackupFiles(): List<File> {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return documentsDir?.listFiles { file ->
            file.name.startsWith("congregation_backup_") && file.name.endsWith(".db")
        }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
