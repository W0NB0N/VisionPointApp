package com.visionpoint.crm.data.repository

import com.visionpoint.crm.data.database.dao.PrescriptionDao
import com.visionpoint.crm.data.model.Prescription
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrescriptionRepository @Inject constructor(
    private val prescriptionDao: PrescriptionDao
) {
    fun getPrescriptionsByCustomer(customerId: Int): Flow<List<Prescription>> =
        prescriptionDao.getByCustomerId(customerId)

    suspend fun getPrescriptionsByCustomerList(customerId: Int): List<Prescription> =
        prescriptionDao.getByCustomerIdList(customerId)

    fun getPrescriptionById(id: Int): Flow<Prescription?> =
        prescriptionDao.getById(id)

    suspend fun getPrescriptionByIdOnce(id: Int): Prescription? =
        prescriptionDao.getByIdOnce(id)

    suspend fun insertPrescription(prescription: Prescription): Long =
        prescriptionDao.insert(prescription)

    suspend fun updatePrescription(prescription: Prescription) =
        prescriptionDao.update(prescription)

    suspend fun deletePrescription(prescription: Prescription) =
        prescriptionDao.delete(prescription)
}
