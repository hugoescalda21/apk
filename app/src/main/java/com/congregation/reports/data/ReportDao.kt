package com.congregation.reports.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports WHERE month = :month AND year = :year ORDER BY publisherId")
    fun getReportsByMonth(month: Int, year: Int): LiveData<List<Report>>

    @Query("SELECT * FROM reports WHERE publisherId = :publisherId ORDER BY year DESC, month DESC")
    fun getReportsByPublisher(publisherId: Long): LiveData<List<Report>>

    @Query("SELECT * FROM reports WHERE publisherId = :publisherId AND month = :month AND year = :year")
    fun getReport(publisherId: Long, month: Int, year: Int): LiveData<Report?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: Report): Long

    @Update
    suspend fun update(report: Report)

    @Delete
    suspend fun delete(report: Report)

    @Query("SELECT COUNT(*) FROM reports WHERE month = :month AND year = :year")
    fun getReportsCountForMonth(month: Int, year: Int): LiveData<Int>

    @Query("SELECT SUM(hours) FROM reports WHERE month = :month AND year = :year")
    fun getTotalHoursForMonth(month: Int, year: Int): LiveData<Int>

    @Query("SELECT * FROM reports WHERE month = :month AND year = :year ORDER BY publisherId")
    suspend fun getReportsForMonthSync(month: Int, year: Int): List<Report>
}
