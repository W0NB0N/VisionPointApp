package com.visionpoint.crm.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.visionpoint.crm.data.database.VisionPointDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BackupManager {

    fun exportDatabaseToDownloads(context: Context): Result<File> {
        return try {
            val dbPath = context.getDatabasePath(VisionPointDatabase.DATABASE_NAME)
            if (!dbPath.exists()) {
                return Result.failure(IllegalStateException("Database file does not exist yet"))
            }

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val backupFileName = "VisionPoint_backup_$timestamp.db"

            // Target in public Downloads or fallback to app-specific external files
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destFile = if (downloadsDir != null && downloadsDir.exists()) {
                File(downloadsDir, backupFileName)
            } else {
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), backupFileName)
            }

            FileInputStream(dbPath).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Also copy WAL and SHM files if they exist
            val walFile = File(dbPath.path + "-wal")
            if (walFile.exists()) {
                val destWal = File(destFile.parentFile, "$backupFileName-wal")
                FileInputStream(walFile).use { input ->
                    FileOutputStream(destWal).use { output -> input.copyTo(output) }
                }
            }

            val shmFile = File(dbPath.path + "-shm")
            if (shmFile.exists()) {
                val destShm = File(destFile.parentFile, "$backupFileName-shm")
                FileInputStream(shmFile).use { input ->
                    FileOutputStream(destShm).use { output -> input.copyTo(output) }
                }
            }

            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareBackupFile(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Vision Point CRM Backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Database Backup"))
        } catch (e: Exception) {
            // Fallback
        }
    }
}
