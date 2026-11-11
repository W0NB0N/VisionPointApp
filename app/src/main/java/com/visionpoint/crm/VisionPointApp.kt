package com.visionpoint.crm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.visionpoint.crm.worker.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VisionPointApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    companion object {
        const val CHANNEL_RECALLS = "recalls_channel"
        const val CHANNEL_BIRTHDAYS = "birthdays_channel"
        const val CHANNEL_DUES = "dues_channel"
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        ReminderScheduler.scheduleDailyReminder(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val recallsChannel = NotificationChannel(
                CHANNEL_RECALLS,
                "Recall Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for customers with eye checkup/recalls due today"
            }

            val birthdaysChannel = NotificationChannel(
                CHANNEL_BIRTHDAYS,
                "Birthday Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for customer birthdays today"
            }

            val duesChannel = NotificationChannel(
                CHANNEL_DUES,
                "Payment Due Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for pending customer dues and balances"
            }

            notificationManager.createNotificationChannels(
                listOf(recallsChannel, birthdaysChannel, duesChannel)
            )
        }
    }
}
