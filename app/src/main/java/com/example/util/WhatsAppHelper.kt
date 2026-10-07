package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    fun sendWhatsApp(context: Context, rawMobile: String, message: String) {
        // Sanitize phone number (remove spaces, hyphens)
        val cleanNumber = rawMobile.replace(Regex("[^0-9]"), "")
        // Prepend country code 91 for India if 10-digit number
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber

        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to generic share chooser
            shareTextMessage(context, message)
        }
    }

    fun shareTextMessage(context: Context, message: String) {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Chitti Reminder")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Chitti Message") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
