package com.congregation.reports

import android.app.Application
import android.os.Build
import com.congregation.reports.utils.NotificationScheduler
import com.google.android.material.color.DynamicColors

class CongregationReportsApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable dynamic colors for Android 12+ (Material You)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        // Schedule smart notifications
        NotificationScheduler.scheduleAllNotifications(this)
    }
}
