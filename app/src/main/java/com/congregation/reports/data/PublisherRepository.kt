package com.congregation.reports.data

import androidx.lifecycle.LiveData

class PublisherRepository(private val publisherDao: PublisherDao) {
    val allActivePublishers: LiveData<List<Publisher>> = publisherDao.getAllActivePublishers()
    val allPublishers: LiveData<List<Publisher>> = publisherDao.getAllPublishers()
    val activePublishersCount: LiveData<Int> = publisherDao.getActivePublishersCount()

    fun getPublisherById(id: Long): LiveData<Publisher> {
        return publisherDao.getPublisherById(id)
    }

    suspend fun insert(publisher: Publisher): Long {
        return publisherDao.insert(publisher)
    }

    suspend fun update(publisher: Publisher) {
        publisherDao.update(publisher)
    }

    suspend fun delete(publisher: Publisher) {
        publisherDao.delete(publisher)
    }
}
