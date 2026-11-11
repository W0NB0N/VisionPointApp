package com.visionpoint.crm.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.visionpoint.crm.MainActivity
import com.visionpoint.crm.VisionPointApp
import com.visionpoint.crm.data.repository.CustomerRepository
import com.visionpoint.crm.data.repository.SaleRepository
import com.visionpoint.crm.data.repository.SettingRepository
import com.visionpoint.crm.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val settingRepository: SettingRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val notifEnabled = settingRepository.getValue(SettingRepository.KEY_NOTIF_ENABLED, "true").toBoolean()
            if (!notifEnabled) {
                return Result.success()
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val permission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    return Result.success()
                }
            }

            val notifRecalls = settingRepository.getValue(SettingRepository.KEY_NOTIF_RECALLS, "true").toBoolean()
            val notifBirthdays = settingRepository.getValue(SettingRepository.KEY_NOTIF_BIRTHDAYS, "true").toBoolean()
            val notifDues = settingRepository.getValue(SettingRepository.KEY_NOTIF_DUES, "true").toBoolean()

            val today = LocalDate.now()
            val notificationManager = NotificationManagerCompat.from(context)

            // 1. Check Recalls
            if (notifRecalls) {
                val recalls = saleRepository.getRecallsForDate(today)
                recalls.forEach { recall ->
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("route", Screen.SaleDetail.createRoute(recall.sale.id))
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        recall.sale.id + 10000,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(context, VisionPointApp.CHANNEL_RECALLS)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("👓 Recall Due: ${recall.customer.name}")
                        .setContentText("Eye checkup / recall scheduled for today (Bill #${recall.sale.id}).")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    try {
                        notificationManager.notify(recall.sale.id + 10000, notification)
                    } catch (e: SecurityException) {
                        // Permission not granted
                    }
                }
            }

            // 2. Check Birthdays
            if (notifBirthdays) {
                val birthdays = customerRepository.getBirthdaysToday(today)
                birthdays.forEach { customer ->
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("route", Screen.CustomerDetail.createRoute(customer.id))
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        customer.id + 20000,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val ageText = if (customer.age != null) " (turning ${customer.age})" else ""
                    val notification = NotificationCompat.Builder(context, VisionPointApp.CHANNEL_BIRTHDAYS)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("🎂 Birthday Today: ${customer.name}$ageText")
                        .setContentText("Send birthday wishes to ${customer.name} via WhatsApp!")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    try {
                        notificationManager.notify(customer.id + 20000, notification)
                    } catch (e: SecurityException) {
                        // Permission not granted
                    }
                }
            }

            // 3. Check Pending Dues
            if (notifDues) {
                val pendingSales = saleRepository.getSalesWithPendingDues()
                if (pendingSales.isNotEmpty()) {
                    val totalDue = pendingSales.sumOf { it.sale.dueAmount }
                    val count = pendingSales.size
                    val formattedDue = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(totalDue)

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("route", Screen.SalesList.route)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        30001,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(context, VisionPointApp.CHANNEL_DUES)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("💰 Outstanding Dues Reminder")
                        .setContentText("$count bills with $formattedDue total balance due. Tap to view.")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .build()

                    try {
                        notificationManager.notify(30001, notification)
                    } catch (e: SecurityException) {
                        // Permission not granted
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
