package com.congregation.reports.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PublisherDao {
    @Query("SELECT * FROM publishers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActivePublishers(): LiveData<List<Publisher>>

    @Query("SELECT * FROM publishers ORDER BY name ASC")
    fun getAllPublishers(): LiveData<List<Publisher>>

    @Query("SELECT * FROM publishers WHERE id = :id")
    fun getPublisherById(id: Long): LiveData<Publisher>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(publisher: Publisher): Long

    @Update
    suspend fun update(publisher: Publisher)

    @Delete
    suspend fun delete(publisher: Publisher)

    @Query("SELECT COUNT(*) FROM publishers WHERE isActive = 1")
    fun getActivePublishersCount(): LiveData<Int>
}
