package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AttendanceLogEntity
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonDue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.viewmodel.LibraryStats
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import java.util.Calendar

data class DeadlineFlag(
    val student: StudentEntity,
    val amountDue: Double,
    val dueStatusText: String, // e.g. "Due in 2 days", "Due Today", "Overdue"
    val isUrgent: Boolean
)

@Composable
fun HomeScreen(
    stats: LibraryStats,
    recentLogs: List<AttendanceLogEntity>,
    students: List<StudentEntity> = emptyList(),
    payments: List<PaymentEntity> = emptyList(),
    onNavigateToSeats: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMonthName = DateUtils.getCurrentMonthName()

    val paymentMap = remember(payments) {
        payments.associateBy { it.studentId }
    }

    // Calculate upcoming payment deadlines for students whose current month fee is unpaid
    val flaggedDeadlines = remember(students, payments) {
        val todayCal = Calendar.getInstance()
        val currentDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)

        students.filter { s -> paymentMap[s.id]?.status != "PAID" }.map { student ->
            // Use enrollment day as the monthly billing renewal day
            val enrollCal = Calendar.getInstance().apply { timeInMillis = student.assignedDate }
            val billingDay = enrollCal.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)

            val daysDiff = billingDay - currentDayOfMonth
            val (statusText, urgent) = when {
                daysDiff < 0 -> Pair("Overdue by ${-daysDiff} days", true)
                daysDiff == 0 -> Pair("Due Today", true)
                daysDiff in 1..3 -> Pair("Due in $daysDiff days", true)
                else -> Pair("Due on ${billingDay}th $currentMonthName", false)
            }

            DeadlineFlag(
                student = student,
                amountDue = paymentMap[student.id]?.amount ?: student.monthlyFee,
                dueStatusText = statusText,
                isUrgent = urgent
            )
        }.sortedWith(compareBy({ !it.isUrgent }, { it.dueStatusText }))
    }

    // Permission launcher for Android 13+ Notification
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                triggerSystemNotification(context, flaggedDeadlines, currentMonthName)
            } else {
                Toast.makeText(context, "Notification permission is required to post system reminders.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Premium Hero Banner with Library Emblem
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF101D36)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_inspire_emblem),
                            contentDescription = "The Inspire Digital Library",
                            modifier = Modifier.size(66.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "The Inspire Digital Library",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Near Maa Durga Treders Desri Bibhutipur",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = DateUtils.getTodayFormatted(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // 4 Key Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Capacity",
                        value = "${stats.totalSeats}",
                        subtitle = "66 Study Desks",
                        icon = Icons.Default.Chair,
                        iconTint = NavyPrimary,
                        backgroundColor = Color(0xFFF1F5F9),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Occupied",
                        value = "${stats.occupiedSeats}",
                        subtitle = "${stats.vacantSeats} Available",
                        icon = Icons.Default.Group,
                        iconTint = Color(0xFF2563EB),
                        backgroundColor = Color(0xFFEFF6FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Present Now",
                        value = "${stats.currentlyPresent}",
                        subtitle = "Active in library",
                        icon = Icons.Default.MeetingRoom,
                        iconTint = EmeraldPresent,
                        backgroundColor = Color(0xFFECFDF5),
                        badge = "LIVE IN",
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Today's Visits",
                        value = "${stats.todayVisits}",
                        subtitle = "Total Check-ins",
                        icon = Icons.Default.Login,
                        iconTint = Color(0xFFD97706),
                        backgroundColor = Color(0xFFFFFBEB),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Automated Payment Deadlines Notification Card (FEATURE)
        if (flaggedDeadlines.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_deadlines_notification_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = CrimsonDue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Payment Deadlines Alert",
                                        fontWeight = FontWeight.Bold,
                                        color = CrimsonDark,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = "${flaggedDeadlines.size} students flagged with upcoming/due fees",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            // Trigger System Reminder Button
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !NotificationHelper.hasNotificationPermission(context)
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        triggerSystemNotification(context, flaggedDeadlines, currentMonthName)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonDue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remind", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Flagged items list
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            flaggedDeadlines.take(3).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (item.isUrgent) Color(0xFFFFF1F2) else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (item.isUrgent) Color(0xFFFECDD3) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NavyPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.student.seatNumber}",
                                                color = GoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                text = item.student.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = NavyPrimary
                                            )
                                            Text(
                                                text = "₹${item.amountDue.toInt()} • ${item.dueStatusText}",
                                                color = if (item.isUrgent) CrimsonDue else Color(0xFF64748B),
                                                fontWeight = if (item.isUrgent) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // 1-tap WhatsApp reminder
                                    OutlinedButton(
                                        onClick = {
                                            val reminder = "Dear ${item.student.name},\nThis is a notification from The Inspire Digital Library for Seat #${item.student.seatNumber}. Your fee deadline is ${item.dueStatusText} (Amount: ₹${item.amountDue.toInt()}/- for $currentMonthName).\nPlease clear dues via UPI or at the library counter. Thank you!"
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, reminder)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Send Fee Deadline to ${item.student.name}"))
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = CrimsonDue, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Alert", fontSize = 10.sp, color = CrimsonDark)
                                    }
                                }
                            }
                        }

                        if (flaggedDeadlines.size > 3) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+ ${flaggedDeadlines.size - 3} more students with upcoming deadlines",
                                fontSize = 11.5.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(onClick = onNavigateToPayments)
                            )
                        }
                    }
                }
            }
        }

        // Primary Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToSeats,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("home_open_seats_button")
                ) {
                    Icon(Icons.Default.Chair, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("66 Seats Grid", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToScan,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("home_open_scanner_button")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Attendance", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Monthly Fees Quick Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToPayments)
                    .testTag("home_fees_summary_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Monthly Fees ($currentMonthName)",
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.paidCount} Paid • ${stats.dueCount} Pending Dues",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View",
                            color = NavyPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Today's Live Attendance Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Today's Attendance Feed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                Text(
                    text = "View All",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onNavigateToReports)
                )
            }
        }

        if (recentLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No attendance recorded yet today.\nScan student QR code or punch ID to record entry.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(recentLogs.take(6)) { log ->
                val isCurrentlyIn = log.exitTimestamp == null
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrentlyIn) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCurrentlyIn) Icons.Default.Login else Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = if (isCurrentlyIn) EmeraldPresent else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = log.studentName,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Seat #${log.seatNumber} • IN: ${log.entryTime}" +
                                            if (log.exitTime != null) " • OUT: ${log.exitTime}" else "",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        if (isCurrentlyIn) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldPresent)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "IN",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (log.durationHours != null) {
                            Text(
                                text = "${log.durationHours} hrs",
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun triggerSystemNotification(
    context: android.content.Context,
    flaggedDeadlines: List<DeadlineFlag>,
    monthName: String
) {
    if (flaggedDeadlines.isEmpty()) {
        Toast.makeText(context, "All students are paid! No pending deadlines.", Toast.LENGTH_SHORT).show()
        return
    }

    val title = "⚠️ Fee Payment Deadline Alert (${flaggedDeadlines.size} Students)"
    val message = "${flaggedDeadlines.size} students have upcoming or overdue library fee deadlines for $monthName."
    val details = flaggedDeadlines.map { "Seat #${it.student.seatNumber} ${it.student.name}: ₹${it.amountDue.toInt()} (${it.dueStatusText})" }

    val success = NotificationHelper.sendFeeDeadlineNotification(
        context = context,
        title = title,
        message = message,
        detailsList = details
    )

    if (success) {
        Toast.makeText(context, "System reminder triggered for ${flaggedDeadlines.size} students!", Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(context, "Please enable notification permissions to receive system reminders.", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }

                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldPresent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = badge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyPrimary,
                    fontSize = 26.sp
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    fontSize = 11.5.sp
                )
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            )
        }
    }
}
