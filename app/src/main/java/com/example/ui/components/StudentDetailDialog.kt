package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AttendanceLogEntity
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonDue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.util.DateUtils

@Composable
fun StudentDetailDialog(
    student: StudentEntity,
    currentLog: AttendanceLogEntity?,
    payment: PaymentEntity?,
    onDismiss: () -> Unit,
    onPunchAttendance: (studentId: String) -> Unit,
    onTogglePayment: (student: StudentEntity, markPaid: Boolean) -> Unit,
    onEditStudent: (student: StudentEntity) -> Unit,
    onShowQrCard: (student: StudentEntity) -> Unit,
    onVacateSeat: (seatNumber: Int) -> Unit
) {
    val context = LocalContext.current
    var showVacateConfirm by remember { mutableStateOf(false) }
    val isPresentNow = currentLog != null && currentLog.exitTimestamp == null
    val isPaid = payment?.status == "PAID"

    if (showVacateConfirm) {
        AlertDialog(
            onDismissRequest = { showVacateConfirm = false },
            title = { Text("Vacate Seat #${student.seatNumber}?") },
            text = { Text("Are you sure you want to remove ${student.name} from Seat #${student.seatNumber}? This will unassign the student and make the desk available.") },
            confirmButton = {
                Button(
                    onClick = {
                        showVacateConfirm = false
                        onVacateSeat(student.seatNumber)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDue)
                ) {
                    Text("Yes, Vacate Seat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVacateConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("student_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar Initials
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(2).uppercase(),
                            color = GoldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 18.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SEAT #${student.seatNumber}",
                                    color = Color(0xFF92400E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "•  ID: ${student.id}",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = { onEditStudent(student) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Student", tint = NavyPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Student Details Information Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Contact Mobile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mobile Phone:", color = Color(0xFF64748B), fontSize = 12.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (student.phone.isNotBlank()) student.phone else "Not provided",
                                fontWeight = FontWeight.Medium,
                                color = NavyPrimary,
                                fontSize = 13.sp
                            )
                            if (student.phone.isNotBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Student",
                                        tint = EmeraldPresent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Contact Email
                    if (student.email.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Email Contact:", color = Color(0xFF64748B), fontSize = 12.5.sp)
                            Text(student.email, fontWeight = FontWeight.Medium, color = NavyPrimary, fontSize = 12.5.sp)
                        }
                    }

                    // Enrollment Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enrollment Date:", color = Color(0xFF64748B), fontSize = 12.5.sp)
                        Text(DateUtils.formatDate(student.assignedDate), color = NavyPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                    }

                    // Target Exam Goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Exam Goal:", color = Color(0xFF64748B), fontSize = 12.5.sp)
                        Text(student.examTarget, fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.5.sp)
                    }

                    // Shift
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Study Shift:", color = Color(0xFF64748B), fontSize = 12.5.sp)
                        Text(student.shift, fontWeight = FontWeight.Medium, color = NavyPrimary, fontSize = 12.5.sp)
                    }

                    // Plan Fee
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Plan:", color = Color(0xFF64748B), fontSize = 12.5.sp)
                        Text("₹${student.monthlyFee.toInt()} / month", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 12.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Attendance Status Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPresentNow) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                        .border(
                            1.dp,
                            if (isPresentNow) EmeraldPresent else Color(0xFFCBD5E1),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isPresentNow) "🟢 Present Inside Library" else "⚪ Currently Outside",
                                fontWeight = FontWeight.Bold,
                                color = if (isPresentNow) EmeraldDark else Color(0xFF475569),
                                fontSize = 13.sp
                            )
                            if (isPresentNow && currentLog != null) {
                                Text(
                                    text = "Checked In at ${currentLog.entryTime}",
                                    fontSize = 11.5.sp,
                                    color = EmeraldDark
                                )
                            } else {
                                Text(
                                    text = "Tap to punch attendance",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Button(
                            onClick = { onPunchAttendance(student.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPresentNow) Color(0xFFEA580C) else EmeraldPresent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPresentNow) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPresentNow) "Punch OUT" else "Punch IN", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fee Status & Payment Toggle Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPaid) Color(0xFFEFF6FF) else Color(0xFFFFF1F2))
                        .border(
                            1.dp,
                            if (isPaid) Color(0xFF93C5FD) else Color(0xFFFECDD3),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Monthly Fee (${DateUtils.getCurrentMonthName()})",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = if (isPaid) "Status: PAID (₹${payment?.amount?.toInt() ?: student.monthlyFee.toInt()})" else "Status: DUE / PENDING",
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) Color(0xFF1D4ED8) else CrimsonDark,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onTogglePayment(student, !isPaid) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isPaid) "Mark Due" else "Mark Paid", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: QR Card & Vacate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onShowQrCard(student) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR Card", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { showVacateConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonDue.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vacate")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color(0xFF64748B))
                }
            }
        }
    }
}
