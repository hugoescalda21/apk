package com.congregation.reports.utils

import android.content.Context
import androidx.work.*
import com.congregation.reports.workers.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val MONTHLY_REMINDER_WORK = "monthly_reminder_work"
    private const val PENDING_REPORTS_WORK = "pending_reports_work"
    private const val INACTIVE_CHECK_WORK = "inactive_check_work"

    fun scheduleAllNotifications(context: Context) {
        scheduleMonthlyReminder(context)
        schedulePendingReportsCheck(context)
        scheduleInactivePublisherCheck(context)
    }

    /**
     * Programa recordatorio mensual para el día 25 de cada mes
     */
    private fun scheduleMonthlyReminder(context: Context) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 25)
        calendar.set(Calendar.HOUR_OF_DAY, 9) // 9:00 AM
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // Si ya pasó el día 25 de este mes, programar para el próximo mes
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val inputData = workDataOf(
            NotificationWorker.WORK_TYPE to NotificationWorker.TYPE_MONTHLY_REMINDER
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            30, TimeUnit.DAYS // Aproximadamente cada mes
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MONTHLY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Programa verificación de informes pendientes para el día 1 de cada mes
     */
    private fun schedulePendingReportsCheck(context: Context) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 10) // 10:00 AM
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        // Si ya pasó el día 1 de este mes, programar para el próximo mes
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val inputData = workDataOf(
            NotificationWorker.WORK_TYPE to NotificationWorker.TYPE_PENDING_REPORTS
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            30, TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PENDING_REPORTS_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Programa verificación de publicadores inactivos cada semana
     */
    private fun scheduleInactivePublisherCheck(context: Context) {
        val inputData = workDataOf(
            NotificationWorker.WORK_TYPE to NotificationWorker.TYPE_INACTIVE_CHECK
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            7, TimeUnit.DAYS // Cada semana
        )
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            INACTIVE_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Programa recordatorio de reunión para un día y hora específicos
     */
    fun scheduleMeetingReminder(context: Context, dayOfWeek: Int, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        // Si ya pasó el día de esta semana, programar para la próxima semana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val inputData = workDataOf(
            NotificationWorker.WORK_TYPE to NotificationWorker.TYPE_MEETING_REMINDER
        )

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            7, TimeUnit.DAYS // Cada semana
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "meeting_reminder_$dayOfWeek",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Cancela todas las notificaciones programadas
     */
    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MONTHLY_REMINDER_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(PENDING_REPORTS_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(INACTIVE_CHECK_WORK)
    }
}
