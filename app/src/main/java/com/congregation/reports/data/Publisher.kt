package com.congregation.reports.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "publishers")
data class Publisher(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String = "",
    val email: String = "",
    val type: PublisherType = PublisherType.PUBLICADOR,
    val isActive: Boolean = true
)

enum class PublisherType {
    PUBLICADOR,
    PRECURSOR_AUXILIAR,
    PRECURSOR_REGULAR,
    PRECURSOR_ESPECIAL
}
