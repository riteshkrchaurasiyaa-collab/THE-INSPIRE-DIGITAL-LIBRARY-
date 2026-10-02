package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceLogEntity
import com.example.data.PunchResult
import com.example.data.StudentEntity
import com.example.ui.components.CameraQrScanner
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun ScanScreen(
    students: List<StudentEntity>,
    recentLogs: List<AttendanceLogEntity>,
    lastResult: PunchResult?,
    onPunchCode: (String) -> Unit,
    onClearResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manualInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "📷 CameraX QR Attendance",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "Live camera scan • Local Room DB timestamp storage",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Room DB",
                        color = EmeraldDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live CameraX Scanner Viewport with Torch & Flip
        CameraQrScanner(
            onQrDecoded = { code ->
                onPunchCode(code)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Punch Feedback Result Card (with Room DB confirmation)
        if (lastResult != null) {
            when (lastResult) {
                is PunchResult.EntrySuccess -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPresent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("punch_entry_success_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        tint = EmeraldPresent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "🟢 ENTRY RECORDED IN ROOM DB",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldDark,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${lastResult.student.name} (Seat #${lastResult.student.seatNumber})",
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                IconButton(onClick = onClearResult) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = EmeraldDark)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = EmeraldPresent.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Entry Time: ${lastResult.time}",
                                    color = EmeraldDark,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "ID: ${lastResult.student.id}",
                                    color = Color(0xFF065F46),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                is PunchResult.ExitSuccess -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF97316)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("punch_exit_success_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "🟠 EXIT RECORDED IN ROOM DB",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFC2410C),
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${lastResult.student.name} (Seat #${lastResult.student.seatNumber})",
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                IconButton(onClick = onClearResult) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF9A3412))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF97316).copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Exit Time: ${lastResult.time}",
                                    color = Color(0xFF9A3412),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Total Study: ${lastResult.durationHours} hrs",
                                    color = Color(0xFFC2410C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                is PunchResult.StudentNotFound -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Student Not Found",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "QR ID '${lastResult.query}' is not registered in the library database.",
                                        color = Color(0xFF7F1D1D),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                            IconButton(onClick = onClearResult) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                }

                is PunchResult.Error -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = lastResult.message, color = Color(0xFF92400E), fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Manual Punch Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚡ Manual QR ID / Seat Punch",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "Enter Student QR ID (e.g. SEAT05) or Seat Number (e.g. 5)",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.5.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text("e.g. SEAT01 or 5") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_punch_input")
                    )

                    Button(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                onPunchCode(manualInput.trim())
                                manualInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("manual_punch_submit_button")
                    ) {
                        Text("Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Students Touch-Punch List
        if (students.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Punch Simulation (Active Students)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    students.take(12).forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .clickable { onPunchCode(s.id) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NavyPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${s.seatNumber}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.name,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NavyPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recent Room DB Attendance Logs Ledger
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Room Database Attendance Ledger",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                }

                Text(
                    text = "${recentLogs.size} logs",
                    color = Color(0xFF64748B),
                    fontSize = 11.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (recentLogs.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No timestamps saved yet. Point camera at student QR or punch manually to save to Room database.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentLogs.take(5).forEach { log ->
                        val isPresent = log.exitTimestamp == null
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isPresent) Color(0xFFF0FDF4) else Color.White),
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
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isPresent) EmeraldDark else NavyPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${log.seatNumber}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = log.studentName,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = "IN: ${log.entryTime}" +
                                                    if (log.exitTime != null) " • OUT: ${log.exitTime}" else " • Present",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isPresent) EmeraldPresent else Color(0xFFE2E8F0))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isPresent) "IN" else "${log.durationHours ?: 0.0}h",
                                            color = if (isPresent) Color.White else NavyPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Room ID: #${log.logId}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
