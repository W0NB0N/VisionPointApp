package com.visionpoint.crm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Prescription::class,
            parentColumns = ["id"],
            childColumns = ["prescriptionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("customerId"),
        Index("prescriptionId")
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val prescriptionId: Int? = null,
    val saleDate: LocalDate,
    val recallDate: LocalDate? = null,
    val subtotal: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountType: String = "percentage", // "percentage" or "amount"
    val discountAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val status: String = "Pending", // "Pending", "Ready", "Completed"
    val createdAt: Long = System.currentTimeMillis()
)
