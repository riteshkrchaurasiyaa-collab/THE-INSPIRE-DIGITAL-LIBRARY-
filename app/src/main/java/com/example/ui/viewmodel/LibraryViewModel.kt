package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceLogEntity
import com.example.data.LibraryRepository
import com.example.data.PaymentEntity
import com.example.data.PunchResult
import com.example.data.StudentEntity
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LibraryStats(
    val totalSeats: Int = 66,
    val occupiedSeats: Int = 0,
    val vacantSeats: Int = 66,
    val currentlyPresent: Int = 0,
    val todayVisits: Int = 0,
    val paidCount: Int = 0,
    val dueCount: Int = 0,
    val totalRevenueCollected: Double = 0.0,
    val totalPendingAmount: Double = 0.0
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LibraryRepository
    val todayDateStr = DateUtils.getTodayDateStr()
    val currentMonthYear = DateUtils.getCurrentMonthYear()

    init {
        val db = AppDatabase.getInstance(application)
        repository = LibraryRepository(db.libraryDao())
        seedInitialDemoDataIfEmpty()
    }

    val students: StateFlow<List<StudentEntity>> = repository.activeStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<AttendanceLogEntity>> = repository.getTodayLogs(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<AttendanceLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyPayments: StateFlow<List<PaymentEntity>> = repository.getPaymentsForMonth(currentMonthYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined library stats
    val stats: StateFlow<LibraryStats> = combine(
        students,
        todayLogs,
        monthlyPayments
    ) { stuList, logs, payments ->
        val occupied = stuList.size
        val vacant = (66 - occupied).coerceAtLeast(0)
        val presentNow = logs.count { it.exitTimestamp == null }
        val visits = logs.size

        val paymentMap = payments.associateBy { it.studentId }
        var paid = 0
        var due = 0
        var revenue = 0.0
        var pending = 0.0

        for (s in stuList) {
            val p = paymentMap[s.id]
            if (p != null && p.status == "PAID") {
                paid++
                revenue += p.amount
            } else {
                due++
                pending += (p?.amount ?: s.monthlyFee)
            }
        }

        LibraryStats(
            totalSeats = 66,
            occupiedSeats = occupied,
            vacantSeats = vacant,
            currentlyPresent = presentNow,
            todayVisits = visits,
            paidCount = paid,
            dueCount = due,
            totalRevenueCollected = revenue,
            totalPendingAmount = pending
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryStats())

    // UI Dialog & Modal States
    private val _selectedSeat = MutableStateFlow<Int?>(null)
    val selectedSeat: StateFlow<Int?> = _selectedSeat.asStateFlow()

    private val _assigningSeatNumber = MutableStateFlow<Int?>(null)
    val assigningSeatNumber: StateFlow<Int?> = _assigningSeatNumber.asStateFlow()

    private val _studentToEdit = MutableStateFlow<StudentEntity?>(null)
    val studentToEdit: StateFlow<StudentEntity?> = _studentToEdit.asStateFlow()

    private val _qrCardStudent = MutableStateFlow<StudentEntity?>(null)
    val qrCardStudent: StateFlow<StudentEntity?> = _qrCardStudent.asStateFlow()

    private val _lastPunchResult = MutableStateFlow<PunchResult?>(null)
    val lastPunchResult: StateFlow<PunchResult?> = _lastPunchResult.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectSeat(seatNumber: Int) {
        _selectedSeat.value = seatNumber
    }

    fun dismissSeatDetail() {
        _selectedSeat.value = null
    }

    fun openAssignModal(seatNumber: Int) {
        _selectedSeat.value = null
        _assigningSeatNumber.value = seatNumber
    }

    fun closeAssignModal() {
        _assigningSeatNumber.value = null
    }

    fun openEditStudent(student: StudentEntity) {
        _selectedSeat.value = null
        _studentToEdit.value = student
    }

    fun closeEditStudent() {
        _studentToEdit.value = null
    }

    fun showQrCard(student: StudentEntity) {
        _qrCardStudent.value = student
    }

    fun closeQrCard() {
        _qrCardStudent.value = null
    }

    fun clearPunchResult() {
        _lastPunchResult.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun assignSeat(
        seatNumber: Int,
        name: String,
        phone: String,
        email: String = "",
        enrollmentDate: Long = System.currentTimeMillis(),
        monthlyFee: Double,
        shift: String,
        examTarget: String = "UPSC / State PCS",
        notes: String = "",
        isPaid: Boolean
    ) {
        viewModelScope.launch {
            val student = repository.assignStudent(
                seatNumber = seatNumber,
                name = name,
                phone = phone,
                email = email,
                enrollmentDate = enrollmentDate,
                feeAmount = monthlyFee,
                shift = shift,
                examTarget = examTarget,
                notes = notes,
                initialPaymentStatus = if (isPaid) "PAID" else "DUE"
            )
            _assigningSeatNumber.value = null
            _statusMessage.value = "Student ${student.name} enrolled & assigned to Seat #$seatNumber."
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _studentToEdit.value = null
            _statusMessage.value = "Student details for ${student.name} updated."
        }
    }

    fun switchSeat(student: StudentEntity, newSeatNumber: Int) {
        viewModelScope.launch {
            val updated = repository.changeStudentSeat(student, newSeatNumber)
            _selectedSeat.value = null
            _statusMessage.value = "${student.name} moved from Seat #${student.seatNumber} to Seat #$newSeatNumber."
        }
    }

    fun vacateSeat(seatNumber: Int) {
        viewModelScope.launch {
            repository.vacateSeat(seatNumber)
            _selectedSeat.value = null
            _statusMessage.value = "Seat $seatNumber has been vacated and made available."
        }
    }

    fun punchAttendance(rawCode: String) {
        viewModelScope.launch {
            val result = repository.punchAttendance(rawCode)
            _lastPunchResult.value = result
            when (result) {
                is PunchResult.EntrySuccess -> {
                    _statusMessage.value = "ENTRY: ${result.student.name} (Seat ${result.student.seatNumber}) at ${result.time}"
                }
                is PunchResult.ExitSuccess -> {
                    _statusMessage.value = "EXIT: ${result.student.name} (Seat ${result.student.seatNumber}) - Study: ${result.durationHours} hrs"
                }
                is PunchResult.StudentNotFound -> {
                    _statusMessage.value = "Student QR ID '$rawCode' not recognized."
                }
                is PunchResult.Error -> {
                    _statusMessage.value = result.message
                }
            }
        }
    }

    fun recordPayment(
        student: StudentEntity,
        amount: Double,
        status: String,
        mode: String
    ) {
        viewModelScope.launch {
            repository.markPayment(
                student = student,
                monthYear = currentMonthYear,
                monthName = DateUtils.getCurrentMonthName(),
                amount = amount,
                status = status,
                mode = mode
            )
            _statusMessage.value = "Payment updated for ${student.name}: $status (₹$amount)"
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _statusMessage.value = "All student and attendance data has been reset."
        }
    }

    private fun seedInitialDemoDataIfEmpty() {
        viewModelScope.launch {
            val existing = repository.getStudentBySeat(1)
            if (existing != null) return@launch

            // Seed realistic demo students for The Inspire Digital Library
            val sampleStudents = listOf(
                Tuple5(1, "Aarav Sharma", "9876543210", "aarav@inspire.org", "UPSC Civil Services"),
                Tuple5(2, "Pooja Verma", "9823412345", "pooja.v@inspire.org", "State PCS"),
                Tuple5(5, "Rohan Gupta", "9711223344", "rohan.g@gmail.com", "SSC CGL"),
                Tuple5(6, "Sneha Patel", "9988776655", "sneha.p@gmail.com", "Banking PO / SBI"),
                Tuple5(12, "Vikram Singh", "9123456780", "vikram.s@outlook.com", "CA Final / IPCC"),
                Tuple5(15, "Ananya Mishra", "9345612789", "ananya.m@gmail.com", "NEET PG"),
                Tuple5(21, "Aditya Yadav", "9456123478", "aditya.y@gmail.com", "JEE Advanced"),
                Tuple5(22, "Divya Chauhan", "9567123489", "divya.c@gmail.com", "Judiciary / Law"),
                Tuple5(30, "Mohd. Tariq", "9678123490", "tariq.m@gmail.com", "UPSC Civil Services"),
                Tuple5(33, "Kavita Joshi", "9789123401", "kavita.j@gmail.com", "Defense / CDS / AFCAT"),
                Tuple5(45, "Rahul Meena", "9890123412", "rahul.m@gmail.com", "State PCS / Police Sub-Inspector"),
                Tuple5(50, "Priya Nair", "9901123423", "priya.n@gmail.com", "GATE / Engineering"),
                Tuple5(60, "Deepak Kumar", "9012123434", "deepak.k@gmail.com", "SSC CGL / CHSL"),
                Tuple5(66, "Simran Kaur", "9123123445", "simran.k@gmail.com", "UPSC Civil Services")
            )

            val shifts = listOf("Full Day", "Morning (6 AM - 2 PM)", "Evening (2 PM - 10 PM)", "24x7 Self Study")
            val enrollmentDates = listOf(
                System.currentTimeMillis() - 45L * 86400000L,
                System.currentTimeMillis() - 30L * 86400000L,
                System.currentTimeMillis() - 20L * 86400000L,
                System.currentTimeMillis() - 10L * 86400000L,
                System.currentTimeMillis() - 5L * 86400000L
            )

            for ((index, item) in sampleStudents.withIndex()) {
                val shift = shifts[item.seat % shifts.size]
                val isPaid = item.seat % 3 != 0
                val fee = if (shift.contains("24x7")) 1000.0 else 800.0
                val enrollDate = enrollmentDates[index % enrollmentDates.size]

                repository.assignStudent(
                    seatNumber = item.seat,
                    name = item.name,
                    phone = item.phone,
                    email = item.email,
                    enrollmentDate = enrollDate,
                    feeAmount = fee,
                    shift = shift,
                    examTarget = item.exam,
                    initialPaymentStatus = if (isPaid) "PAID" else "DUE"
                )
            }

            // Also seed a couple of active checked-in sessions for today so UI feels immediately alive
            val activeSeats = listOf(1, 5, 21, 33, 66)
            for (s in activeSeats) {
                repository.punchAttendance("SEAT%02d".format(s))
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val seat: A,
    val name: B,
    val phone: C,
    val email: D,
    val exam: E
)
