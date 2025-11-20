package com.congregation.reports.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: MeetingType,
    val date: Long, // timestamp
    val totalAttendance: Int = 0
)

enum class MeetingType {
    FIN_DE_SEMANA, // Reunión de fin de semana
    ENTRE_SEMANA,  // Reunión entre semana
    ASAMBLEA,
    CONGRESO,
    MEMORIAL
}
