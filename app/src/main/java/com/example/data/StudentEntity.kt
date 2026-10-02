package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val id: String, // e.g. "SEAT01", "SEAT02" or custom ID
    val name: String,
    val phone: String = "",
    val email: String = "",
    val seatNumber: Int, // 1..66 linked directly to their assigned seat
    val assignedDate: Long = System.currentTimeMillis(), // enrollment date
    val monthlyFee: Double = 800.0,
    val shift: String = "Full Day", // "Morning", "Evening", "Full Day", "24x7 Self Study"
    val examTarget: String = "UPSC / State PCS",
    val status: String = "ACTIVE",
    val notes: String = ""
)
