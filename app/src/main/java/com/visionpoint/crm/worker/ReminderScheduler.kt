package com.visionpoint.crm.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val WORK_NAME = "vision_point_daily_reminders"
    const val WORK_TAG = "daily_reminders_tag"

    fun scheduleDailyReminder(context: Context, timeString: String = "08:00") {
        val targetTime = try {
            val parts = timeString.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            LocalTime.of(8, 0)
        }

        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)
        var targetDateTime = now.withHour(targetTime.hour).withMinute(targetTime.minute).withSecond(0).withNano(0)

        if (now.isAfter(targetDateTime)) {
            targetDateTime = targetDateTime.plusDays(1)
        }

        val initialDelayMillis = Duration.between(now, targetDateTime).toMillis()

        val periodicWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWork
        )
    }

    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun triggerTestReminderNow(context: Context) {
        val oneTimeWork = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .addTag("test_reminder")
            .build()
        WorkManager.getInstance(context).enqueue(oneTimeWork)
    }
}
