package com.visionpoint.crm.ui.screens.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionpoint.crm.data.model.Prescription
import com.visionpoint.crm.data.repository.CustomerRepository
import com.visionpoint.crm.data.repository.PrescriptionRepository
import com.visionpoint.crm.data.repository.SettingRepository
import com.visionpoint.crm.util.EyePowerUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val prescriptionRepository: PrescriptionRepository,
    private val customerRepository: CustomerRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private val _prescriptionIdToEdit = MutableStateFlow<Int?>(null)
    val prescriptionIdToEdit: StateFlow<Int?> = _prescriptionIdToEdit.asStateFlow()

    private val _customerId = MutableStateFlow(0)
    val customerId: StateFlow<Int> = _customerId.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    private val _type = MutableStateFlow("Specs")
    val type: StateFlow<String> = _type.asStateFlow()

    private val _doctorName = MutableStateFlow("Arun Bhalla")
    val doctorName: StateFlow<String> = _doctorName.asStateFlow()

    private val _eyeFields = MutableStateFlow(SettingRepository.DEFAULT_EYE_FIELDS)
    val eyeFields: StateFlow<List<String>> = _eyeFields.asStateFlow()

    private val _rightEye = MutableStateFlow<Map<String, String>>(emptyMap())
    val rightEye: StateFlow<Map<String, String>> = _rightEye.asStateFlow()

    private val _leftEye = MutableStateFlow<Map<String, String>>(emptyMap())
    val leftEye: StateFlow<Map<String, String>> = _leftEye.asStateFlow()

    private val _pd = MutableStateFlow("")
    val pd: StateFlow<String> = _pd.asStateFlow()

    private val _psm = MutableStateFlow("")
    val psm: StateFlow<String> = _psm.asStateFlow()

    private val _fh = MutableStateFlow("")
    val fh: StateFlow<String> = _fh.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _isPreviousLens = MutableStateFlow(false)
    val isPreviousLens: StateFlow<Boolean> = _isPreviousLens.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun init(customerId: Int, prescriptionId: Int? = null) {
        _customerId.value = customerId
        viewModelScope.launch {
            val customer = customerRepository.getCustomerByIdOnce(customerId)
            _customerName.value = customer?.name ?: ""

            val dynamicFields = settingRepository.getEyeFields()
            _eyeFields.value = dynamicFields

            if (prescriptionId != null && prescriptionId > 0) {
                val existingRx = prescriptionRepository.getPrescriptionByIdOnce(prescriptionId)
                if (existingRx != null) {
                    _prescriptionIdToEdit.value = prescriptionId
                    _date.value = existingRx.date
                    _type.value = existingRx.type ?: "Specs"
                    _doctorName.value = existingRx.doctorName ?: "Arun Bhalla"

                    val decodedRight = EyePowerUtils.decodeEyeData(existingRx.rightEye)
                    val decodedLeft = EyePowerUtils.decodeEyeData(existingRx.leftEye)

                    // Merge with dynamic fields ensuring all keys exist
                    _rightEye.value = dynamicFields.associateWith { decodedRight[it] ?: "" }
                    _leftEye.value = dynamicFields.associateWith { decodedLeft[it] ?: "" }

                    _pd.value = existingRx.pd ?: ""
                    _psm.value = existingRx.psm ?: ""
                    _fh.value = existingRx.fh ?: ""
                    _notes.value = existingRx.notes ?: ""
                    _isPreviousLens.value = existingRx.isPreviousLens
                    return@launch
                }
            }

            // Fresh creation mode
            _prescriptionIdToEdit.value = null
            _rightEye.value = dynamicFields.associateWith { "" }
            _leftEye.value = dynamicFields.associateWith { "" }
            _date.value = LocalDate.now()
            _type.value = "Specs"
            _doctorName.value = "Arun Bhalla"
            _pd.value = ""
            _psm.value = ""
            _fh.value = ""
            _notes.value = ""
            _isPreviousLens.value = false
        }
    }

    fun onDateChange(newDate: LocalDate) {
        _date.value = newDate
    }

    fun onTypeChange(newType: String) {
        _type.value = newType
    }

    fun onDoctorNameChange(newName: String) {
        _doctorName.value = newName
    }

    fun onRightEyeFieldChange(field: String, value: String) {
        val updated = _rightEye.value.toMutableMap()
        updated[field] = value
        _rightEye.value = updated
    }

    fun onLeftEyeFieldChange(field: String, value: String) {
        val updated = _leftEye.value.toMutableMap()
        updated[field] = value
        _leftEye.value = updated
    }

    fun copyRightToLeft() {
        _leftEye.value = _rightEye.value.toMap()
    }

    fun onPdChange(value: String) {
        _pd.value = value
    }

    fun onPsmChange(value: String) {
        _psm.value = value
    }

    fun onFhChange(value: String) {
        _fh.value = value
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun onIsPreviousLensChange(value: Boolean) {
        _isPreviousLens.value = value
    }

    fun savePrescription(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val encodedRightEye = EyePowerUtils.encodeEyeData(_rightEye.value)
                val encodedLeftEye = EyePowerUtils.encodeEyeData(_leftEye.value)

                val editId = _prescriptionIdToEdit.value
                val prescription = Prescription(
                    id = editId ?: 0,
                    customerId = _customerId.value,
                    date = _date.value,
                    type = _type.value,
                    doctorName = _doctorName.value.trim().ifBlank { "Arun Bhalla" },
                    rightEye = encodedRightEye.ifBlank { null },
                    leftEye = encodedLeftEye.ifBlank { null },
                    pd = _pd.value.trim().ifBlank { null },
                    psm = _psm.value.trim().ifBlank { null },
                    fh = _fh.value.trim().ifBlank { null },
                    notes = _notes.value.trim().ifBlank { null },
                    isPreviousLens = _isPreviousLens.value
                )

                if (editId != null && editId > 0) {
                    prescriptionRepository.updatePrescription(prescription)
                } else {
                    prescriptionRepository.insertPrescription(prescription)
                }
                _isSaving.value = false
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
            }
        }
    }
}
