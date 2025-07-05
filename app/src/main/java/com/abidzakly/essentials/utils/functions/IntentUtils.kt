package com.abidzakly.essentials.utils.functions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

/**
 * Utility class for handling various intents and external app integrations.
 */
object IntentUtils {

    private const val TAG = "IntentUtils"
    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    /**
     * Opens WhatsApp with a pre-filled message to a specific phone number.
     *
     * @param context Application context
     * @param phoneNumber The phone number to send message to
     * @param message The pre-filled message
     * @param showLoadingToast Whether to show loading toast
     */
    fun openWhatsAppWithTemplate(
        context: Context,
        phoneNumber: String,
        message: String,
        showLoadingToast: Boolean = true
    ) {
        try {
            if (showLoadingToast) {
                Toast.makeText(context, "Opening WhatsApp...", Toast.LENGTH_SHORT).show()
            }

            val normalizedNumber = normalizePhoneNumber(phoneNumber)
            val encodedMessage = Uri.encode(message)
            val uri = Uri.parse("https://wa.me/$normalizedNumber?text=$encodedMessage")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                // Try WhatsApp first, then WhatsApp Business as fallback
                `package` = if (isPackageInstalled(context, WHATSAPP_PACKAGE)) {
                    WHATSAPP_PACKAGE
                } else if (isPackageInstalled(context, WHATSAPP_BUSINESS_PACKAGE)) {
                    WHATSAPP_BUSINESS_PACKAGE
                } else {
                    null // Let system handle it
                }
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WhatsApp: ${e.message}", e)
            Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the default email client with pre-filled recipient, subject, and body.
     *
     * @param context Application context
     * @param recipient Email recipient
     * @param subject Email subject
     * @param body Email body
     */
    fun openEmailClient(
        context: Context,
        recipient: String,
        subject: String = "",
        body: String = ""
    ) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$recipient")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            context.startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening email client: ${e.message}", e)
            Toast.makeText(context, "Unable to open email client", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the phone dialer with a pre-filled phone number.
     *
     * @param context Application context
     * @param phoneNumber The phone number to dial
     */
    fun openPhoneDialer(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening phone dialer: ${e.message}", e)
            Toast.makeText(context, "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    // Private helper methods

    private fun normalizePhoneNumber(phoneNumber: String): String {
        var normalized = phoneNumber.trim()
            .replace(Regex("[^\\d+]"), "") // Keep only digits and '+'

        return when {
            normalized.startsWith("+") -> normalized.substring(1)
            normalized.startsWith("0") -> "62${normalized.substring(1)}" // Indonesia country code
            normalized.startsWith("62") -> normalized
            else -> "62$normalized" // Assume Indonesian number
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}