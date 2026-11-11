package com.visionpoint.crm.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    val IST_ZONE: ZoneId = ZoneId.of("Asia/Kolkata")

    private val DEFAULT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    private val FULL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
    private val TIME_12H_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    private val TIME_24H_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    fun getTodayIST(): LocalDate = LocalDate.now(IST_ZONE)

    fun formatDate(date: LocalDate?, pattern: String = "dd/MM/yyyy"): String {
        if (date == null) return ""
        return try {
            val formatter = if (pattern == "dd/MM/yyyy") DEFAULT_DATE_FORMATTER else DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
            date.format(formatter)
        } catch (e: Exception) {
            date.toString()
        }
    }

    fun formatFullDate(date: LocalDate?): String {
        if (date == null) return ""
        return date.format(FULL_DATE_FORMATTER)
    }

    fun formatEpochMillis(epochMillis: Long, pattern: String = "dd/MM/yyyy HH:mm"): String {
        return try {
            val instant = Instant.ofEpochMilli(epochMillis)
            val zonedDateTime = instant.atZone(IST_ZONE)
            zonedDateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
        } catch (e: Exception) {
            epochMillis.toString()
        }
    }

    fun formatTime(hour: Int, minute: Int, use12Hour: Boolean = true): String {
        val time = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        return if (use12Hour) time.format(TIME_12H_FORMATTER) else time.format(TIME_24H_FORMATTER)
    }

    fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.trim().split(":")
            if (parts.size >= 2) {
                val hour = parts[0].toIntOrNull() ?: 8
                val min = parts[1].take(2).toIntOrNull() ?: 0
                Pair(hour.coerceIn(0, 23), min.coerceIn(0, 59))
            } else {
                Pair(8, 0)
            }
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }

    fun formatTimeString(timeStr: String, use12Hour: Boolean = true): String {
        val (h, m) = parseTime(timeStr)
        return formatTime(h, m, use12Hour)
    }
}
