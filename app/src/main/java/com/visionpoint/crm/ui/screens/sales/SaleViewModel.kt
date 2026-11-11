package com.visionpoint.crm.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.Payment
import com.visionpoint.crm.data.model.Prescription
import com.visionpoint.crm.data.model.Sale
import com.visionpoint.crm.data.model.SaleItem
import com.visionpoint.crm.data.model.SaleWithCustomer
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.max

data class SaleItemFormState(
    val category: String = "Product", // "Product" or "Service"
    val itemType: String = "Frames",
    val itemDetail: String = "",
    val name: String = "",
    val note: String = "",
    val priceInput: String = "",
    val quantityInput: String = "1"
) {
    val price: Double get() = priceInput.toDoubleOrNull() ?: 0.0
    val quantity: Int get() = quantityInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val amount: Double get() = price * quantity
}

data class PaymentFormState(
    val amountInput: String = "",
    val method: String = "Cash", // "Cash", "Card", "UPI"
    val date: LocalDate = LocalDate.now()
) {
    val amount: Double get() = amountInput.toDoubleOrNull() ?: 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SaleViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    // --- Sales List State ---
    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _startDateFilter = MutableStateFlow<LocalDate?>(null)
    val startDateFilter: StateFlow<LocalDate?> = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow<LocalDate?>(null)
    val endDateFilter: StateFlow<LocalDate?> = _endDateFilter.asStateFlow()

    val salesList: StateFlow<List<SaleWithCustomer>> = combine(
        _statusFilter,
        _searchQuery,
        _startDateFilter,
        _endDateFilter
    ) { status, query, start, end ->
        FilterParams(status, query, start, end)
    }.flatMapLatest { params ->
        saleRepository.getFilteredSales(
            status = if (params.status == "All") null else params.status,
            customerId = null,
            startDate = params.start,
            endDate = params.end
        )
    }.combine(_searchQuery) { sales, query ->
        if (query.isBlank()) sales
        else sales.filter {
            it.customer.name.contains(query.trim(), ignoreCase = true) ||
            it.customer.phone.contains(query.trim()) ||
            it.sale.id.toString().contains(query.trim())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setStatusFilter(status: String) { _statusFilter.value = status }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setDateRange(start: LocalDate?, end: LocalDate?) {
        _startDateFilter.value = start
        _endDateFilter.value = end
    }

    // --- Create / Edit Sale State ---
    var saleIdToEdit: Int? = null
        private set

    val availableCustomers: StateFlow<List<Customer>> = customerRepository.getAllCustomers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _customerPrescriptions = MutableStateFlow<List<Prescription>>(emptyList())
    val customerPrescriptions: StateFlow<List<Prescription>> = _customerPrescriptions.asStateFlow()

    private val _selectedPrescriptionId = MutableStateFlow<Int?>(null)
    val selectedPrescriptionId: StateFlow<Int?> = _selectedPrescriptionId.asStateFlow()

    private val _saleDate = MutableStateFlow(LocalDate.now())
    val saleDate: StateFlow<LocalDate> = _saleDate.asStateFlow()

    private val _recallDate = MutableStateFlow<LocalDate?>(null)
    val recallDate: StateFlow<LocalDate?> = _recallDate.asStateFlow()

    private val _items = MutableStateFlow<List<SaleItemFormState>>(listOf(SaleItemFormState()))
    val items: StateFlow<List<SaleItemFormState>> = _items.asStateFlow()

    private val _discountType = MutableStateFlow("percentage") // "percentage" or "amount"
    val discountType: StateFlow<String> = _discountType.asStateFlow()

    private val _discountPercentInput = MutableStateFlow("")
    val discountPercentInput: StateFlow<String> = _discountPercentInput.asStateFlow()

    private val _discountAmountInput = MutableStateFlow("")
    val discountAmountInput: StateFlow<String> = _discountAmountInput.asStateFlow()

    private val _payments = MutableStateFlow<List<PaymentFormState>>(emptyList())
    val payments: StateFlow<List<PaymentFormState>> = _payments.asStateFlow()

    private val _saleStatus = MutableStateFlow("Pending")
    val saleStatus: StateFlow<String> = _saleStatus.asStateFlow()

    private val _availableItemTypes = MutableStateFlow(SettingRepository.DEFAULT_ITEM_TYPES)
    val availableItemTypes: StateFlow<List<String>> = _availableItemTypes.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    fun initSaleForm(saleId: Int? = null, preselectedCustomerId: Int? = null) {
        _formError.value = null

        viewModelScope.launch {
            _availableItemTypes.value = settingRepository.getItemTypes()

            if (saleId != null) {
                // Edit Mode: ALWAYS reload the sale from DB when editing a sale
                saleIdToEdit = saleId
                val saleWithDetails = saleRepository.getSaleByIdOnce(saleId)
                if (saleWithDetails != null) {
                    _selectedCustomer.value = saleWithDetails.customer
                    _selectedPrescriptionId.value = saleWithDetails.sale.prescriptionId
                    _saleDate.value = saleWithDetails.sale.saleDate
                    _recallDate.value = saleWithDetails.sale.recallDate
                    _discountType.value = saleWithDetails.sale.discountType
                    if (saleWithDetails.sale.discountType == "amount") {
                        _discountAmountInput.value = if (saleWithDetails.sale.discountAmount > 0) saleWithDetails.sale.discountAmount.toString() else ""
                        _discountPercentInput.value = ""
                    } else {
                        _discountPercentInput.value = if (saleWithDetails.sale.discountPercent > 0) saleWithDetails.sale.discountPercent.toString() else ""
                        _discountAmountInput.value = ""
                    }
                    _saleStatus.value = saleWithDetails.sale.status
                    _items.value = saleWithDetails.items.map {
                        SaleItemFormState(
                            category = it.category,
                            itemType = it.itemType ?: "Frames",
                            itemDetail = it.itemDetail ?: "",
                            name = it.name,
                            note = it.note ?: "",
                            priceInput = if (it.price > 0) it.price.toString() else "",
                            quantityInput = it.quantity.toString()
                        )
                    }
                    _payments.value = emptyList()
                    _customerPrescriptions.value = prescriptionRepository.getPrescriptionsByCustomerList(saleWithDetails.customer.id)
                }
            } else if (preselectedCustomerId != null) {
                // Create Mode with customer preselected
                if (saleIdToEdit != null || _selectedCustomer.value?.id != preselectedCustomerId) {
                    resetSaleForm()
                    val customer = customerRepository.getCustomerByIdOnce(preselectedCustomerId)
                    if (customer != null) {
                        onCustomerSelected(customer)
                    }
                }
            } else {
                // Create Mode (New Sale tab)
                if (saleIdToEdit != null) {
                    // Reset if previously in edit mode
                    resetSaleForm()
                }
            }
        }
    }

    fun resetSaleForm() {
        saleIdToEdit = null
        _selectedCustomer.value = null
        _customerPrescriptions.value = emptyList()
        _selectedPrescriptionId.value = null
        _saleDate.value = LocalDate.now()
        _recallDate.value = null
        _discountType.value = "percentage"
        _discountPercentInput.value = ""
        _discountAmountInput.value = ""
        _saleStatus.value = "Pending"
        _items.value = listOf(SaleItemFormState())
        _payments.value = emptyList()
        _formError.value = null
    }

    fun onCustomerSelected(customer: Customer) {
        _selectedCustomer.value = customer
        _selectedPrescriptionId.value = null
        viewModelScope.launch {
            _customerPrescriptions.value = prescriptionRepository.getPrescriptionsByCustomerList(customer.id)
        }
    }

    fun clearCustomer() {
        _selectedCustomer.value = null
        _selectedPrescriptionId.value = null
        _customerPrescriptions.value = emptyList()
    }

    fun onPrescriptionSelected(prescriptionId: Int?) {
        _selectedPrescriptionId.value = prescriptionId
    }

    fun onSaleDateChange(date: LocalDate) { _saleDate.value = date }
    fun onRecallDateChange(date: LocalDate?) { _recallDate.value = date }

    fun onDiscountTypeChange(type: String) {
        _discountType.value = type
    }

    fun onDiscountInputChange(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } <= 1) {
            _discountPercentInput.value = sanitized
        }
    }

    fun onDiscountAmountInputChange(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } <= 1) {
            _discountAmountInput.value = sanitized
        }
    }

    fun onStatusChange(status: String) { _saleStatus.value = status }

    fun addItem() {
        _items.value = _items.value + SaleItemFormState()
    }

    fun removeItem(index: Int) {
        if (_items.value.size > 1) {
            val updated = _items.value.toMutableList()
            updated.removeAt(index)
            _items.value = updated
        }
    }

    fun updateItem(index: Int, item: SaleItemFormState) {
        if (index in _items.value.indices) {
            val updated = _items.value.toMutableList()
            updated[index] = item
            _items.value = updated
        }
    }

    fun addPayment() {
        val currentSubtotal = _items.value.sumOf { it.amount }
        val effectiveDiscount = if (_discountType.value == "amount") {
            (_discountAmountInput.value.toDoubleOrNull() ?: 0.0).coerceAtMost(currentSubtotal)
        } else {
            val pct = _discountPercentInput.value.toDoubleOrNull() ?: 0.0
            (currentSubtotal * pct.coerceIn(0.0, 100.0) / 100.0)
        }
        val currentNet = max(0.0, currentSubtotal - effectiveDiscount)
        val currentPaid = _payments.value.sumOf { it.amount }
        val remainingDue = max(0.0, currentNet - currentPaid)

        val defaultAmountStr = if (remainingDue > 0) String.format("%.2f", remainingDue).trimEnd('0').trimEnd('.') else ""
        _payments.value = _payments.value + PaymentFormState(amountInput = defaultAmountStr)
    }

    fun removePayment(index: Int) {
        if (index in _payments.value.indices) {
            val updated = _payments.value.toMutableList()
            updated.removeAt(index)
            _payments.value = updated
        }
    }

    fun updatePayment(index: Int, payment: PaymentFormState) {
        if (index in _payments.value.indices) {
            val updated = _payments.value.toMutableList()
            updated[index] = payment
            _payments.value = updated
        }
    }

    fun saveSale(onSuccess: (saleId: Int) -> Unit) {
        val customer = _selectedCustomer.value
        if (customer == null) {
            _formError.value = "Please select a customer"
            return
        }

        val invalidItem = _items.value.find { it.name.trim().isBlank() }
        if (invalidItem != null) {
            _formError.value = "All items must have a name"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _formError.value = null

            val subtotal = _items.value.sumOf { it.amount }
            val currentDiscountType = _discountType.value
            val pctInput = _discountPercentInput.value.toDoubleOrNull() ?: 0.0
            val amtInput = _discountAmountInput.value.toDoubleOrNull() ?: 0.0

            val effectiveDiscountAmount = if (currentDiscountType == "amount") {
                amtInput.coerceAtMost(subtotal)
            } else {
                (subtotal * pctInput.coerceIn(0.0, 100.0) / 100.0)
            }
            val effectiveDiscountPercent = if (currentDiscountType == "percentage") {
                pctInput
            } else {
                if (subtotal > 0) (effectiveDiscountAmount / subtotal) * 100.0 else 0.0
            }

            val netAmount = max(0.0, subtotal - effectiveDiscountAmount)

            try {
                if (saleIdToEdit == null) {
                    // Create New Sale
                    val saleEntity = Sale(
                        customerId = customer.id,
                        prescriptionId = _selectedPrescriptionId.value,
                        saleDate = _saleDate.value,
                        recallDate = _recallDate.value,
                        subtotal = subtotal,
                        discountPercent = if (currentDiscountType == "percentage") pctInput else 0.0,
                        discountType = currentDiscountType,
                        discountAmount = effectiveDiscountAmount,
                        netAmount = netAmount,
                        status = _saleStatus.value
                    )

                    val saleItems = _items.value.map {
                        SaleItem(
                            saleId = 0,
                            category = it.category,
                            itemType = it.itemType,
                            itemDetail = it.itemDetail.ifBlank { null },
                            name = it.name.trim(),
                            note = it.note.ifBlank { null },
                            price = it.price,
                            quantity = it.quantity,
                            amount = it.amount
                        )
                    }

                    val initialPayments = _payments.value.filter { it.amount > 0 }.map {
                        Payment(
                            saleId = 0,
                            amount = it.amount,
                            method = it.method,
                            date = it.date
                        )
                    }

                    val createdId = saleRepository.createSale(
                        sale = saleEntity,
                        items = saleItems,
                        payments = initialPayments
                    )
                    resetSaleForm()
                    _isSaving.value = false
                    onSuccess(createdId.toInt())
                } else {
                    // Update Existing Sale
                    val targetSaleId = saleIdToEdit ?: throw IllegalStateException("No sale ID to edit")
                    val saleEntity = Sale(
                        id = targetSaleId,
                        customerId = customer.id,
                        prescriptionId = _selectedPrescriptionId.value,
                        saleDate = _saleDate.value,
                        recallDate = _recallDate.value,
                        subtotal = subtotal,
                        discountPercent = if (currentDiscountType == "percentage") pctInput else 0.0,
                        discountType = currentDiscountType,
                        discountAmount = effectiveDiscountAmount,
                        netAmount = netAmount,
                        status = _saleStatus.value
                    )

                    val saleItems = _items.value.map {
                        SaleItem(
                            saleId = targetSaleId,
                            category = it.category,
                            itemType = it.itemType,
                            itemDetail = it.itemDetail.ifBlank { null },
                            name = it.name.trim(),
                            note = it.note.ifBlank { null },
                            price = it.price,
                            quantity = it.quantity,
                            amount = it.amount
                        )
                    }

                    saleRepository.updateSale(sale = saleEntity, items = saleItems)
                    resetSaleForm()
                    _isSaving.value = false
                    onSuccess(targetSaleId)
                }
            } catch (e: Exception) {
                _formError.value = e.message ?: "Failed to save sale invoice"
                _isSaving.value = false
            }
        }
    }

    // --- Sale Detail State ---
    private val _selectedDetailSaleId = MutableStateFlow<Int?>(null)

    val selectedSaleDetail: StateFlow<SaleWithDetails?> = _selectedDetailSaleId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else saleRepository.getSaleById(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadSaleDetail(saleId: Int) {
        _selectedDetailSaleId.value = saleId
    }

    fun updateDetailSaleStatus(newStatus: String) {
        val saleId = _selectedDetailSaleId.value ?: return
        viewModelScope.launch {
            saleRepository.updateSaleStatus(saleId, newStatus)
        }
    }

    fun updateSaleStatus(newStatus: String) {
        updateDetailSaleStatus(newStatus)
    }

    fun addPaymentToSale(amount: Double, method: String, date: LocalDate, onSuccess: () -> Unit) {
        val saleId = _selectedDetailSaleId.value ?: return
        viewModelScope.launch {
            try {
                val payment = Payment(
                    saleId = saleId,
                    amount = amount,
                    method = method,
                    date = date
                )
                saleRepository.addPayment(saleId, payment)
                onSuccess()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun addPaymentToSale(payment: Payment, onSuccess: () -> Unit) {
        val saleId = _selectedDetailSaleId.value ?: return
        viewModelScope.launch {
            try {
                saleRepository.addPayment(saleId, payment)
                onSuccess()
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    suspend fun getShopDetails(): com.visionpoint.crm.util.ShopDetails {
        val name = settingRepository.getValue(SettingRepository.KEY_SHOP_NAME, "Vision Point")
        val phone = settingRepository.getValue(SettingRepository.KEY_SHOP_PHONE, "")
        val address = settingRepository.getValue(SettingRepository.KEY_SHOP_ADDRESS, SettingRepository.DEFAULT_SHOP_ADDRESS)
        return com.visionpoint.crm.util.ShopDetails(name = name, phone = phone, address = address)
    }

    suspend fun getStatusReminderMessage(
        customerName: String,
        status: String,
        dueAmount: Double,
        netAmount: Double,
        billId: Int
    ): String {
        val shop = getShopDetails()
        val key = when (status) {
            "Pending" -> SettingRepository.KEY_MSG_STATUS_PENDING
            "Ready" -> SettingRepository.KEY_MSG_STATUS_READY
            "Completed" -> SettingRepository.KEY_MSG_STATUS_COMPLETED
            else -> SettingRepository.KEY_MSG_STATUS_PENDING
        }
        val defaultTemplate = when (status) {
            "Pending" -> SettingRepository.DEFAULT_MSG_STATUS_PENDING
            "Ready" -> SettingRepository.DEFAULT_MSG_STATUS_READY
            "Completed" -> SettingRepository.DEFAULT_MSG_STATUS_COMPLETED
            else -> SettingRepository.DEFAULT_MSG_STATUS_PENDING
        }

        val template = settingRepository.getValue(key, defaultTemplate)
        val formattedBill = String.format("%03d", billId)
        val formattedDue = String.format("%.2f", dueAmount)
        val formattedNet = String.format("%.2f", netAmount)

        return template
            .replace("{name}", customerName)
            .replace("{shop}", shop.name)
            .replace("{bill}", formattedBill)
            .replace("{amount}", formattedDue)
            .replace("{due}", formattedDue)
            .replace("{net}", formattedNet)
    }

    suspend fun getDueReminderMessage(
        customerName: String,
        dueAmount: Double,
        billId: Int
    ): String {
        val shop = getShopDetails()
        val template = settingRepository.getValue(SettingRepository.KEY_MSG_DUE, SettingRepository.DEFAULT_MSG_DUE)
        val formattedBill = String.format("%03d", billId)
        val formattedDue = String.format("%.2f", dueAmount)

        return template
            .replace("{name}", customerName)
            .replace("{shop}", shop.name)
            .replace("{bill}", formattedBill)
            .replace("{amount}", formattedDue)
            .replace("{due}", formattedDue)
    }

    suspend fun getStatusWhatsAppMessage(status: String, customerName: String, billNumber: Int, dueAmount: Double): String {
        return getStatusReminderMessage(
            customerName = customerName,
            status = status,
            dueAmount = dueAmount,
            netAmount = 0.0,
            billId = billNumber
        )
    }
}

private data class FilterParams(
    val status: String,
    val query: String,
    val start: LocalDate?,
    val end: LocalDate?
)
