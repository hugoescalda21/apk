package com.congregation.reports.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY date DESC")
    fun getAllMeetings(): LiveData<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    fun getMeetingById(id: Long): LiveData<Meeting>

    @Query("SELECT * FROM meetings WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getMeetingsByDateRange(startDate: Long, endDate: Long): LiveData<List<Meeting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meeting: Meeting): Long

    @Update
    suspend fun update(meeting: Meeting)

    @Delete
    suspend fun delete(meeting: Meeting)

    @Query("SELECT AVG(totalAttendance) FROM meetings WHERE type = :type")
    fun getAverageAttendanceByType(type: MeetingType): LiveData<Double>
}
