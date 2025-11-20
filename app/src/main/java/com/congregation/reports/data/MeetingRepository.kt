package com.congregation.reports.data

import androidx.lifecycle.LiveData

class MeetingRepository(private val meetingDao: MeetingDao) {
    val allMeetings: LiveData<List<Meeting>> = meetingDao.getAllMeetings()

    fun getMeetingById(id: Long): LiveData<Meeting> {
        return meetingDao.getMeetingById(id)
    }

    fun getMeetingsByDateRange(startDate: Long, endDate: Long): LiveData<List<Meeting>> {
        return meetingDao.getMeetingsByDateRange(startDate, endDate)
    }

    fun getAverageAttendanceByType(type: MeetingType): LiveData<Double> {
        return meetingDao.getAverageAttendanceByType(type)
    }

    suspend fun insert(meeting: Meeting): Long {
        return meetingDao.insert(meeting)
    }

    suspend fun update(meeting: Meeting) {
        meetingDao.update(meeting)
    }

    suspend fun delete(meeting: Meeting) {
        meetingDao.delete(meeting)
    }
}
