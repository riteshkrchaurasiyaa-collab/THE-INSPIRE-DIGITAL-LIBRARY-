package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import com.example.ui.components.CollectFeeDialog
import com.example.ui.components.FeeReceiptDialog
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonDue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.DateUtils
import com.example.util.ExportUtils

enum class PaymentFilter {
    ALL,
    DUE_FLAGGED,
    PAID
}

@Composable
fun PaymentScreen(
    students: List<StudentEntity>,
    payments: List<PaymentEntity>,
    onRecordPayment: (student: StudentEntity, amount: Double, status: String, mode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(PaymentFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    // Modal states
    var feeStudentToCollect by remember { mutableStateOf<StudentEntity?>(null) }
    var receiptToShow by remember { mutableStateOf<Pair<StudentEntity, PaymentEntity>?>(null) }
    var showRateList by remember { mutableStateOf(false) }

    val currentMonthYear = DateUtils.getCurrentMonthYear()
    val currentMonthName = DateUtils.getCurrentMonthName()

    val paymentMap = remember(payments) {
        payments.associateBy { it.studentId }
    }

    val paidCount = students.count { s -> paymentMap[s.id]?.status == "PAID" }
    val dueCount = students.size - paidCount

    val totalCollected = students.sumOf { s ->
        val p = paymentMap[s.id]
        if (p?.status == "PAID") p.amount else 0.0
    }

    val totalPending = students.sumOf { s ->
        val p = paymentMap[s.id]
        if (p?.status != "PAID") (p?.amount ?: s.monthlyFee) else 0.0
    }

    val totalExpected = totalCollected + totalPending
    val collectionRate = if (totalExpected > 0) (totalCollected / totalExpected).toFloat() else 0f

    val filteredStudents = students.filter { s ->
        val p = paymentMap[s.id]
        val isPaid = p?.status == "PAID"

        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            s.name.lowercase().contains(q) ||
                    s.seatNumber.toString() == q ||
                    s.phone.contains(q) ||
                    s.id.lowercase().contains(q)
        }

        val matchesFilter = when (selectedFilter) {
            PaymentFilter.ALL -> true
            PaymentFilter.DUE_FLAGGED -> !isPaid
            PaymentFilter.PAID -> isPaid
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "💳 Monthly Revenue Dashboard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Text(
                        text = "Billing Period: $currentMonthName",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showRateList = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("Rate List", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            ExportUtils.exportPaymentsCsv(
                                context = context,
                                students = students,
                                payments = payments,
                                monthName = currentMonthName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val reportText = buildString {
                                appendLine("🏛️ The Inspire Digital Library - Monthly Revenue Report")
                                appendLine("Period: $currentMonthName")
                                appendLine("---------------------------------------")
                                appendLine("Total Expected Revenue: ₹${totalExpected.toInt()}")
                                appendLine("Total Collected Revenue: ₹${totalCollected.toInt()} ($paidCount Students)")
                                appendLine("Total Pending Dues: ₹${totalPending.toInt()} ($dueCount Students)")
                                appendLine("Collection Rate: ${(collectionRate * 100).toInt()}%")
                                appendLine("---------------------------------------")
                                appendLine("PENDING DUES BREAKDOWN:")
                                students.filter { paymentMap[it.id]?.status != "PAID" }.forEach { s ->
                                    appendLine("Seat #${s.seatNumber} | ${s.name} | Phone: ${s.phone} | Due: ₹${s.monthlyFee.toInt()}")
                                }
                            }
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Revenue Report"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Hero Monthly Revenue Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revenue_hero_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TOTAL MONTHLY REVENUE",
                        color = GoldPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "₹${totalCollected.toInt()}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 34.sp
                            )
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF102847))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(collectionRate * 100).toInt()}% COLLECTED",
                                color = EmeraldPresent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dual progress indicator
                    LinearProgressIndicator(
                        progress = { collectionRate },
                        color = EmeraldPresent,
                        trackColor = Color(0xFF334155),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Expected", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
                            Text("₹${totalExpected.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Paid Students", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
                            Text("$paidCount / ${students.size}", color = EmeraldPresent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending Dues", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
                            Text("₹${totalPending.toInt()}", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Flagged Pending Dues Warning Banner (if any)
        if (dueCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = CrimsonDue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$dueCount Students Flagged with Pending Fees",
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonDark,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Total ₹${totalPending.toInt()} pending collection for $currentMonthName",
                                    color = Color(0xFF991B1B),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = { selectedFilter = PaymentFilter.DUE_FLAGGED },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonDue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("View Due", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name, seat #, or phone...", fontSize = 12.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == PaymentFilter.ALL,
                    onClick = { selectedFilter = PaymentFilter.ALL },
                    label = { Text("All (${students.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == PaymentFilter.DUE_FLAGGED,
                    onClick = { selectedFilter = PaymentFilter.DUE_FLAGGED },
                    label = { Text("🚩 Flagged Pending ($dueCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonDue,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == PaymentFilter.PAID,
                    onClick = { selectedFilter = PaymentFilter.PAID },
                    label = { Text("🟢 Paid / Cleared ($paidCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPresent,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Student Fee Records List
        if (filteredStudents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No fee records match this filter.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredStudents, key = { it.id }) { student ->
                val payment = paymentMap[student.id]
                val isPaid = payment?.status == "PAID"
                val feeAmount = payment?.amount ?: student.monthlyFee

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isPaid) Color.White else Color(0xFFFFFDFD)),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isPaid) 1.dp else 1.5.dp,
                        color = if (isPaid) Color(0xFFE2E8F0) else Color(0xFFFCA5A5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isPaid) 1.dp else 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPaid) NavyPrimary else CrimsonDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${student.seatNumber}",
                                        color = if (isPaid) GoldPrimary else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        fontSize = 14.5.sp
                                    )
                                    Text(
                                        text = "Shift: ${student.shift} • ID: ${student.id}",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            // Status Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isPaid) "PAID" else "PENDING",
                                    color = if (isPaid) EmeraldDark else CrimsonDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Amount & Payment details strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Monthly Plan Fee: ", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                Text("₹${feeAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = NavyPrimary)
                            }

                            if (isPaid) {
                                Text(
                                    text = "Paid on ${payment?.paymentDate} (${payment?.paymentMode})",
                                    fontSize = 11.sp,
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "⚠️ Due for $currentMonthName",
                                    fontSize = 11.sp,
                                    color = CrimsonDue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isPaid) {
                                // View Receipt Button
                                OutlinedButton(
                                    onClick = {
                                        if (payment != null) {
                                            receiptToShow = Pair(student, payment)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View Receipt", fontSize = 11.5.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onRecordPayment(student, feeAmount, "DUE", "UPI")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .height(34.dp)
                                ) {
                                    Text("Set Due", fontSize = 11.5.sp, color = Color(0xFF64748B))
                                }
                            } else {
                                // WhatsApp / SMS Fee Reminder Button
                                OutlinedButton(
                                    onClick = {
                                        val reminderText = "Dear ${student.name},\nThis is a gentle reminder from The Inspire Digital Library - Self Study Point for your Seat #${student.seatNumber} monthly fee of ₹${feeAmount.toInt()} for $currentMonthName.\nPlease clear your dues via UPI or at the library desk. Thank you!"
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, reminderText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Send Fee Reminder to ${student.name}"))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = CrimsonDue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Send Reminder", fontSize = 11.sp, color = CrimsonDark, fontWeight = FontWeight.SemiBold)
                                }

                                // Collect Fee Button
                                Button(
                                    onClick = { feeStudentToCollect = student },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Collect Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Collect Fee Dialog
    if (feeStudentToCollect != null) {
        CollectFeeDialog(
            student = feeStudentToCollect!!,
            monthName = currentMonthName,
            onDismiss = { feeStudentToCollect = null },
            onConfirm = { amount, mode ->
                val s = feeStudentToCollect!!
                onRecordPayment(s, amount, "PAID", mode)
                feeStudentToCollect = null
            }
        )
    }

    // Modal: Fee Receipt Dialog
    if (receiptToShow != null) {
        val (student, payment) = receiptToShow!!
        FeeReceiptDialog(
            student = student,
            payment = payment,
            onDismiss = { receiptToShow = null }
        )
    }

    // Modal: Official Library Rate List Dialog
    if (showRateList) {
        com.example.ui.components.RateListDialog(
            onDismiss = { showRateList = false }
        )
    }
}
