package com.visionpoint.crm.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "customers",
    indices = [Index(value = ["phone"], unique = true)]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phone: String,
    val gender: String? = null,
    val dob: LocalDate? = null,
    val age: Int? = null,
    val address: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
