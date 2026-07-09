package com.pab.digitallearning.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {

    fun getCountdownString(dueDateStr: String?): String {
        if (dueDateStr.isNullOrBlank()) return "Tidak ada tenggat waktu"
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )
        var date: Date? = null
        for (fmt in formats) {
            try {
                val parser = SimpleDateFormat(fmt, Locale.getDefault())
                if (fmt.endsWith("'Z'")) {
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                }
                date = parser.parse(dueDateStr)
                if (date != null) break
            } catch (e: Exception) {
                // Try next format
            }
        }
        if (date == null) return "Format tenggat tidak valid"

        val diffMs = date.time - System.currentTimeMillis()
        if (diffMs <= 0) {
            return "Tenggat Terlewati"
        }

        val seconds = diffMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val remainingHours = hours % 24
        val remainingMinutes = minutes % 60

        return when {
            days > 0 -> "Sisa Waktu: $days Hari $remainingHours Jam"
            hours > 0 -> "Sisa Waktu: $hours Jam $remainingMinutes Menit"
            else -> "Sisa Waktu: $minutes Menit"
        }
    }

    /**
     * Returns true ONLY if the teacher has explicitly closed the content (isClosed = true).
     * Does NOT consider overdue date. Use this to block ALL submissions unconditionally.
     */
    fun isStrictlyClosed(isClosed: Boolean): Boolean {
        return isClosed
    }

    /**
     * Returns true if the due date has already passed (regardless of isClosed flag).
     * Use this to show a 'Terlambat' warning while still allowing late submission.
     */
    fun isOverdue(dueDateStr: String?): Boolean {
        if (dueDateStr.isNullOrBlank()) return false
        val date = parseDate(dueDateStr) ?: return false
        return date.time <= System.currentTimeMillis()
    }

    /**
     * Legacy helper: content is considered "closed" either because the teacher explicitly
     * closed it (isClosed = true) OR the due date has passed.
     * Kept for backward compatibility — prefer isStrictlyClosed + isOverdue separately.
     */
    fun isContentClosed(dueDateStr: String?, isClosed: Boolean): Boolean {
        if (isClosed) return true
        return isOverdue(dueDateStr)
    }

    private fun parseDate(dueDateStr: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )
        for (fmt in formats) {
            try {
                val parser = SimpleDateFormat(fmt, Locale.getDefault())
                if (fmt.endsWith("'Z'")) {
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = parser.parse(dueDateStr)
                if (date != null) return date
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
    }

    /**
     * Given the submission time and the content's due date, returns a human-readable string:
     * - If submitted before/on deadline: "Sisa X jam Y menit" (remaining time at the moment of submit)
     * - If submitted after deadline: "Terlambat X jam Y menit" (how late)
     * - If dueDate is null/blank: returns null (don't show anything)
     */
    fun getLateInfo(submittedAtStr: String, dueDateStr: String?): String? {
        if (dueDateStr.isNullOrBlank()) return null
        val submittedAt = parseDate(submittedAtStr) ?: return null
        val dueDate = parseDate(dueDateStr) ?: return null

        val diffMs = submittedAt.time - dueDate.time

        return if (diffMs <= 0) {
            // Submitted ON TIME — show remaining time at moment of submission
            val remaining = -diffMs
            val hours = remaining / (1000 * 60 * 60)
            val minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60)
            when {
                hours > 0 -> "Dikumpul ${hours}j ${minutes}m sebelum tenggat"
                minutes > 0 -> "Dikumpul ${minutes} menit sebelum tenggat"
                else -> "Dikumpul tepat pada tenggat waktu"
            }
        } else {
            // Submitted LATE — show how late
            val hours = diffMs / (1000 * 60 * 60)
            val minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60)
            when {
                hours > 0 -> "Terlambat ${hours} jam ${minutes} menit"
                minutes > 0 -> "Terlambat ${minutes} menit"
                else -> "Terlambat kurang dari 1 menit"
            }
        }
    }

    private fun parseDate2(dateStr: String): Date? = parseDate(dateStr)
}
