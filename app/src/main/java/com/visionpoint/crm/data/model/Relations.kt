package com.visionpoint.crm.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CustomerWithPrescriptions(
    @Embedded val customer: Customer,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val prescriptions: List<Prescription>
)

data class CustomerWithSales(
    @Embedded val customer: Customer,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val sales: List<Sale>
)

data class SaleWithCustomer(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer
)

data class SaleWithDetails(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItem>,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val payments: List<Payment>,
    @Relation(
        parentColumn = "prescriptionId",
        entityColumn = "id"
    )
    val prescription: Prescription? = null
)
