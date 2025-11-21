package com.congregation.reports.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FieldServiceGroupDao {

    @Query("SELECT * FROM field_service_groups WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveGroups(): LiveData<List<FieldServiceGroup>>

    @Query("SELECT * FROM field_service_groups ORDER BY name ASC")
    fun getAllGroups(): LiveData<List<FieldServiceGroup>>

    @Query("SELECT * FROM field_service_groups WHERE id = :groupId")
    fun getGroupById(groupId: Long): LiveData<FieldServiceGroup?>

    @Query("SELECT * FROM field_service_groups WHERE id = :groupId")
    suspend fun getGroupByIdSync(groupId: Long): FieldServiceGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: FieldServiceGroup): Long

    @Update
    suspend fun update(group: FieldServiceGroup)

    @Delete
    suspend fun delete(group: FieldServiceGroup)

    @Query("UPDATE field_service_groups SET isActive = 0 WHERE id = :groupId")
    suspend fun deactivateGroup(groupId: Long)
}
