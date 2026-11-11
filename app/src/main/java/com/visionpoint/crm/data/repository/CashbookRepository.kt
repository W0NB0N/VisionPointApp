package com.visionpoint.crm.data.repository

import com.visionpoint.crm.data.database.dao.CashbookDao
import com.visionpoint.crm.data.model.Cashbook
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashbookRepository @Inject constructor(
    private val cashbookDao: CashbookDao
) {
    fun getAllEntries(): Flow<List<Cashbook>> = cashbookDao.getAll()

    fun getFilteredEntries(
        type: String? = null,
        method: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Flow<List<Cashbook>> = cashbookDao.getFiltered(type, method, startDate, endDate)

    suspend fun insertEntry(entry: Cashbook): Long = cashbookDao.insert(entry)

    suspend fun updateEntry(entry: Cashbook) = cashbookDao.update(entry)

    suspend fun deleteEntry(entry: Cashbook) = cashbookDao.delete(entry)

    fun getTotalCredit(): Flow<Double> = cashbookDao.getTotalCredit()

    fun getTotalDebit(): Flow<Double> = cashbookDao.getTotalDebit()
}
