package com.congregation.reports.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.congregation.reports.data.AppDatabase
import com.congregation.reports.data.Attendance
import com.congregation.reports.data.AttendanceRepository
import kotlinx.coroutines.launch

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AttendanceRepository

    init {
        val attendanceDao = AppDatabase.getDatabase(application).attendanceDao()
        repository = AttendanceRepository(attendanceDao)
    }

    fun getAttendanceByMeeting(meetingId: Long): LiveData<List<Attendance>> {
        return repository.getAttendanceByMeeting(meetingId)
    }

    fun getAttendanceByPublisher(publisherId: Long): LiveData<List<Attendance>> {
        return repository.getAttendanceByPublisher(publisherId)
    }

    fun getAttendance(publisherId: Long, meetingId: Long): LiveData<Attendance?> {
        return repository.getAttendance(publisherId, meetingId)
    }

    fun getPresentCount(meetingId: Long): LiveData<Int> {
        return repository.getPresentCount(meetingId)
    }

    fun insert(attendance: Attendance) = viewModelScope.launch {
        repository.insert(attendance)
    }

    fun update(attendance: Attendance) = viewModelScope.launch {
        repository.update(attendance)
    }

    fun delete(attendance: Attendance) = viewModelScope.launch {
        repository.delete(attendance)
    }
}
