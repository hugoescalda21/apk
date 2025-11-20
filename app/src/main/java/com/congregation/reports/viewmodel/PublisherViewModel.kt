package com.congregation.reports.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.congregation.reports.data.AppDatabase
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.PublisherRepository
import kotlinx.coroutines.launch

class PublisherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PublisherRepository
    val allActivePublishers: LiveData<List<Publisher>>
    val allPublishers: LiveData<List<Publisher>>
    val activePublishersCount: LiveData<Int>

    init {
        val publisherDao = AppDatabase.getDatabase(application).publisherDao()
        repository = PublisherRepository(publisherDao)
        allActivePublishers = repository.allActivePublishers
        allPublishers = repository.allPublishers
        activePublishersCount = repository.activePublishersCount
    }

    fun insert(publisher: Publisher) = viewModelScope.launch {
        repository.insert(publisher)
    }

    fun update(publisher: Publisher) = viewModelScope.launch {
        repository.update(publisher)
    }

    fun delete(publisher: Publisher) = viewModelScope.launch {
        repository.delete(publisher)
    }

    fun getPublisherById(id: Long): LiveData<Publisher> {
        return repository.getPublisherById(id)
    }
}
