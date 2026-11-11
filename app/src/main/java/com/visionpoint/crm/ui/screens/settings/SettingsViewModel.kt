package com.visionpoint.crm.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionpoint.crm.data.repository.CustomerRepository
import com.visionpoint.crm.data.repository.SettingRepository
import com.visionpoint.crm.util.ContactsHelper
import com.visionpoint.crm.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingRepository: SettingRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // --- Theme Settings ---
    val themeMode: StateFlow<String> = settingRepository.getSettingFlow(
        SettingRepository.KEY_THEME_MODE,
        "system"
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "system"
    )

    val dynamicColor: StateFlow<Boolean> = settingRepository.getSettingFlow(
        SettingRepository.KEY_DYNAMIC_COLOR,
        "false"
    ).map { it.toBoolean() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingRepository.setValue(SettingRepository.KEY_THEME_MODE, mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingRepository.setValue(SettingRepository.KEY_DYNAMIC_COLOR, enabled.toString())
        }
    }

    // --- Shop Details ---
    private val _shopName = MutableStateFlow("Vision Point")
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _shopAddress = MutableStateFlow("")
    val shopAddress: StateFlow<String> = _shopAddress.asStateFlow()

    private val _shopPhone = MutableStateFlow("")
    val shopPhone: StateFlow<String> = _shopPhone.asStateFlow()

    // --- Item Types & Eye Fields ---
    private val _itemTypes = MutableStateFlow(SettingRepository.DEFAULT_ITEM_TYPES)
    val itemTypes: StateFlow<List<String>> = _itemTypes.asStateFlow()

    private val _eyeFields = MutableStateFlow(SettingRepository.DEFAULT_EYE_FIELDS)
    val eyeFields: StateFlow<List<String>> = _eyeFields.asStateFlow()

    // --- WhatsApp Message Templates ---
    private val _msgStatusPending = MutableStateFlow(SettingRepository.DEFAULT_MSG_STATUS_PENDING)
    val msgStatusPending: StateFlow<String> = _msgStatusPending.asStateFlow()

    private val _msgStatusReady = MutableStateFlow(SettingRepository.DEFAULT_MSG_STATUS_READY)
    val msgStatusReady: StateFlow<String> = _msgStatusReady.asStateFlow()

    private val _msgStatusCompleted = MutableStateFlow(SettingRepository.DEFAULT_MSG_STATUS_COMPLETED)
    val msgStatusCompleted: StateFlow<String> = _msgStatusCompleted.asStateFlow()

    private val _msgBirthday = MutableStateFlow(SettingRepository.DEFAULT_MSG_BIRTHDAY)
    val msgBirthday: StateFlow<String> = _msgBirthday.asStateFlow()

    private val _msgRecall = MutableStateFlow(SettingRepository.DEFAULT_MSG_RECALL)
    val msgRecall: StateFlow<String> = _msgRecall.asStateFlow()

    private val _msgDue = MutableStateFlow(SettingRepository.DEFAULT_MSG_DUE)
    val msgDue: StateFlow<String> = _msgDue.asStateFlow()

    // --- Notification Settings ---
    private val _notifEnabled = MutableStateFlow(true)
    val notifEnabled: StateFlow<Boolean> = _notifEnabled.asStateFlow()

    private val _notifRecalls = MutableStateFlow(true)
    val notifRecalls: StateFlow<Boolean> = _notifRecalls.asStateFlow()

    private val _notifBirthdays = MutableStateFlow(true)
    val notifBirthdays: StateFlow<Boolean> = _notifBirthdays.asStateFlow()

    private val _notifDues = MutableStateFlow(true)
    val notifDues: StateFlow<Boolean> = _notifDues.asStateFlow()

    private val _notifTime = MutableStateFlow("08:00")
    val notifTime: StateFlow<String> = _notifTime.asStateFlow()

    init {
        loadAllSettings()
    }

    private fun loadAllSettings() {
        viewModelScope.launch {
            _shopName.value = settingRepository.getValue(SettingRepository.KEY_SHOP_NAME, "Vision Point")
            _shopAddress.value = settingRepository.getValue(SettingRepository.KEY_SHOP_ADDRESS, "")
            _shopPhone.value = settingRepository.getValue(SettingRepository.KEY_SHOP_PHONE, "")

            _itemTypes.value = settingRepository.getItemTypes()
            _eyeFields.value = settingRepository.getEyeFields()

            _msgStatusPending.value = settingRepository.getValue(SettingRepository.KEY_MSG_STATUS_PENDING, SettingRepository.DEFAULT_MSG_STATUS_PENDING)
            _msgStatusReady.value = settingRepository.getValue(SettingRepository.KEY_MSG_STATUS_READY, SettingRepository.DEFAULT_MSG_STATUS_READY)
            _msgStatusCompleted.value = settingRepository.getValue(SettingRepository.KEY_MSG_STATUS_COMPLETED, SettingRepository.DEFAULT_MSG_STATUS_COMPLETED)
            _msgBirthday.value = settingRepository.getValue(SettingRepository.KEY_MSG_BIRTHDAY, SettingRepository.DEFAULT_MSG_BIRTHDAY)
            _msgRecall.value = settingRepository.getValue(SettingRepository.KEY_MSG_RECALL, SettingRepository.DEFAULT_MSG_RECALL)
            _msgDue.value = settingRepository.getValue(SettingRepository.KEY_MSG_DUE, SettingRepository.DEFAULT_MSG_DUE)

            _notifEnabled.value = settingRepository.getValue(SettingRepository.KEY_NOTIF_ENABLED, "true").toBoolean()
            _notifRecalls.value = settingRepository.getValue(SettingRepository.KEY_NOTIF_RECALLS, "true").toBoolean()
            _notifBirthdays.value = settingRepository.getValue(SettingRepository.KEY_NOTIF_BIRTHDAYS, "true").toBoolean()
            _notifDues.value = settingRepository.getValue(SettingRepository.KEY_NOTIF_DUES, "true").toBoolean()
            _notifTime.value = settingRepository.getValue(SettingRepository.KEY_NOTIF_TIME, "08:00")
        }
    }

    fun onShopNameChange(value: String) { _shopName.value = value }
    fun onShopAddressChange(value: String) { _shopAddress.value = value }
    fun onShopPhoneChange(value: String) { _shopPhone.value = value }

    fun saveShopDetails(onSaved: () -> Unit) {
        viewModelScope.launch {
            settingRepository.setMultiple(
                mapOf(
                    SettingRepository.KEY_SHOP_NAME to _shopName.value.trim(),
                    SettingRepository.KEY_SHOP_ADDRESS to _shopAddress.value.trim(),
                    SettingRepository.KEY_SHOP_PHONE to _shopPhone.value.trim()
                )
            )
            onSaved()
        }
    }

    fun addItemType(type: String) {
        if (type.isBlank() || _itemTypes.value.contains(type.trim())) return
        val updated = _itemTypes.value + type.trim()
        _itemTypes.value = updated
        viewModelScope.launch { settingRepository.setItemTypes(updated) }
    }

    fun removeItemType(type: String) {
        val updated = _itemTypes.value - type
        _itemTypes.value = updated
        viewModelScope.launch { settingRepository.setItemTypes(updated) }
    }

    fun addEyeField(field: String) {
        if (field.isBlank() || _eyeFields.value.contains(field.trim())) return
        val updated = _eyeFields.value + field.trim()
        _eyeFields.value = updated
        viewModelScope.launch { settingRepository.setEyeFields(updated) }
    }

    fun removeEyeField(field: String) {
        val updated = _eyeFields.value - field
        _eyeFields.value = updated
        viewModelScope.launch { settingRepository.setEyeFields(updated) }
    }

    fun onMsgStatusPendingChange(value: String) { _msgStatusPending.value = value }
    fun onMsgStatusReadyChange(value: String) { _msgStatusReady.value = value }
    fun onMsgStatusCompletedChange(value: String) { _msgStatusCompleted.value = value }
    fun onMsgBirthdayChange(value: String) { _msgBirthday.value = value }
    fun onMsgRecallChange(value: String) { _msgRecall.value = value }
    fun onMsgDueChange(value: String) { _msgDue.value = value }

    fun saveMessageTemplates(onSaved: () -> Unit) {
        viewModelScope.launch {
            settingRepository.setMultiple(
                mapOf(
                    SettingRepository.KEY_MSG_STATUS_PENDING to _msgStatusPending.value,
                    SettingRepository.KEY_MSG_STATUS_READY to _msgStatusReady.value,
                    SettingRepository.KEY_MSG_STATUS_COMPLETED to _msgStatusCompleted.value,
                    SettingRepository.KEY_MSG_BIRTHDAY to _msgBirthday.value,
                    SettingRepository.KEY_MSG_RECALL to _msgRecall.value,
                    SettingRepository.KEY_MSG_DUE to _msgDue.value
                )
            )
            onSaved()
        }
    }

    fun resetMessageTemplates() {
        _msgStatusPending.value = SettingRepository.DEFAULT_MSG_STATUS_PENDING
        _msgStatusReady.value = SettingRepository.DEFAULT_MSG_STATUS_READY
        _msgStatusCompleted.value = SettingRepository.DEFAULT_MSG_STATUS_COMPLETED
        _msgBirthday.value = SettingRepository.DEFAULT_MSG_BIRTHDAY
        _msgRecall.value = SettingRepository.DEFAULT_MSG_RECALL
        _msgDue.value = SettingRepository.DEFAULT_MSG_DUE
        viewModelScope.launch {
            settingRepository.setMultiple(
                mapOf(
                    SettingRepository.KEY_MSG_STATUS_PENDING to SettingRepository.DEFAULT_MSG_STATUS_PENDING,
                    SettingRepository.KEY_MSG_STATUS_READY to SettingRepository.DEFAULT_MSG_STATUS_READY,
                    SettingRepository.KEY_MSG_STATUS_COMPLETED to SettingRepository.DEFAULT_MSG_STATUS_COMPLETED,
                    SettingRepository.KEY_MSG_BIRTHDAY to SettingRepository.DEFAULT_MSG_BIRTHDAY,
                    SettingRepository.KEY_MSG_RECALL to SettingRepository.DEFAULT_MSG_RECALL,
                    SettingRepository.KEY_MSG_DUE to SettingRepository.DEFAULT_MSG_DUE
                )
            )
        }
    }

    fun setNotificationToggle(context: Context, key: String, enabled: Boolean) {
        when (key) {
            SettingRepository.KEY_NOTIF_ENABLED -> {
                _notifEnabled.value = enabled
                if (!enabled) {
                    ReminderScheduler.cancelDailyReminder(context)
                } else {
                    ReminderScheduler.scheduleDailyReminder(context, _notifTime.value)
                }
            }
            SettingRepository.KEY_NOTIF_RECALLS -> _notifRecalls.value = enabled
            SettingRepository.KEY_NOTIF_BIRTHDAYS -> _notifBirthdays.value = enabled
            SettingRepository.KEY_NOTIF_DUES -> _notifDues.value = enabled
        }
        viewModelScope.launch {
            settingRepository.setValue(key, enabled.toString())
        }
    }

    fun setNotificationTime(context: Context, timeStr: String) {
        _notifTime.value = timeStr
        viewModelScope.launch {
            settingRepository.setValue(SettingRepository.KEY_NOTIF_TIME, timeStr)
            if (_notifEnabled.value) {
                ReminderScheduler.scheduleDailyReminder(context, timeStr)
            }
        }
    }

    fun testNotificationsNow(context: Context) {
        ReminderScheduler.triggerTestReminderNow(context)
    }

    fun syncAllCustomersToContacts(
        context: Context,
        onComplete: (syncedCount: Int, totalCount: Int) -> Unit
    ) {
        viewModelScope.launch {
            val allCustomers = customerRepository.getAllCustomers().first()
            val shopName = settingRepository.getValue(SettingRepository.KEY_SHOP_NAME, "Vision Point")
            var count = 0
            for (cust in allCustomers) {
                if (!ContactsHelper.isContactSaved(context, cust.phone)) {
                    val saved = ContactsHelper.saveCustomerToContacts(context, cust.name, cust.phone, shopName)
                    if (saved) count++
                }
            }
            onComplete(count, allCustomers.size)
        }
    }
}
