package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.visionpoint.crm.data.model.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SaleItem>)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteBySaleId(saleId: Int)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getBySaleId(saleId: Int): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getBySaleIdList(saleId: Int): List<SaleItem>
}
