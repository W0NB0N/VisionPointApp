package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.visionpoint.crm.data.model.Prescription
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: Prescription): Long

    @Update
    suspend fun update(prescription: Prescription)

    @Delete
    suspend fun delete(prescription: Prescription)

    @Query("SELECT * FROM prescriptions WHERE customerId = :customerId ORDER BY date DESC, id DESC")
    fun getByCustomerId(customerId: Int): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE customerId = :customerId ORDER BY date DESC, id DESC")
    suspend fun getByCustomerIdList(customerId: Int): List<Prescription>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    fun getById(id: Int): Flow<Prescription?>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getByIdOnce(id: Int): Prescription?
}
