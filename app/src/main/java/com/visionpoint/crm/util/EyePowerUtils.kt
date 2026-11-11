package com.visionpoint.crm.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object EyePowerUtils {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun encodeEyeData(map: Map<String, String>): String {
        val cleanMap = map.filterValues { it.isNotBlank() }
        return json.encodeToString(cleanMap)
    }

    fun decodeEyeData(jsonStr: String?): Map<String, String> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        return try {
            json.decodeFromString<Map<String, String>>(jsonStr)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
