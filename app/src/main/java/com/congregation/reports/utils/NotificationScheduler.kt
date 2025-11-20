package com.congregation.reports.utils

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleMonthlyReminder(dayOfMonth: Int = 25) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If the due date has passed, schedule for next month
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.MONTH, 1)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag("monthly_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "monthly_report_reminder",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminders() {
        WorkManager.getInstance(context).cancelAllWorkByTag("monthly_reminder")
    }

    fun scheduleWeeklyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            7, TimeUnit.DAYS
        )
            .setInitialDelay(calculateNextSunday(), TimeUnit.MILLISECONDS)
            .addTag("weekly_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weekly_meeting_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateNextSunday(): Long {
        val currentDate = Calendar.getInstance()
        val nextSunday = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (nextSunday.before(currentDate)) {
            nextSunday.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return nextSunday.timeInMillis - currentDate.timeInMillis
    }
}
