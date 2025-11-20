package com.congregation.reports.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reports",
    foreignKeys = [
        ForeignKey(
            entity = Publisher::class,
            parentColumns = ["id"],
            childColumns = ["publisherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("publisherId")]
)
data class Report(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val publisherId: Long,
    val month: Int, // 1-12
    val year: Int,
    val hours: Int = 0,
    val publications: Int = 0,
    val videos: Int = 0,
    val returnVisits: Int = 0,
    val bibleStudies: Int = 0,
    val comments: String = ""
)
