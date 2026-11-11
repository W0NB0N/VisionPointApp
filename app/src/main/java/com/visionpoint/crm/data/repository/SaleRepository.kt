package com.visionpoint.crm.data.repository

import androidx.room.withTransaction
import com.visionpoint.crm.data.database.VisionPointDatabase
import com.visionpoint.crm.data.database.dao.CashbookDao
import com.visionpoint.crm.data.database.dao.PaymentDao
import com.visionpoint.crm.data.database.dao.SaleDao
import com.visionpoint.crm.data.database.dao.SaleItemDao
import com.visionpoint.crm.data.model.Cashbook
import com.visionpoint.crm.data.model.Payment
import com.visionpoint.crm.data.model.Sale
import com.visionpoint.crm.data.model.SaleItem
import com.visionpoint.crm.data.model.SaleWithCustomer
import com.visionpoint.crm.data.model.SaleWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class SaleRepository @Inject constructor(
    private val database: VisionPointDatabase,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val paymentDao: PaymentDao,
    private val cashbookDao: CashbookDao
) {
    fun getAllSales(): Flow<List<SaleWithCustomer>> = saleDao.getAllWithCustomer()

    fun getFilteredSales(
        status: String? = null,
        customerId: Int? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Flow<List<SaleWithCustomer>> = saleDao.getFilteredWithCustomer(status, customerId, startDate, endDate)

    fun getSaleById(id: Int): Flow<SaleWithDetails?> = saleDao.getById(id)

    suspend fun getSaleByIdOnce(id: Int): SaleWithDetails? = saleDao.getByIdOnce(id)

    fun getSalesByCustomer(customerId: Int): Flow<List<SaleWithDetails>> = saleDao.getByCustomerId(customerId)

    suspend fun createSale(
        sale: Sale,
        items: List<SaleItem>,
        payments: List<Payment> = emptyList()
    ): Long = database.withTransaction {
        // Calculate received amount from initial payments
        val totalReceived = payments.sumOf { it.amount }
        val calculatedDue = max(0.0, sale.netAmount - totalReceived)

        val preparedSale = sale.copy(
            receivedAmount = totalReceived,
            dueAmount = calculatedDue,
            status = sale.status
        )

        val saleId = saleDao.insert(preparedSale).toInt()

        val preparedItems = items.map { it.copy(saleId = saleId) }
        saleItemDao.insertAll(preparedItems)

        for (payment in payments) {
            val paymentId = paymentDao.insert(payment.copy(saleId = saleId)).toInt()
            cashbookDao.insert(
                Cashbook(
                    date = payment.date,
                    type = "Credit",
                    amount = payment.amount,
                    method = payment.method,
                    note = "Payment for Bill #$saleId",
                    paymentId = paymentId
                )
            )
        }

        saleId.toLong()
    }

    suspend fun updateSale(
        sale: Sale,
        items: List<SaleItem>
    ) = database.withTransaction {
        val existingSale = saleDao.getSaleEntityById(sale.id)
            ?: throw IllegalArgumentException("Sale not found: ${sale.id}")

        val recalculatedDue = max(0.0, sale.netAmount - existingSale.receivedAmount)

        val updatedSale = sale.copy(
            receivedAmount = existingSale.receivedAmount,
            dueAmount = recalculatedDue,
            status = sale.status
        )

        saleDao.update(updatedSale)
        saleItemDao.deleteBySaleId(sale.id)
        val preparedItems = items.map { it.copy(saleId = sale.id) }
        saleItemDao.insertAll(preparedItems)
    }

    suspend fun addPayment(
        saleId: Int,
        payment: Payment
    ) = database.withTransaction {
        val sale = saleDao.getSaleEntityById(saleId)
            ?: throw IllegalArgumentException("Sale not found: $saleId")

        val paymentId = paymentDao.insert(payment.copy(saleId = saleId)).toInt()

        val newReceived = sale.receivedAmount + payment.amount
        val newDue = max(0.0, sale.netAmount - newReceived)

        saleDao.update(
            sale.copy(
                receivedAmount = newReceived,
                dueAmount = newDue,
                status = sale.status
            )
        )

        cashbookDao.insert(
            Cashbook(
                date = payment.date,
                type = "Credit",
                amount = payment.amount,
                method = payment.method,
                note = "Payment for Bill #$saleId",
                paymentId = paymentId
            )
        )
    }

    suspend fun updateSaleStatus(saleId: Int, status: String) {
        val sale = saleDao.getSaleEntityById(saleId) ?: return
        saleDao.update(sale.copy(status = status))
    }

    // Dashboard Statistics
    suspend fun getTotalSalesForDate(date: LocalDate): Double = saleDao.getTotalSalesForDate(date)

    suspend fun getSalesCountForDate(date: LocalDate): Int = saleDao.getSalesCountForDate(date)

    suspend fun getTotalSalesForRange(start: LocalDate, end: LocalDate): Double =
        saleDao.getTotalSalesForRange(start, end)

    suspend fun getSalesCountForRange(start: LocalDate, end: LocalDate): Int =
        saleDao.getSalesCountForRange(start, end)

    suspend fun getTotalDue(): Double = saleDao.getTotalDue()

    suspend fun getRecentSales(): List<SaleWithCustomer> = saleDao.getRecentSales()

    suspend fun getRecallsForDate(date: LocalDate): List<SaleWithCustomer> =
        saleDao.getSalesWithRecallDate(date)

    suspend fun getSalesWithPendingDues(): List<SaleWithCustomer> =
        saleDao.getSalesWithPendingDues()
}
