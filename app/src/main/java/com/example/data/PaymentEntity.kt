package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val paymentId: Long = 0,
    val studentId: String,
    val studentName: String,
    val seatNumber: Int,
    val monthYear: String, // e.g. "2026-10"
    val monthName: String, // e.g. "October 2026"
    val amount: Double,
    val status: String, // "PAID", "DUE"
    val paymentDate: String = "",
    val paymentMode: String = "UPI", // "UPI", "Cash", "Card"
    val receiptNumber: String = ""
)
