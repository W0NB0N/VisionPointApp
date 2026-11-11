package com.visionpoint.crm.ui.screens.cashbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionpoint.crm.data.model.Cashbook
import com.visionpoint.crm.data.repository.CashbookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CashbookViewModel @Inject constructor(
    private val cashbookRepository: CashbookRepository
) : ViewModel() {

    // --- List Filters ---
    private val _typeFilter = MutableStateFlow("All")
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _methodFilter = MutableStateFlow("All")
    val methodFilter: StateFlow<String> = _methodFilter.asStateFlow()

    private val _startDateFilter = MutableStateFlow<LocalDate?>(null)
    val startDateFilter: StateFlow<LocalDate?> = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow<LocalDate?>(null)
    val endDateFilter: StateFlow<LocalDate?> = _endDateFilter.asStateFlow()

    val entries: StateFlow<List<Cashbook>> = combine(
        _typeFilter,
        _methodFilter,
        _startDateFilter,
        _endDateFilter
    ) { type, method, start, end ->
        CashbookFilterParams(type, method, start, end)
    }.flatMapLatest { params ->
        cashbookRepository.getFilteredEntries(
            type = if (params.type == "All") null else params.type,
            method = if (params.method == "All") null else params.method,
            startDate = params.start,
            endDate = params.end
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalCredit: StateFlow<Double> = cashbookRepository.getTotalCredit()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalDebit: StateFlow<Double> = cashbookRepository.getTotalDebit()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val netEarnings: StateFlow<Double> = combine(totalCredit, totalDebit) { credit, debit ->
        credit - debit
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun setTypeFilter(type: String) { _typeFilter.value = type }
    fun setMethodFilter(method: String) { _methodFilter.value = method }

    // --- Add Manual Entry State ---
    private val _entryDate = MutableStateFlow(LocalDate.now())
    val entryDate: StateFlow<LocalDate> = _entryDate.asStateFlow()

    private val _entryType = MutableStateFlow("Credit") // "Credit" or "Debit"
    val entryType: StateFlow<String> = _entryType.asStateFlow()

    private val _entryAmount = MutableStateFlow("")
    val entryAmount: StateFlow<String> = _entryAmount.asStateFlow()

    private val _entryMethod = MutableStateFlow("Cash") // "Cash", "Card", "UPI"
    val entryMethod: StateFlow<String> = _entryMethod.asStateFlow()

    private val _entryNote = MutableStateFlow("")
    val entryNote: StateFlow<String> = _entryNote.asStateFlow()

    private val _entryAmountError = MutableStateFlow<String?>(null)
    val entryAmountError: StateFlow<String?> = _entryAmountError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun initAddEntry() {
        _entryDate.value = LocalDate.now()
        _entryType.value = "Credit"
        _entryAmount.value = ""
        _entryMethod.value = "Cash"
        _entryNote.value = ""
        _entryAmountError.value = null
        _isSaving.value = false
    }

    fun onEntryDateChange(date: LocalDate) { _entryDate.value = date }
    fun onEntryTypeChange(type: String) { _entryType.value = type }
    fun onEntryAmountChange(amount: String) {
        val sanitized = amount.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } <= 1) {
            _entryAmount.value = sanitized
            _entryAmountError.value = null
        }
    }
    fun onEntryMethodChange(method: String) { _entryMethod.value = method }
    fun onEntryNoteChange(note: String) { _entryNote.value = note }

    fun saveEntry(onSuccess: () -> Unit) {
        val parsed = _entryAmount.value.toDoubleOrNull()
        if (parsed == null || parsed <= 0) {
            _entryAmountError.value = "Enter a valid amount"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            val cashbookEntry = Cashbook(
                date = _entryDate.value,
                type = _entryType.value,
                amount = parsed,
                method = _entryMethod.value,
                note = _entryNote.value.trim().ifBlank { null }
            )
            cashbookRepository.insertEntry(cashbookEntry)
            _isSaving.value = false
            onSuccess()
        }
    }

    fun deleteEntry(entry: Cashbook) {
        viewModelScope.launch {
            cashbookRepository.deleteEntry(entry)
        }
    }
}

private data class CashbookFilterParams(
    val type: String,
    val method: String,
    val start: LocalDate?,
    val end: LocalDate?
)
