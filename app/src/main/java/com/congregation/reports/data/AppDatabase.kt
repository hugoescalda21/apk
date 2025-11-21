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
    version = 3,
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
                try {
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
                    // We use try-catch for each column in case it already exists
                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN groupId INTEGER DEFAULT NULL")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN dateOfBirth TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN dateOfBaptism TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN emergencyContact TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN address TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }

        // Migration from version 2 to version 3
        // This migration ensures field_service_groups table exists for users who upgraded to v2 before migration was added
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Ensure field_service_groups table exists
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS `field_service_groups` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `name` TEXT NOT NULL,
                            `overseerPublisherId` INTEGER,
                            `assistantPublisherId` INTEGER,
                            `isActive` INTEGER NOT NULL DEFAULT 1
                        )
                    """.trimIndent())

                    // Ensure new columns in publishers table exist
                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN groupId INTEGER DEFAULT NULL")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN dateOfBirth TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN dateOfBaptism TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN emergencyContact TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }

                    try {
                        database.execSQL("ALTER TABLE publishers ADD COLUMN address TEXT NOT NULL DEFAULT ''")
                    } catch (e: Exception) {
                        // Column already exists, ignore
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // Keep as fallback for unexpected migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
