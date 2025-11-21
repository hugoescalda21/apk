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
    val isActive: Boolean = true,
    val groupId: Long? = null, // ID del grupo de predicación
    val dateOfBirth: String = "", // Fecha de nacimiento para formularios
    val dateOfBaptism: String = "", // Fecha de bautismo
    val emergencyContact: String = "", // Contacto de emergencia
    val address: String = "" // Dirección completa
)

enum class PublisherType {
    PUBLICADOR,
    PRECURSOR_AUXILIAR,
    PRECURSOR_REGULAR,
    PRECURSOR_ESPECIAL
}
