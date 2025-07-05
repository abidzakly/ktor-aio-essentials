package com.abidzakly.essentials.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import androidx.core.os.ConfigurationCompat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Universal Date Extensions Library
 * Provides dynamic locale-aware date formatting with special Indonesian support
 * Optimized for AAR export and universal usage
 */

// Extension functions for Firebase Timestamp
fun Timestamp?.formatWith(pattern: String = "yyyy-MM-dd", locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith(pattern, locale) ?: "N/A"
}

fun Timestamp?.toSlashDateFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith("dd/MM/yyyy", locale) ?: "N/A"
}

fun Timestamp?.toReadableDateFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith("dd MMM yyyy", locale) ?: "N/A"
}

fun Timestamp?.toTimeOnlyFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith("HH:mm", locale) ?: "N/A"
}

fun Timestamp?.toFullDateTimeFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith("dd-MM-yyyy HH.mm", locale) ?: ""
}

// Extension functions for Date
fun Date?.formatWith(pattern: String = "yyyy-MM-dd", locale: Locale = Locale.getDefault()): String {
    return this?.let { date ->
        SimpleDateFormat(pattern, locale).apply {
            timeZone = TimeZone.getDefault()
        }.format(date)
    } ?: "N/A"
}

fun Date?.toSlashDateFormat(locale: Locale = Locale.getDefault()): String =
    this?.formatWith("dd/MM/yyyy", locale) ?: "N/A"

fun Date?.toReadableDateFormat(locale: Locale = Locale.getDefault()): String =
    this?.formatWith("dd MMM yyyy", locale) ?: "N/A"

fun Date?.toTimeOnlyFormat(locale: Locale = Locale.getDefault()): String =
    this?.formatWith("HH:mm", locale) ?: "N/A"

fun Date?.toFullDateTimeFormat(locale: Locale = Locale.getDefault()): String =
    this?.formatWith("dd-MM-yyyy HH.mm", locale) ?: ""

// Advanced formatting extensions
fun Timestamp?.toReadableDayAndDateFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith("EEE, dd MMM", locale) ?: "N/A"
}

fun Timestamp?.toSocialMediaTimeFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.toSocialMediaTimeFormat(locale) ?: ""
}

