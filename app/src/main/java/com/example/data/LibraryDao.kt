package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    // --- Students ---
    @Query("SELECT * FROM students WHERE status = 'ACTIVE' ORDER BY seatNumber ASC")
    fun getAllActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE seatNumber = :seatNumber AND status = 'ACTIVE' LIMIT 1")
    fun getStudentBySeat(seatNumber: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE seatNumber = :seatNumber AND status = 'ACTIVE' LIMIT 1")
    suspend fun findStudentBySeatSync(seatNumber: Int): StudentEntity?

    @Query("SELECT * FROM students WHERE LOWER(id) = LOWER(:id) AND status = 'ACTIVE' LIMIT 1")
    suspend fun findStudentByIdSync(id: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("UPDATE students SET status = 'VACATED' WHERE seatNumber = :seatNumber")
    suspend fun vacateSeat(seatNumber: Int)

    @Query("DELETE FROM students WHERE seatNumber = :seatNumber")
    suspend fun deleteStudentBySeat(seatNumber: Int)

    // --- Attendance Logs ---
    @Query("SELECT * FROM attendance_logs WHERE dateStr = :dateStr ORDER BY entryTimestamp DESC")
    fun getTodayLogs(dateStr: String): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs ORDER BY entryTimestamp DESC LIMIT 200")
    fun getAllAttendanceLogs(): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs WHERE studentId = :studentId AND dateStr = :dateStr AND exitTimestamp IS NULL ORDER BY entryTimestamp DESC LIMIT 1")
    suspend fun getActiveSession(studentId: String, dateStr: String): AttendanceLogEntity?

    @Query("SELECT * FROM attendance_logs WHERE dateStr = :dateStr AND exitTimestamp IS NULL")
    fun getCurrentlyPresentLogs(dateStr: String): Flow<List<AttendanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLogEntity): Long

    @Update
    suspend fun updateAttendanceLog(log: AttendanceLogEntity)

    @Query("DELETE FROM attendance_logs WHERE logId = :logId")
    suspend fun deleteAttendanceLog(logId: Long)

    // --- Payments ---
    @Query("SELECT * FROM payments WHERE monthYear = :monthYear ORDER BY seatNumber ASC")
    fun getPaymentsForMonth(monthYear: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY paymentId DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId AND monthYear = :monthYear LIMIT 1")
    suspend fun getStudentPayment(studentId: String, monthYear: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    // --- Management / Reset ---
    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("DELETE FROM attendance_logs")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()
}
