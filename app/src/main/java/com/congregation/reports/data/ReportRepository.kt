package com.congregation.reports.data

import androidx.lifecycle.LiveData

class ReportRepository(private val reportDao: ReportDao) {
    fun getAllReports(): LiveData<List<Report>> {
        return reportDao.getAllReports()
    }

    fun getReportsByMonth(month: Int, year: Int): LiveData<List<Report>> {
        return reportDao.getReportsByMonth(month, year)
    }

    fun getReportsByPublisher(publisherId: Long): LiveData<List<Report>> {
        return reportDao.getReportsByPublisher(publisherId)
    }

    fun getReport(publisherId: Long, month: Int, year: Int): LiveData<Report?> {
        return reportDao.getReport(publisherId, month, year)
    }

    fun getReportsCountForMonth(month: Int, year: Int): LiveData<Int> {
        return reportDao.getReportsCountForMonth(month, year)
    }

    fun getTotalHoursForMonth(month: Int, year: Int): LiveData<Int> {
        return reportDao.getTotalHoursForMonth(month, year)
    }

    suspend fun insert(report: Report): Long {
        return reportDao.insert(report)
    }

    suspend fun update(report: Report) {
        reportDao.update(report)
    }

    suspend fun delete(report: Report) {
        reportDao.delete(report)
    }
}
