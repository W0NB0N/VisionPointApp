package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.visionpoint.crm.data.model.Cashbook
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CashbookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Cashbook): Long

    @Update
    suspend fun update(entry: Cashbook)

    @Delete
    suspend fun delete(entry: Cashbook)

    @Query("SELECT * FROM cashbook ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<Cashbook>>

    @Query("""
        SELECT * FROM cashbook
        WHERE (:type IS NULL OR :type = 'All' OR type = :type)
          AND (:method IS NULL OR :method = 'All' OR method = :method)
          AND (:startDate IS NULL OR date >= :startDate)
          AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC, id DESC
    """)
    fun getFiltered(
        type: String?,
        method: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Flow<List<Cashbook>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cashbook WHERE type = 'Credit'")
    fun getTotalCredit(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cashbook WHERE type = 'Debit'")
    fun getTotalDebit(): Flow<Double>

    @Query("DELETE FROM cashbook WHERE paymentId = :paymentId")
    suspend fun deleteByPaymentId(paymentId: Int)
}
