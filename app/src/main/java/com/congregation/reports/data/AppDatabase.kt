package com.congregation.reports.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Publisher::class, Report::class, Meeting::class, Attendance::class, FieldServiceGroup::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun publisherDao(): PublisherDao
    abstract fun reportDao(): ReportDao
    abstract fun meetingDao(): MeetingDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun fieldServiceGroupDao(): FieldServiceGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "congregation_reports_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
