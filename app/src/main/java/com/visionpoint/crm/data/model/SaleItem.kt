package com.visionpoint.crm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleId: Int,
    val category: String = "Product", // "Product" or "Service"
    val itemType: String? = "Frames", // Configurable
    val itemDetail: String? = null,
    val name: String,
    val note: String? = null,
    val price: Double = 0.0,
    val quantity: Int = 1,
    val amount: Double = 0.0
)
