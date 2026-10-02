package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getTodayFormatted(): String {
        return SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    fun getCurrentMonthYear(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun getCurrentMonthName(): String {
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatRupees(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(amount).replace("₹", "₹ ")
    }
}
