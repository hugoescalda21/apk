package com.congregation.reports.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.congregation.reports.data.AppDatabase
import com.congregation.reports.data.Report
import com.congregation.reports.data.ReportRepository
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReportRepository

    init {
        val reportDao = AppDatabase.getDatabase(application).reportDao()
        repository = ReportRepository(reportDao)
    }

    fun getReportsByMonth(month: Int, year: Int): LiveData<List<Report>> {
        return repository.getReportsByMonth(month, year)
    }

    fun getReportsByPublisher(publisherId: Long): LiveData<List<Report>> {
        return repository.getReportsByPublisher(publisherId)
    }

    fun getReport(publisherId: Long, month: Int, year: Int): LiveData<Report?> {
        return repository.getReport(publisherId, month, year)
    }

    fun getReportsCountForMonth(month: Int, year: Int): LiveData<Int> {
        return repository.getReportsCountForMonth(month, year)
    }

    fun getTotalHoursForMonth(month: Int, year: Int): LiveData<Int> {
        return repository.getTotalHoursForMonth(month, year)
    }

    fun insert(report: Report) = viewModelScope.launch {
        repository.insert(report)
    }

    fun update(report: Report) = viewModelScope.launch {
        repository.update(report)
    }

    fun delete(report: Report) = viewModelScope.launch {
        repository.delete(report)
    }
}
