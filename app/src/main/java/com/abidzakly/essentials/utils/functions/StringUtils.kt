package com.abidzakly.essentials.utils.functions
import java.util.UUID

/**
 * Utility class for string-related operations.
 */
object StringUtils {

    /**
     * Generates a short UUID string (first 12 characters).
     *
     * @return A short UUID string
     */
    fun generateShortUUID(): String {
        return UUID.randomUUID().toString().substring(0, 12)
    }

    /**
     * Generates a full UUID string.
     *
     * @return A full UUID string
     */
    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Checks if a string is a valid email address.
     *
     * @param email The email string to validate
     * @return true if valid email, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if a string is a valid phone number.
     *
     * @param phone The phone string to validate
     * @return true if valid phone, false otherwise
     */
    fun isValidPhone(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }

    /**
     * Capitalizes the first letter of each word in a string.
     *
     * @param text The text to capitalize
     * @return Capitalized text
     */
    fun capitalizeWords(text: String): String {
        return text.split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}