package com.visionpoint.crm.data.repository

import com.visionpoint.crm.data.database.dao.CustomerDao
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.CustomerWithPrescriptions
import com.visionpoint.crm.data.model.CustomerWithSales
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAll()

    suspend fun getAllCustomersList(): List<Customer> = customerDao.getAllList()

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.search(query)

    fun getCustomerById(id: Int): Flow<Customer?> = customerDao.getById(id)

    suspend fun getCustomerByIdOnce(id: Int): Customer? = customerDao.getByIdOnce(id)

    fun getCustomerWithPrescriptions(id: Int): Flow<CustomerWithPrescriptions?> =
        customerDao.getCustomerWithPrescriptions(id).map { customerWithRx ->
            customerWithRx?.copy(
                prescriptions = customerWithRx.prescriptions.sortedWith(
                    compareByDescending<com.visionpoint.crm.data.model.Prescription> { it.date }.thenByDescending { it.id }
                )
            )
        }

    fun getCustomerWithSales(id: Int): Flow<CustomerWithSales?> =
        customerDao.getCustomerWithSales(id)

    suspend fun insertCustomer(customer: Customer): Long {
        val existing = customerDao.getByPhone(customer.phone.trim())
        if (existing != null) {
            throw IllegalArgumentException("Customer with phone ${customer.phone} already exists")
        }
        return customerDao.insert(customer.copy(phone = customer.phone.trim()))
    }

    suspend fun updateCustomer(customer: Customer) {
        val existing = customerDao.getByPhone(customer.phone.trim())
        if (existing != null && existing.id != customer.id) {
            throw IllegalArgumentException("Customer with phone ${customer.phone} already exists")
        }
        customerDao.update(customer.copy(phone = customer.phone.trim()))
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.delete(customer)
    }

    suspend fun getBirthdaysToday(today: LocalDate = LocalDate.now()): List<Customer> {
        val all = customerDao.getAllList()
        return all.filter { customer ->
            customer.dob?.let { dob ->
                dob.monthValue == today.monthValue && dob.dayOfMonth == today.dayOfMonth
            } ?: false
        }
    }
}
