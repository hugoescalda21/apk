package com.congregation.reports.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.congregation.reports.data.AppDatabase
import com.congregation.reports.data.FieldServiceGroup
import com.congregation.reports.data.FieldServiceGroupRepository
import kotlinx.coroutines.launch

class FieldServiceGroupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FieldServiceGroupRepository
    val allActiveGroups: LiveData<List<FieldServiceGroup>>
    val allGroups: LiveData<List<FieldServiceGroup>>

    init {
        val groupDao = AppDatabase.getDatabase(application).fieldServiceGroupDao()
        repository = FieldServiceGroupRepository(groupDao)
        allActiveGroups = repository.allActiveGroups
        allGroups = repository.allGroups
    }

    fun getGroupById(groupId: Long): LiveData<FieldServiceGroup?> {
        return repository.getGroupById(groupId)
    }

    fun insert(group: FieldServiceGroup, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insert(group)
            onComplete(id)
        }
    }

    fun update(group: FieldServiceGroup, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.update(group)
            onComplete()
        }
    }

    fun delete(group: FieldServiceGroup, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(group)
            onComplete()
        }
    }

    fun deactivateGroup(groupId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deactivateGroup(groupId)
            onComplete()
        }
    }
}
