package com.congregation.reports.data

import androidx.lifecycle.LiveData

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    fun getAttendanceByMeeting(meetingId: Long): LiveData<List<Attendance>> {
        return attendanceDao.getAttendanceByMeeting(meetingId)
    }

    fun getAttendanceByPublisher(publisherId: Long): LiveData<List<Attendance>> {
        return attendanceDao.getAttendanceByPublisher(publisherId)
    }

    fun getAttendance(publisherId: Long, meetingId: Long): LiveData<Attendance?> {
        return attendanceDao.getAttendance(publisherId, meetingId)
    }

    fun getPresentCount(meetingId: Long): LiveData<Int> {
        return attendanceDao.getPresentCount(meetingId)
    }

    suspend fun insert(attendance: Attendance): Long {
        return attendanceDao.insert(attendance)
    }

    suspend fun update(attendance: Attendance) {
        attendanceDao.update(attendance)
    }

    suspend fun delete(attendance: Attendance) {
        attendanceDao.delete(attendance)
    }
}
