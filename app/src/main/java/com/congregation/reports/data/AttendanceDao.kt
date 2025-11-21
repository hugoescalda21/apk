package com.congregation.reports.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance")
    fun getAllAttendances(): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE meetingId = :meetingId")
    fun getAttendanceByMeeting(meetingId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE publisherId = :publisherId ORDER BY meetingId DESC")
    fun getAttendanceByPublisher(publisherId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE publisherId = :publisherId AND meetingId = :meetingId")
    fun getAttendance(publisherId: Long, meetingId: Long): LiveData<Attendance?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance): Long

    @Update
    suspend fun update(attendance: Attendance)

    @Delete
    suspend fun delete(attendance: Attendance)

    @Query("SELECT COUNT(*) FROM attendance WHERE meetingId = :meetingId AND wasPresent = 1")
    fun getPresentCount(meetingId: Long): LiveData<Int>
}
