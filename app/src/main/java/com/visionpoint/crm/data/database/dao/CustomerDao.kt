package com.visionpoint.crm.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.CustomerWithPrescriptions
import com.visionpoint.crm.data.model.CustomerWithSales
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllList(): List<Customer>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name COLLATE NOCASE ASC")
    fun search(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getById(id: Int): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getByIdOnce(id: Int): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): Customer?

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerWithPrescriptions(id: Int): Flow<CustomerWithPrescriptions?>

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerWithSales(id: Int): Flow<CustomerWithSales?>
}
