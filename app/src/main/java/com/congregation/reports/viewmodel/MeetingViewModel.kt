package com.congregation.reports.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.congregation.reports.data.AppDatabase
import com.congregation.reports.data.Meeting
import com.congregation.reports.data.MeetingRepository
import com.congregation.reports.data.MeetingType
import kotlinx.coroutines.launch

class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MeetingRepository
    val allMeetings: LiveData<List<Meeting>>

    init {
        val meetingDao = AppDatabase.getDatabase(application).meetingDao()
        repository = MeetingRepository(meetingDao)
        allMeetings = repository.allMeetings
    }

    fun getMeetingById(id: Long): LiveData<Meeting> {
        return repository.getMeetingById(id)
    }

    fun getMeetingsByDateRange(startDate: Long, endDate: Long): LiveData<List<Meeting>> {
        return repository.getMeetingsByDateRange(startDate, endDate)
    }

    fun getAverageAttendanceByType(type: MeetingType): LiveData<Double> {
        return repository.getAverageAttendanceByType(type)
    }

    fun insert(meeting: Meeting) = viewModelScope.launch {
        repository.insert(meeting)
    }

    fun update(meeting: Meeting) = viewModelScope.launch {
        repository.update(meeting)
    }

    fun delete(meeting: Meeting) = viewModelScope.launch {
        repository.delete(meeting)
    }
}
