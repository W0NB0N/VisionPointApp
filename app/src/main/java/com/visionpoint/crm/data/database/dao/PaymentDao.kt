package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.visionpoint.crm.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments WHERE saleId = :saleId ORDER BY date DESC, id DESC")
    fun getBySaleId(saleId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE saleId = :saleId ORDER BY date DESC, id DESC")
    suspend fun getBySaleIdList(saleId: Int): List<Payment>
}
