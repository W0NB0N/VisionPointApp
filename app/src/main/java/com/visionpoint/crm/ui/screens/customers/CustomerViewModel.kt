package com.visionpoint.crm.ui.screens.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.CustomerWithPrescriptions
import com.visionpoint.crm.data.model.Prescription
import com.visionpoint.crm.data.model.SaleWithDetails
import com.visionpoint.crm.data.repository.CustomerRepository
import com.visionpoint.crm.data.repository.PrescriptionRepository
import com.visionpoint.crm.data.repository.SaleRepository
import com.visionpoint.crm.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val saleRepository: SaleRepository,
    private val settingRepository: SettingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- Customer List State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val customers: StateFlow<List<Customer>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                customerRepository.getAllCustomers()
            } else {
                customerRepository.searchCustomers(query.trim())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // --- Add / Edit Customer State ---
    var customerIdToEdit: Int? = null
        private set

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _gender = MutableStateFlow<String?>("Male")
    val gender: StateFlow<String?> = _gender.asStateFlow()

    private val _dob = MutableStateFlow<LocalDate?>(null)
    val dob: StateFlow<LocalDate?> = _dob.asStateFlow()

    private val _age = MutableStateFlow<Int?>(null)
    val age: StateFlow<Int?> = _age.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _generalError = MutableStateFlow<String?>(null)
    val generalError: StateFlow<String?> = _generalError.asStateFlow()

    fun initEditCustomer(customerId: Int?) {
        customerIdToEdit = customerId
        if (customerId != null) {
            viewModelScope.launch {
                val customer = customerRepository.getCustomerByIdOnce(customerId)
                if (customer != null) {
                    _name.value = customer.name
                    _phone.value = customer.phone
                    _gender.value = customer.gender ?: "Male"
                    _dob.value = customer.dob
                    _age.value = customer.age
                    _address.value = customer.address ?: ""
                    _notes.value = customer.notes ?: ""
                }
            }
        } else {
            // Reset form for create
            _name.value = ""
            _phone.value = ""
            _gender.value = "Male"
            _dob.value = null
            _age.value = null
            _address.value = ""
            _notes.value = ""
            _nameError.value = null
            _phoneError.value = null
            _generalError.value = null
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
        if (value.isNotBlank()) _nameError.value = null
    }

    fun onPhoneChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '+' }
        _phone.value = filtered
        if (filtered.isNotBlank()) _phoneError.value = null
    }

    fun onGenderChange(value: String?) {
        _gender.value = value
    }

    fun onDobChange(selectedDob: LocalDate?) {
        _dob.value = selectedDob
        if (selectedDob != null) {
            val calculatedAge = Period.between(selectedDob, LocalDate.now()).years
            _age.value = calculatedAge
        }
    }

    fun onAgeChange(ageValue: Int?) {
        _age.value = ageValue
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun onAddressChange(value: String) {
        _address.value = value
    }

    fun saveCustomer(onSuccess: (savedId: Int) -> Unit) {
        var isValid = true

        if (_name.value.trim().isBlank()) {
            _nameError.value = "Customer name is required"
            isValid = false
        }

        val phoneTrimmed = _phone.value.trim()
        if (phoneTrimmed.isBlank()) {
            _phoneError.value = "Phone number is required"
            isValid = false
        } else if (phoneTrimmed.length < 10) {
            _phoneError.value = "Phone number must be at least 10 digits"
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _isSaving.value = true
            _generalError.value = null
            try {
                val customer = Customer(
                    id = customerIdToEdit ?: 0,
                    name = _name.value.trim(),
                    phone = phoneTrimmed,
                    gender = _gender.value,
                    dob = _dob.value,
                    age = _age.value,
                    address = _address.value.trim().ifBlank { null },
                    notes = _notes.value.trim().ifBlank { null }
                )

                if (customerIdToEdit == null) {
                    val newId = customerRepository.insertCustomer(customer)
                    _isSaving.value = false
                    onSuccess(newId.toInt())
                } else {
                    customerRepository.updateCustomer(customer)
                    _isSaving.value = false
                    onSuccess(customerIdToEdit!!)
                }
            } catch (e: IllegalArgumentException) {
                _phoneError.value = e.message ?: "Phone number already exists"
                _isSaving.value = false
            } catch (e: Exception) {
                _generalError.value = e.message ?: "An error occurred while saving"
                _isSaving.value = false
            }
        }
    }

    // --- Customer Detail State ---
    private val _selectedCustomerId = MutableStateFlow<Int?>(null)

    val selectedCustomer: StateFlow<CustomerWithPrescriptions?> = _selectedCustomerId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else customerRepository.getCustomerWithPrescriptions(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val selectedCustomerSales: StateFlow<List<SaleWithDetails>> = _selectedCustomerId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else saleRepository.getSalesByCustomer(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadCustomerDetail(customerId: Int) {
        if (_selectedCustomerId.value != customerId) {
            _selectedCustomerId.value = customerId
        }
    }

    fun deleteCustomer(customer: Customer, onDeleted: () -> Unit) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customer)
            onDeleted()
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch {
            prescriptionRepository.deletePrescription(prescription)
        }
    }

    suspend fun getShopName(): String {
        return settingRepository.getValue(SettingRepository.KEY_SHOP_NAME, "Vision Point")
    }

    suspend fun getShopDetails(): com.visionpoint.crm.util.ShopDetails {
        val name = settingRepository.getValue(SettingRepository.KEY_SHOP_NAME, "Vision Point")
        val addr = settingRepository.getValue(SettingRepository.KEY_SHOP_ADDRESS, "")
        val phone = settingRepository.getValue(SettingRepository.KEY_SHOP_PHONE, "")
        return com.visionpoint.crm.util.ShopDetails(name = name, address = addr, phone = phone)
    }

    suspend fun getEyeFields(): List<String> {
        return settingRepository.getEyeFields()
    }
}
