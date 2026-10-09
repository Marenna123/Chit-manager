package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppHelper {

    /**
     * Constructs a WhatsApp Intent with pre-filled payment details for a specific phone number.
     */
    fun createWhatsAppPaymentIntent(rawMobile: String, message: String): Intent {
        val cleanNumber = rawMobile.replace(Regex("[^0-9]"), "")
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (_: Exception) {
            Uri.encode(message)
        }

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Triggers a WhatsApp message intent with pre-filled payment details for members with outstanding installments.
     * Tries the standard WhatsApp client first, then WhatsApp Business, and falls back to system sharing
     * if neither is installed.
     *
     * @param context Context used to start activity
     * @param rawMobile Member's mobile phone number
     * @param message Pre-filled payment reminder message
     * @return true if the intent was started successfully, false otherwise
     */
    fun triggerWhatsAppMessageIntent(context: Context, rawMobile: String, message: String): Boolean {
        val cleanNumber = rawMobile.replace(Regex("[^0-9]"), "")
        val formattedNumber = if (cleanNumber.length == 10) "91$cleanNumber" else cleanNumber
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (_: Exception) {
            Uri.encode(message)
        }

        // 1. Try launching with WhatsApp package (com.whatsapp)
        try {
            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")).apply {
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (waIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(waIntent)
                return true
            }
        } catch (_: Exception) {
            // Continue to next option
        }

        // 2. Try launching with WhatsApp Business package (com.whatsapp.w4b)
        try {
            val w4bIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")).apply {
                setPackage("com.whatsapp.w4b")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (w4bIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(w4bIntent)
                return true
            }
        } catch (_: Exception) {
            // Continue to next option
        }

        // 3. Try standard Uri intent (handles WhatsApp or browser redirect)
        try {
            val genericWaIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMessage")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(genericWaIntent)
            return true
        } catch (_: Exception) {
            // 4. Fallback to generic text share chooser
            return shareTextMessage(context, message)
        }
    }

    /**
     * Backward-compatible alias for triggerWhatsAppMessageIntent
     */
    fun sendWhatsApp(context: Context, rawMobile: String, message: String) {
        triggerWhatsAppMessageIntent(context, rawMobile, message)
    }

    fun shareTextMessage(context: Context, message: String): Boolean {
        return try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Payment Reminder").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open sharing: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Chitti Message") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
