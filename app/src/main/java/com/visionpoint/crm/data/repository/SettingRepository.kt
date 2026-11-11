package com.visionpoint.crm.data.repository

import com.visionpoint.crm.data.database.dao.SettingDao
import com.visionpoint.crm.data.model.Setting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository @Inject constructor(
    private val settingDao: SettingDao
) {
    companion object {
        const val KEY_SHOP_NAME = "shop_name"
        const val KEY_SHOP_ADDRESS = "shop_address"
        const val KEY_SHOP_PHONE = "shop_phone"
        const val KEY_ITEM_TYPES = "item_types"
        const val KEY_EYE_FIELDS = "eye_fields"
        const val KEY_MSG_BIRTHDAY = "msg_birthday"
        const val KEY_MSG_RECALL = "msg_recall"
        const val KEY_MSG_DUE = "msg_due"
        const val KEY_MSG_STATUS_PENDING = "msg_status_pending"
        const val KEY_MSG_STATUS_READY = "msg_status_ready"
        const val KEY_MSG_STATUS_COMPLETED = "msg_status_completed"
        const val KEY_NOTIF_ENABLED = "notif_enabled"
        const val KEY_NOTIF_RECALLS = "notif_recalls"
        const val KEY_NOTIF_BIRTHDAYS = "notif_birthdays"
        const val KEY_NOTIF_DUES = "notif_dues"
        const val KEY_NOTIF_TIME = "notif_time"
        const val KEY_THEME_MODE = "theme_mode" // "system", "light", "dark"
        const val KEY_DYNAMIC_COLOR = "dynamic_color" // "true", "false"

        const val DEFAULT_SHOP_ADDRESS = "Shop no. A-1, DDA Mini Market, Opp. Gyan Bharti School, Saket, New Delhi-110017"

        val DEFAULT_ITEM_TYPES = listOf("Frames", "Sunglass", "Contact Lens", "Eye Testing", "Repair")
        val DEFAULT_EYE_FIELDS = listOf("SPH", "CYL", "Axis", "Add", "V/A")

        const val DEFAULT_MSG_BIRTHDAY =
            "🎂 Happy Birthday, {name}! Wishing you a wonderful day. Thank you for being a valued customer of {shop}. 🎉"
        const val DEFAULT_MSG_RECALL =
            "Hello {name}, this is a reminder from {shop} for your scheduled eye checkup/follow-up. Please visit us at your convenience. 👓"
        const val DEFAULT_MSG_DUE =
            "Hello {name}, this is a gentle reminder from {shop}. You have an outstanding balance of ₹{amount} for Bill #{bill}. Please visit us for payment at your convenience."
        const val DEFAULT_MSG_STATUS_PENDING =
            "Thank you for visiting {shop}, {name}! Your order (Bill #{bill}) has been received and is being processed. Outstanding balance: ₹{amount}. We will notify you once your spectacles are ready! 👓"
        const val DEFAULT_MSG_STATUS_READY =
            "Hello {name}, your spectacle/order (Bill #{bill}) is READY for pickup at {shop}! 👓 Please visit us to collect. Balance due: ₹{amount}."
        const val DEFAULT_MSG_STATUS_COMPLETED =
            "Thank you {name} for choosing {shop}! Your order (Bill #{bill}) is completed. Please reach out to us if you need any frame adjustments or checkups. Have a wonderful day! 😊"
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun getAllSettings(): Flow<List<Setting>> = settingDao.getAll()

    fun getSettingFlow(key: String, defaultValue: String): Flow<String> {
        return settingDao.getByKey(key).map { it?.value ?: defaultValue }
    }

    suspend fun getValue(key: String, defaultValue: String? = null): String {
        return settingDao.getValue(key) ?: defaultValue ?: when (key) {
            KEY_SHOP_NAME -> "Vision Point"
            KEY_SHOP_ADDRESS -> DEFAULT_SHOP_ADDRESS
            KEY_MSG_BIRTHDAY -> DEFAULT_MSG_BIRTHDAY
            KEY_MSG_RECALL -> DEFAULT_MSG_RECALL
            KEY_MSG_DUE -> DEFAULT_MSG_DUE
            KEY_MSG_STATUS_PENDING -> DEFAULT_MSG_STATUS_PENDING
            KEY_MSG_STATUS_READY -> DEFAULT_MSG_STATUS_READY
            KEY_MSG_STATUS_COMPLETED -> DEFAULT_MSG_STATUS_COMPLETED
            KEY_NOTIF_ENABLED -> "true"
            KEY_NOTIF_RECALLS -> "true"
            KEY_NOTIF_BIRTHDAYS -> "true"
            KEY_NOTIF_DUES -> "true"
            KEY_NOTIF_TIME -> "08:00"
            KEY_THEME_MODE -> "system"
            KEY_DYNAMIC_COLOR -> "false"
            else -> ""
        }
    }

    suspend fun setValue(key: String, value: String) {
        settingDao.upsert(Setting(key = key, value = value))
    }

    suspend fun setMultiple(settingsMap: Map<String, String>) {
        val list = settingsMap.map { Setting(key = it.key, value = it.value) }
        settingDao.upsertAll(list)
    }

    suspend fun getItemTypes(): List<String> {
        val raw = settingDao.getValue(KEY_ITEM_TYPES) ?: return DEFAULT_ITEM_TYPES
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (e: Exception) {
            DEFAULT_ITEM_TYPES
        }
    }

    suspend fun setItemTypes(types: List<String>) {
        val raw = json.encodeToString(types)
        settingDao.upsert(Setting(key = KEY_ITEM_TYPES, value = raw))
    }

    suspend fun getEyeFields(): List<String> {
        val raw = settingDao.getValue(KEY_EYE_FIELDS) ?: return DEFAULT_EYE_FIELDS
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (e: Exception) {
            DEFAULT_EYE_FIELDS
        }
    }

    suspend fun setEyeFields(fields: List<String>) {
        val raw = json.encodeToString(fields)
        settingDao.upsert(Setting(key = KEY_EYE_FIELDS, value = raw))
    }
}
