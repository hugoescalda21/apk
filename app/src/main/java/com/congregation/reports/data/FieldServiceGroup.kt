package com.congregation.reports.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_service_groups")
data class FieldServiceGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val overseerPublisherId: Long? = null, // ID del superintendente del grupo
    val assistantPublisherId: Long? = null, // ID del ayudante del grupo
    val isActive: Boolean = true
)
