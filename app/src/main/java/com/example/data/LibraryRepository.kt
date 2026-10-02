package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class PunchResult {
    data class EntrySuccess(val student: StudentEntity, val time: String) : PunchResult()
    data class ExitSuccess(val student: StudentEntity, val time: String, val durationHours: Double) : PunchResult()
    data class StudentNotFound(val query: String) : PunchResult()
    data class Error(val message: String) : PunchResult()
}

class LibraryRepository(private val dao: LibraryDao) {

    val activeStudents: Flow<List<StudentEntity>> = dao.getAllActiveStudents()
    val allLogs: Flow<List<AttendanceLogEntity>> = dao.getAllAttendanceLogs()

    fun getTodayLogs(dateStr: String): Flow<List<AttendanceLogEntity>> = dao.getTodayLogs(dateStr)

    fun getCurrentlyPresentLogs(dateStr: String): Flow<List<AttendanceLogEntity>> =
        dao.getCurrentlyPresentLogs(dateStr)

    fun getPaymentsForMonth(monthYear: String): Flow<List<PaymentEntity>> =
        dao.getPaymentsForMonth(monthYear)

    suspend fun getStudentBySeat(seatNumber: Int): StudentEntity? =
        dao.findStudentBySeatSync(seatNumber)

    suspend fun getStudentById(id: String): StudentEntity? =
        dao.findStudentByIdSync(id)

    suspend fun assignStudent(
        seatNumber: Int,
        name: String,
        phone: String,
        email: String = "",
        enrollmentDate: Long = System.currentTimeMillis(),
        feeAmount: Double = 800.0,
        shift: String = "Full Day",
        examTarget: String = "UPSC / State PCS",
        notes: String = "",
        initialPaymentStatus: String = "PAID"
    ): StudentEntity {
        val studentId = "SEAT%02d".format(seatNumber)
        val student = StudentEntity(
            id = studentId,
            name = name.trim(),
            phone = phone.trim(),
            email = email.trim(),
            seatNumber = seatNumber,
            assignedDate = enrollmentDate,
            monthlyFee = feeAmount,
            shift = shift,
            examTarget = examTarget,
            status = "ACTIVE",
            notes = notes.trim()
        )
        dao.insertStudent(student)

        // Also record/update current month payment
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val payment = PaymentEntity(
            studentId = studentId,
            studentName = student.name,
            seatNumber = seatNumber,
            monthYear = currentMonthYear,
            monthName = currentMonthName,
            amount = feeAmount,
            status = initialPaymentStatus,
            paymentDate = if (initialPaymentStatus == "PAID") todayStr else "",
            paymentMode = "UPI",
            receiptNumber = "RCPT-${seatNumber}-${System.currentTimeMillis() % 10000}"
        )
        dao.insertPayment(payment)

        return student
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student)
    }

    suspend fun changeStudentSeat(student: StudentEntity, newSeatNumber: Int): StudentEntity {
        // First delete or vacate old seat record
        dao.deleteStudentBySeat(student.seatNumber)
        val newId = "SEAT%02d".format(newSeatNumber)
        val updatedStudent = student.copy(
            id = newId,
            seatNumber = newSeatNumber
        )
        dao.insertStudent(updatedStudent)
        return updatedStudent
    }

    suspend fun vacateSeat(seatNumber: Int) {
        dao.deleteStudentBySeat(seatNumber)
    }

    suspend fun punchAttendance(identifier: String): PunchResult {
        val clean = identifier.trim()
        if (clean.isEmpty()) {
            return PunchResult.Error("QR or Student ID cannot be empty.")
        }

        // Try to match student by ID, or if numeric, by seat number
        var student = dao.findStudentByIdSync(clean)
        if (student == null) {
            val asNum = clean.toIntOrNull()
            if (asNum != null) {
                student = dao.findStudentBySeatSync(asNum)
            } else if (clean.uppercase().startsWith("SEAT")) {
                val numPart = clean.substring(4).toIntOrNull()
                if (numPart != null) {
                    student = dao.findStudentBySeatSync(numPart)
                }
            }
        }

        if (student == null) {
            return PunchResult.StudentNotFound(clean)
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = System.currentTimeMillis()
        val currentTimeStr = timeFormat.format(Date(now))

        // Check if there is an active session (i.e. entry without exit) today
        val activeSession = dao.getActiveSession(student.id, todayDate)
        if (activeSession != null) {
            // Punch OUT
            val durationMillis = now - activeSession.entryTimestamp
            val durationHours = String.format(Locale.US, "%.2f", durationMillis / 3600000.0).toDoubleOrNull() ?: 0.0
            val updated = activeSession.copy(
                exitTime = currentTimeStr,
                exitTimestamp = now,
                durationHours = durationHours
            )
            dao.updateAttendanceLog(updated)
            return PunchResult.ExitSuccess(student, currentTimeStr, durationHours)
        } else {
            // Punch IN
            val newLog = AttendanceLogEntity(
                studentId = student.id,
                studentName = student.name,
                seatNumber = student.seatNumber,
                dateStr = todayDate,
                entryTime = currentTimeStr,
                entryTimestamp = now
            )
            dao.insertAttendanceLog(newLog)
            return PunchResult.EntrySuccess(student, currentTimeStr)
        }
    }

    suspend fun updatePaymentStatus(payment: PaymentEntity) {
        dao.updatePayment(payment)
    }

    suspend fun markPayment(
        student: StudentEntity,
        monthYear: String,
        monthName: String,
        amount: Double,
        status: String,
        mode: String
    ) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = dao.getStudentPayment(student.id, monthYear)
        if (existing != null) {
            val updated = existing.copy(
                status = status,
                amount = amount,
                paymentMode = mode,
                paymentDate = if (status == "PAID") todayStr else ""
            )
            dao.updatePayment(updated)
        } else {
            val newPayment = PaymentEntity(
                studentId = student.id,
                studentName = student.name,
                seatNumber = student.seatNumber,
                monthYear = monthYear,
                monthName = monthName,
                amount = amount,
                status = status,
                paymentDate = if (status == "PAID") todayStr else "",
                paymentMode = mode,
                receiptNumber = "RCPT-${student.seatNumber}-${System.currentTimeMillis() % 10000}"
            )
            dao.insertPayment(newPayment)
        }
    }

    suspend fun clearAllData() {
        dao.deleteAllStudents()
        dao.deleteAllLogs()
        dao.deleteAllPayments()
    }
}