fun Date?.toSocialMediaTimeFormat(locale: Locale = Locale.getDefault()): String {
    return this?.let { date ->
        val calendar = Calendar.getInstance().apply { time = date }
        val now = Calendar.getInstance()

        val timeFormat = SimpleDateFormat("h:mm a", locale).apply { timeZone = TimeZone.getDefault() }
        val fullDateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", locale).apply { timeZone = TimeZone.getDefault() }

        // Calculate difference in days
        val startDay = Calendar.getInstance().apply {
            time = calendar.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endDay = Calendar.getInstance().apply {
            time = now.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays = ((endDay.timeInMillis - startDay.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        when {
            now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) -> {
                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    date.time,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
                "Today, $timeAgo"
            }
            diffDays == 1 -> "Yesterday, ${timeFormat.format(date)}"
            diffDays in 2..6 -> "$diffDays days ago, ${timeFormat.format(date)}"
            diffDays in 7..13 -> "Last week, ${timeFormat.format(date)}"
            else -> fullDateFormat.format(date)
        }
    } ?: ""
}

@SuppressLint("SimpleDateFormat")
fun Timestamp?.toCompactDateTimeFormat(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.formatWith("dd MMM yyyy, HH.mm", locale) ?: ""
}

// Relative time extensions
fun Timestamp?.toHumanReadableTimeAgo(locale: Locale = Locale.getDefault()): String {
    return this?.toDate()?.toHumanReadableTimeAgo(locale) ?: ""
}

fun Date?.toHumanReadableTimeAgo(locale: Locale = Locale.getDefault()): String {
    return this?.let { date ->
        val now = System.currentTimeMillis()
        val time = date.time
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes.toInt()} minute${if (minutes > 1) "s" else ""} ago"
            hours < 24 -> "${hours.toInt()} hour${if (hours > 1) "s" else ""} ago"
            days == 1L -> "Yesterday"
            days < 7 -> "${days.toInt()} day${if (days > 1) "s" else ""} ago"
            weeks == 1L -> "A week ago"
            weeks < 4 -> "${weeks.toInt()} week${if (weeks > 1) "s" else ""} ago"
            months == 1L -> "A month ago"
            months < 12 -> "${months.toInt()} month${if (months > 1) "s" else ""} ago"
            years == 1L -> "A year ago"
            else -> "${years.toInt()} year${if (years > 1) "s" else ""} ago"
        }
    } ?: ""
}

// Indonesian-specific extensions
fun Timestamp?.toIndonesianSlashDateFormat(): String = this?.toSlashDateFormat(Locale("id", "ID")) ?: "N/A"
fun Timestamp?.toIndonesianReadableDateFormat(): String = this?.toReadableDateFormat(Locale("id", "ID")) ?: "N/A"
fun Timestamp?.toIndonesianTimeOnlyFormat(): String = this?.toTimeOnlyFormat(Locale("id", "ID")) ?: "N/A"
fun Timestamp?.toIndonesianFullDateTimeFormat(): String = this?.toFullDateTimeFormat(Locale("id", "ID")) ?: ""
fun Timestamp?.toIndonesianReadableDayAndDateFormat(): String = this?.toReadableDayAndDateFormat(Locale("id", "ID")) ?: "N/A"

fun Timestamp?.toIndonesianTimeAgo(): String {
    return this?.let { timestamp ->
        val now = System.currentTimeMillis()
        val time = timestamp.toDate().time
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        when {
            seconds < 60 -> "Baru saja"
            minutes < 60 -> "${minutes.toInt()} menit yang lalu"
            hours < 24 -> "${hours.toInt()} jam yang lalu"
            days == 1L -> "Kemarin"
            days < 7 -> "${days.toInt()} hari yang lalu"
            weeks == 1L -> "Seminggu yang lalu"
            weeks < 4 -> "${weeks.toInt()} minggu yang lalu"
            months == 1L -> "Sebulan yang lalu"
            months < 12 -> "${months.toInt()} bulan yang lalu"
            years == 1L -> "Setahun yang lalu"
            else -> "${years.toInt()} tahun yang lalu"
        }
    } ?: ""
}

// Date extension versions for Indonesian
fun Date?.toIndonesianSlashDateFormat(): String = this?.toSlashDateFormat(Locale("id", "ID")) ?: "N/A"
fun Date?.toIndonesianReadableDateFormat(): String = this?.toReadableDateFormat(Locale("id", "ID")) ?: "N/A"
fun Date?.toIndonesianTimeOnlyFormat(): String = this?.toTimeOnlyFormat(Locale("id", "ID")) ?: "N/A"
fun Date?.toIndonesianFullDateTimeFormat(): String = this?.toFullDateTimeFormat(Locale("id", "ID")) ?: ""
fun Date?.toIndonesianReadableDayAndDateFormat(): String = this?.formatWith("EEE, dd MMM", Locale("id", "ID")) ?: "N/A"

fun Date?.toIndonesianTimeAgo(): String {
    return this?.let { date ->
        val now = System.currentTimeMillis()
        val time = date.time
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        when {
            seconds < 60 -> "Baru saja"
            minutes < 60 -> "${minutes.toInt()} menit yang lalu"
            hours < 24 -> "${hours.toInt()} jam yang lalu"
            days == 1L -> "Kemarin"
            days < 7 -> "${days.toInt()} hari yang lalu"
            weeks == 1L -> "Seminggu yang lalu"
            weeks < 4 -> "${weeks.toInt()} minggu yang lalu"
            months == 1L -> "Sebulan yang lalu"
            months < 12 -> "${months.toInt()} bulan yang lalu"
            years == 1L -> "Setahun yang lalu"
            else -> "${years.toInt()} tahun yang lalu"
        }
    } ?: ""
}

// String parsing extensions
fun String?.parseToFirebaseTimestamp(pattern: String = "yyyy-MM-dd", locale: Locale = Locale.getDefault()): Timestamp? {
    return this?.let { dateString ->
        try {
            val sdf = SimpleDateFormat(pattern, locale)
            val date = sdf.parse(dateString)
            date?.let { Timestamp(it) }
        } catch (e: Exception) {
            null
        }
    }
}

fun String?.parseToDate(pattern: String = "yyyy-MM-dd", locale: Locale = Locale.getDefault()): Date? {
    return this?.let { dateString ->
        try {
            SimpleDateFormat(pattern, locale).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

// Context-aware locale extensions
fun Context.getDeviceLocale(): Locale {
    return ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()
}

fun Timestamp?.formatWithDeviceLocale(context: Context, pattern: String = "yyyy-MM-dd"): String {
    return this?.formatWith(pattern, context.getDeviceLocale()) ?: "N/A"
}

fun Date?.formatWithDeviceLocale(context: Context, pattern: String = "yyyy-MM-dd"): String {
    return this?.formatWith(pattern, context.getDeviceLocale()) ?: "N/A"
}

fun Timestamp?.toSlashDateFormat(context: Context): String = this?.toSlashDateFormat(context.getDeviceLocale()) ?: "N/A"
fun Timestamp?.toReadableDateFormat(context: Context): String = this?.toReadableDateFormat(context.getDeviceLocale()) ?: "N/A"
fun Timestamp?.toTimeOnlyFormat(context: Context): String = this?.toTimeOnlyFormat(context.getDeviceLocale()) ?: "N/A"
fun Timestamp?.toFullDateTimeFormat(context: Context): String = this?.toFullDateTimeFormat(context.getDeviceLocale()) ?: ""
fun Timestamp?.toSocialMediaTimeFormat(context: Context): String = this?.toSocialMediaTimeFormat(context.getDeviceLocale()) ?: ""
fun Timestamp?.toHumanReadableTimeAgo(context: Context): String = this?.toHumanReadableTimeAgo(context.getDeviceLocale()) ?: ""

fun Date?.toSlashDateFormat(context: Context): String = this?.toSlashDateFormat(context.getDeviceLocale()) ?: "N/A"
fun Date?.toReadableDateFormat(context: Context): String = this?.toReadableDateFormat(context.getDeviceLocale()) ?: "N/A"
fun Date?.toTimeOnlyFormat(context: Context): String = this?.toTimeOnlyFormat(context.getDeviceLocale()) ?: "N/A"
fun Date?.toFullDateTimeFormat(context: Context): String = this?.toFullDateTimeFormat(context.getDeviceLocale()) ?: ""
fun Date?.toSocialMediaTimeFormat(context: Context): String = this?.toSocialMediaTimeFormat(context.getDeviceLocale()) ?: ""
fun Date?.toHumanReadableTimeAgo(context: Context): String = this?.toHumanReadableTimeAgo(context.getDeviceLocale()) ?: ""

fun String?.parseToFirebaseTimestamp(context: Context, pattern: String = "yyyy-MM-dd"): Timestamp? {
    return this?.parseToFirebaseTimestamp(pattern, context.getDeviceLocale())
}

fun String?.parseToDate(context: Context, pattern: String = "yyyy-MM-dd"): Date? {
    return this?.parseToDate(pattern, context.getDeviceLocale())
}

/**
 * Usage Examples:
 *
 * // Basic usage with default locale
 * val timestamp = Timestamp.now()
 * val formatted = timestamp.toReadableDateFormat() // Uses device locale
 *
 * // With specific locale
 * val formatted = timestamp.toReadableDateFormat(Locale.US)
 *
 * // Indonesian specific
 * val indonesianFormat = timestamp.toIndonesianReadableDateFormat()
 * val indonesianRelative = timestamp.toIndonesianTimeAgo()
 *
 * // Context-aware (automatically uses device locale)
 * val contextFormatted = timestamp.toReadableDateFormat(context)
 *
 * // String parsing
 * val timestamp = "2024-01-15".parseToFirebaseTimestamp()
 * val date = "2024-01-15".parseToDate()
 *
 * // Date extensions
 * val date = Date()
 * val formatted = date.toReadableDateFormat()
 * val indonesianFormatted = date.toIndonesianReadableDateFormat()
 *
 * // Social media style formatting
 * val socialFormat = timestamp.toSocialMediaTimeFormat()
 *
 * // Human readable time ago
 * val timeAgo = timestamp.toHumanReadableTimeAgo()
 *
 * // Specific formatting patterns
 * val customFormat = timestamp.formatWith("dd/MM/yyyy HH:mm")
 */