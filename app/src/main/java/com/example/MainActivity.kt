package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AssignSeatDialog
import com.example.ui.components.LibraryTopBar
import com.example.ui.components.StudentDetailDialog
import com.example.ui.components.StudentEditDialog
import com.example.ui.components.StudentQrCardDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.SeatsScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    SEATS,
    STUDENTS,
    SCAN,
    REPORTS,
    PAYMENTS,
    ABOUT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                InspireLibraryApp()
            }
        }
    }
}

@Composable
fun InspireLibraryApp(
    viewModel: LibraryViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ViewModel State collection
    val students by viewModel.students.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val payments by viewModel.monthlyPayments.collectAsStateWithLifecycle()

    val selectedSeatNumber by viewModel.selectedSeat.collectAsStateWithLifecycle()
    val assigningSeatNumber by viewModel.assigningSeatNumber.collectAsStateWithLifecycle()
    val studentToEdit by viewModel.studentToEdit.collectAsStateWithLifecycle()
    val qrCardStudent by viewModel.qrCardStudent.collectAsStateWithLifecycle()
    val lastPunchResult by viewModel.lastPunchResult.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // Back handling
    if (currentTab != ScreenTab.HOME) {
        BackHandler {
            currentTab = ScreenTab.HOME
        }
    }

    // Status message snackbar
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearStatusMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            LibraryTopBar(
                onQuickScanClick = { currentTab = ScreenTab.SCAN },
                onInfoClick = { currentTab = ScreenTab.ABOUT }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavyDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { currentTab = ScreenTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.65f),
                        unselectedTextColor = Color.White.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SEATS,
                    onClick = { currentTab = ScreenTab.SEATS },
                    icon = { Icon(Icons.Default.Chair, contentDescription = "66 Seats") },
                    label = { Text("66 Seats", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.65f),
                        unselectedTextColor = Color.White.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.testTag("nav_tab_seats")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.STUDENTS,
                    onClick = { currentTab = ScreenTab.STUDENTS },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Students") },
                    label = { Text("Students", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.65f),
                        unselectedTextColor = Color.White.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.testTag("nav_tab_students")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SCAN,
                    onClick = { currentTab = ScreenTab.SCAN },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR") },
                    label = { Text("Scan QR", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.65f),
                        unselectedTextColor = Color.White.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.testTag("nav_tab_scan")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.REPORTS,
                    onClick = { currentTab = ScreenTab.REPORTS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("Report", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.65f),
                        unselectedTextColor = Color.White.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.testTag("nav_tab_reports")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.PAYMENTS,
                    onClick = { currentTab = ScreenTab.PAYMENTS },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Payments") },
                    label = { Text("Payment", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = Color.White.copy(alpha = 0.65f),
                        unselectedTextColor = Color.White.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier.testTag("nav_tab_payments")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == ScreenTab.SEATS) {
                FloatingActionButton(
                    onClick = { currentTab = ScreenTab.SCAN },
                    containerColor = EmeraldPresent,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_scan_attendance")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        stats = stats,
                        recentLogs = todayLogs,
                        students = students,
                        payments = payments,
                        onNavigateToSeats = { currentTab = ScreenTab.SEATS },
                        onNavigateToScan = { currentTab = ScreenTab.SCAN },
                        onNavigateToPayments = { currentTab = ScreenTab.PAYMENTS },
                        onNavigateToReports = { currentTab = ScreenTab.REPORTS }
                    )
                }

                ScreenTab.SEATS -> {
                    SeatsScreen(
                        students = students,
                        todayLogs = todayLogs,
                        payments = payments,
                        onSeatClick = { seatNumber ->
                            val s = students.find { it.seatNumber == seatNumber }
                            if (s != null) {
                                viewModel.selectSeat(seatNumber)
                            } else {
                                viewModel.openAssignModal(seatNumber)
                            }
                        }
                    )
                }

                ScreenTab.STUDENTS -> {
                    StudentsScreen(
                        students = students,
                        payments = payments,
                        onStudentClick = { student ->
                            viewModel.selectSeat(student.seatNumber)
                        },
                        onEditStudent = { student ->
                            viewModel.openEditStudent(student)
                        },
                        onShowQrCard = { student ->
                            viewModel.showQrCard(student)
                        },
                        onEnrollClick = {
                            val firstAvailableSeat = (1..66).firstOrNull { seatNum ->
                                students.none { it.seatNumber == seatNum }
                            } ?: 1
                            viewModel.openAssignModal(firstAvailableSeat)
                        }
                    )
                }

                ScreenTab.SCAN -> {
                    ScanScreen(
                        students = students,
                        recentLogs = todayLogs,
                        lastResult = lastPunchResult,
                        onPunchCode = { code -> viewModel.punchAttendance(code) },
                        onClearResult = { viewModel.clearPunchResult() }
                    )
                }

                ScreenTab.REPORTS -> {
                    ReportScreen(
                        logs = todayLogs,
                        onResetAllData = { viewModel.resetAllData() }
                    )
                }

                ScreenTab.PAYMENTS -> {
                    PaymentScreen(
                        students = students,
                        payments = payments,
                        onRecordPayment = { student, amount, status, mode ->
                            viewModel.recordPayment(
                                student = student,
                                amount = amount,
                                status = status,
                                mode = mode
                            )
                        }
                    )
                }

                ScreenTab.ABOUT -> {
                    AboutScreen()
                }
            }
        }
    }

    // Modal: Assign Seat Dialog
    if (assigningSeatNumber != null) {
        AssignSeatDialog(
            seatNumber = assigningSeatNumber!!,
            onDismiss = { viewModel.closeAssignModal() },
            onConfirm = { name, phone, email, enrollmentDate, fee, shift, examTarget, notes, isPaid ->
                viewModel.assignSeat(
                    seatNumber = assigningSeatNumber!!,
                    name = name,
                    phone = phone,
                    email = email,
                    enrollmentDate = enrollmentDate,
                    monthlyFee = fee,
                    shift = shift,
                    examTarget = examTarget,
                    notes = notes,
                    isPaid = isPaid
                )
            }
        )
    }

    // Modal: Edit Student Dialog
    if (studentToEdit != null) {
        val freeSeats = (1..66).filter { seatNum -> students.none { it.seatNumber == seatNum } }
        StudentEditDialog(
            student = studentToEdit!!,
            availableSeats = freeSeats,
            onDismiss = { viewModel.closeEditStudent() },
            onSave = { updatedStudent, newSeatNumber ->
                viewModel.updateStudent(updatedStudent)
                if (newSeatNumber != updatedStudent.seatNumber) {
                    viewModel.switchSeat(updatedStudent, newSeatNumber)
                }
            }
        )
    }

    // Modal: Student Detail Dialog (when occupied seat clicked)
    if (selectedSeatNumber != null) {
        val student = students.find { it.seatNumber == selectedSeatNumber }
        if (student != null) {
            val studentLog = todayLogs.find { it.studentId == student.id }
            val payment = payments.find { it.studentId == student.id }

            StudentDetailDialog(
                student = student,
                currentLog = studentLog,
                payment = payment,
                onDismiss = { viewModel.dismissSeatDetail() },
                onPunchAttendance = { studentId ->
                    viewModel.punchAttendance(studentId)
                    viewModel.dismissSeatDetail()
                },
                onTogglePayment = { s, markPaid ->
                    viewModel.recordPayment(
                        student = s,
                        amount = s.monthlyFee,
                        status = if (markPaid) "PAID" else "DUE",
                        mode = "UPI"
                    )
                },
                onEditStudent = { s ->
                    viewModel.openEditStudent(s)
                },
                onShowQrCard = { s ->
                    viewModel.dismissSeatDetail()
                    viewModel.showQrCard(s)
                },
                onVacateSeat = { seatNumber ->
                    viewModel.vacateSeat(seatNumber)
                }
            )
        }
    }

    // Modal: Student QR Card Dialog
    if (qrCardStudent != null) {
        StudentQrCardDialog(
            student = qrCardStudent!!,
            onDismiss = { viewModel.closeQrCard() }
        )
    }
}
