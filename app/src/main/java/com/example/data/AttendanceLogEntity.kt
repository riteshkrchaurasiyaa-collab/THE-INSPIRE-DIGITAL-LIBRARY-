package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_logs")
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val studentId: String,
    val studentName: String,
    val seatNumber: Int,
    val dateStr: String, // YYYY-MM-DD
    val entryTime: String,
    val exitTime: String? = null,
    val entryTimestamp: Long = System.currentTimeMillis(),
    val exitTimestamp: Long? = null,
    val durationHours: Double? = null
)
