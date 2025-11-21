package com.congregation.reports.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        // Migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new field_service_groups table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `field_service_groups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `overseerPublisherId` INTEGER,
                        `assistantPublisherId` INTEGER,
                        `isActive` INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())

                // Add new columns to publishers table with default values
                database.execSQL("ALTER TABLE publishers ADD COLUMN groupId INTEGER DEFAULT NULL")
                database.execSQL("ALTER TABLE publishers ADD COLUMN dateOfBirth TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE publishers ADD COLUMN dateOfBaptism TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE publishers ADD COLUMN emergencyContact TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE publishers ADD COLUMN address TEXT NOT NULL DEFAULT ''")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "congregation_reports_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // Keep as fallback for unexpected migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
