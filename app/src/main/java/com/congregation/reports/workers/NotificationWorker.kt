package com.congregation.reports.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.congregation.reports.R
import com.congregation.reports.data.AppDatabase
import com.congregation.reports.ui.MainActivity
import com.congregation.reports.ui.ReportListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "congregation_reports_channel"
        const val NOTIFICATION_ID_MONTHLY_REMINDER = 1
        const val NOTIFICATION_ID_PENDING_REPORTS = 2
        const val NOTIFICATION_ID_INACTIVE_PUBLISHER = 3
        const val NOTIFICATION_ID_MEETING = 4

        const val WORK_TYPE = "work_type"
        const val TYPE_MONTHLY_REMINDER = "monthly_reminder"
        const val TYPE_PENDING_REPORTS = "pending_reports"
        const val TYPE_INACTIVE_CHECK = "inactive_check"
        const val TYPE_MEETING_REMINDER = "meeting_reminder"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            createNotificationChannel()

            val workType = inputData.getString(WORK_TYPE) ?: TYPE_MONTHLY_REMINDER

            when (workType) {
                TYPE_MONTHLY_REMINDER -> checkMonthlyReminder()
                TYPE_PENDING_REPORTS -> checkPendingReports()
                TYPE_INACTIVE_CHECK -> checkInactivePublishers()
                TYPE_MEETING_REMINDER -> sendMeetingReminder()
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun checkMonthlyReminder() {
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Recordatorio el día 25 de cada mes
        if (dayOfMonth == 25) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(applicationContext, ReportListActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("📋 Recordatorio de Informes")
                .setContentText("Recuerda enviar tu informe de servicio del campo antes del fin de mes")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Es hora de recopilar los informes de servicio del campo. Asegúrate de que todos los publicadores entreguen su informe antes del día 1."))
                .build()

            notificationManager.notify(NOTIFICATION_ID_MONTHLY_REMINDER, notification)
        }
    }

    private suspend fun checkPendingReports() {
        val calendar = Calendar.getInstance()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Alerta el día 1 si hay informes pendientes
        if (dayOfMonth == 1) {
            val database = AppDatabase.getDatabase(applicationContext)
            val publisherDao = database.publisherDao()
            val reportDao = database.reportDao()

            // Obtener mes y año anterior
            val lastMonth = if (calendar.get(Calendar.MONTH) == 0) 12 else calendar.get(Calendar.MONTH)
            val lastYear = if (calendar.get(Calendar.MONTH) == 0) calendar.get(Calendar.YEAR) - 1 else calendar.get(Calendar.YEAR)

            val activePublishers = publisherDao.getActivePublishersSync()
            val reportsLastMonth = reportDao.getReportsForMonthSync(lastMonth, lastYear)

            val publishersWithReports = reportsLastMonth.map { it.publisherId }.toSet()
            val publishersWithoutReports = activePublishers.filter { it.id !in publishersWithReports }

            if (publishersWithoutReports.isNotEmpty()) {
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val intent = Intent(applicationContext, ReportListActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("⚠️ Informes Pendientes")
                    .setContentText("${publishersWithoutReports.size} publicador(es) no han entregado su informe")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("Hay ${publishersWithoutReports.size} publicador(es) que no han entregado su informe del mes pasado. Toca para ver detalles."))
                    .build()

                notificationManager.notify(NOTIFICATION_ID_PENDING_REPORTS, notification)
            }
        }
    }

    private suspend fun checkInactivePublishers() {
        val database = AppDatabase.getDatabase(applicationContext)
        val publisherDao = database.publisherDao()
        val reportDao = database.reportDao()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        // Calcular mes anterior
        val lastMonth = if (currentMonth == 1) 12 else currentMonth - 1
        val lastYear = if (currentMonth == 1) currentYear - 1 else currentYear

        // Calcular hace 2 meses
        val twoMonthsAgo = if (currentMonth <= 2) 12 + currentMonth - 2 else currentMonth - 2
        val yearTwoMonthsAgo = if (currentMonth <= 2) currentYear - 1 else currentYear

        val activePublishers = publisherDao.getActivePublishersSync()
        val reportsLastMonth = reportDao.getReportsForMonthSync(lastMonth, lastYear)
        val reportsTwoMonthsAgo = reportDao.getReportsForMonthSync(twoMonthsAgo, yearTwoMonthsAgo)

        val publishersWithReportsLastMonth = reportsLastMonth.map { it.publisherId }.toSet()
        val publishersWithReportsTwoMonthsAgo = reportsTwoMonthsAgo.map { it.publisherId }.toSet()

        val inactivePublishers = activePublishers.filter {
            it.id !in publishersWithReportsLastMonth && it.id !in publishersWithReportsTwoMonthsAgo
        }

        if (inactivePublishers.isNotEmpty()) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(applicationContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val publisherNames = inactivePublishers.take(3).joinToString(", ") { it.name }
            val moreCount = if (inactivePublishers.size > 3) " y ${inactivePublishers.size - 3} más" else ""

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🔔 Publicadores Inactivos")
                .setContentText("${inactivePublishers.size} publicador(es) sin informes por 2 meses")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Los siguientes publicadores no han entregado informes en los últimos 2 meses: $publisherNames$moreCount"))
                .build()

            notificationManager.notify(NOTIFICATION_ID_INACTIVE_PUBLISHER, notification)
        }
    }

    private suspend fun sendMeetingReminder() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📅 Recordatorio de Reunión")
            .setContentText("Recuerda tomar asistencia en la reunión de hoy")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_MEETING, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Congregación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para informes, reuniones y actividad de publicadores"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
