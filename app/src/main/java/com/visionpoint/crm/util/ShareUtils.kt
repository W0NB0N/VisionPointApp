package com.visionpoint.crm.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {

    fun sharePdf(context: Context, file: File, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share $title"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun viewPdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareViaWhatsApp(
        context: Context,
        phone: String,
        message: String,
        pdfFile: File? = null
    ) {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone

        try {
            if (pdfFile != null) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    setPackage("com.whatsapp")
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("jid", "$formattedPhone@s.whatsapp.net")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            } else {
                val url = if (message.isNotBlank()) {
                    "https://wa.me/$formattedPhone?text=${Uri.encode(message)}"
                } else {
                    "https://wa.me/$formattedPhone"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Fallback to standard share chooser
            if (pdfFile != null) {
                sharePdf(context, pdfFile, "Document")
            } else {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
