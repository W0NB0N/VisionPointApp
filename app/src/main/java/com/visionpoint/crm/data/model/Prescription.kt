package com.visionpoint.crm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "prescriptions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val date: LocalDate,
    val type: String? = "Specs", // "Specs" or "Contact Lens"
    val doctorName: String? = null,
    val rightEye: String? = null, // Stored as JSON string
    val leftEye: String? = null,  // Stored as JSON string
    val pd: String? = null,
    val psm: String? = null,
    val fh: String? = null,
    val notes: String? = null,
    val isPreviousLens: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
