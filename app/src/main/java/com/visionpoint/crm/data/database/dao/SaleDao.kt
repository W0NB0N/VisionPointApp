package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.visionpoint.crm.data.model.Sale
import com.visionpoint.crm.data.model.SaleWithCustomer
import com.visionpoint.crm.data.model.SaleWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: Sale): Long

    @Update
    suspend fun update(sale: Sale)

    @Delete
    suspend fun delete(sale: Sale)

    @Transaction
    @Query("SELECT * FROM sales ORDER BY saleDate DESC, id DESC")
    fun getAllWithCustomer(): Flow<List<SaleWithCustomer>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    fun getById(id: Int): Flow<SaleWithDetails?>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getByIdOnce(id: Int): SaleWithDetails?

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleEntityById(id: Int): Sale?

    @Transaction
    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY saleDate DESC, id DESC")
    fun getByCustomerId(customerId: Int): Flow<List<SaleWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM sales
        WHERE (:status IS NULL OR :status = 'All' OR status = :status)
          AND (:customerId IS NULL OR customerId = :customerId)
          AND (:startDate IS NULL OR saleDate >= :startDate)
          AND (:endDate IS NULL OR saleDate <= :endDate)
        ORDER BY saleDate DESC, id DESC
    """)
    fun getFilteredWithCustomer(
        status: String?,
        customerId: Int?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<List<SaleWithCustomer>>

    // Dashboard Statistics
    @Query("SELECT COALESCE(SUM(netAmount), 0.0) FROM sales WHERE saleDate = :date")
    suspend fun getTotalSalesForDate(date: LocalDate): Double

    @Query("SELECT COUNT(*) FROM sales WHERE saleDate = :date")
    suspend fun getSalesCountForDate(date: LocalDate): Int

    @Query("SELECT COALESCE(SUM(netAmount), 0.0) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate")
    suspend fun getTotalSalesForRange(startDate: LocalDate, endDate: LocalDate): Double

    @Query("SELECT COUNT(*) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate")
    suspend fun getSalesCountForRange(startDate: LocalDate, endDate: LocalDate): Int

    @Query("SELECT COALESCE(SUM(dueAmount), 0.0) FROM sales WHERE dueAmount > 0")
    suspend fun getTotalDue(): Double

    @Transaction
    @Query("SELECT * FROM sales ORDER BY saleDate DESC, id DESC LIMIT 5")
    suspend fun getRecentSales(): List<SaleWithCustomer>

    @Transaction
    @Query("SELECT * FROM sales WHERE recallDate = :date")
    suspend fun getSalesWithRecallDate(date: LocalDate): List<SaleWithCustomer>

    @Transaction
    @Query("SELECT * FROM sales WHERE dueAmount > 0 ORDER BY dueAmount DESC")
    suspend fun getSalesWithPendingDues(): List<SaleWithCustomer>
}
