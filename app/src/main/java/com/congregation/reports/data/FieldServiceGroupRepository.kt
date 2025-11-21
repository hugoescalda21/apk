package com.congregation.reports.data

import androidx.lifecycle.LiveData

class FieldServiceGroupRepository(private val groupDao: FieldServiceGroupDao) {

    val allActiveGroups: LiveData<List<FieldServiceGroup>> = groupDao.getAllActiveGroups()
    val allGroups: LiveData<List<FieldServiceGroup>> = groupDao.getAllGroups()

    fun getGroupById(groupId: Long): LiveData<FieldServiceGroup?> {
        return groupDao.getGroupById(groupId)
    }

    suspend fun getGroupByIdSync(groupId: Long): FieldServiceGroup? {
        return groupDao.getGroupByIdSync(groupId)
    }

    suspend fun insert(group: FieldServiceGroup): Long {
        return groupDao.insert(group)
    }

    suspend fun update(group: FieldServiceGroup) {
        groupDao.update(group)
    }

    suspend fun delete(group: FieldServiceGroup) {
        groupDao.delete(group)
    }

    suspend fun deactivateGroup(groupId: Long) {
        groupDao.deactivateGroup(groupId)
    }
}
