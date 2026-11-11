package com.visionpoint.crm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "cashbook",
    foreignKeys = [
        ForeignKey(
            entity = Payment::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("paymentId")]
)
data class Cashbook(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate,
    val type: String, // "Credit" or "Debit"
    val amount: Double,
    val method: String? = "Cash", // "Cash", "Card", "UPI"
    val note: String? = null,
    val paymentId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
