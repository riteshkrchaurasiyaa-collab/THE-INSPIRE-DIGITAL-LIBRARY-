package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceLogEntity
import com.example.ui.theme.CrimsonDue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.util.DateUtils
import com.example.util.ExportUtils

@Composable
fun ReportScreen(
    logs: List<AttendanceLogEntity>,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    val totalHoursToday = remember(logs) {
        String.format(
            "%.1f",
            logs.mapNotNull { it.durationHours }.sum()
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("⚠️ Clear All Data?") },
            text = { Text("Are you sure you want to clear all students, attendance logs, and fee records? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonDue)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📊 Today's Attendance Report",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Text(
                        text = DateUtils.getTodayFormatted(),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Export Attendance CSV Button
                    Button(
                        onClick = {
                            ExportUtils.exportAttendanceCsv(
                                context = context,
                                logs = logs,
                                reportTitle = "Daily_Attendance"
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

                    // Share Text Summary
                    OutlinedButton(
                        onClick = {
                            val reportText = buildString {
                                appendLine("The Inspire Digital Library - Daily Attendance")
                                appendLine("Date: ${DateUtils.getTodayFormatted()}")
                                appendLine("Total Entries: ${logs.size}")
                                appendLine("Total Study Hours: $totalHoursToday hrs")
                                appendLine("-----------------------------")
                                logs.forEach { l ->
                                    appendLine("Seat #${l.seatNumber} | ${l.studentName} | In: ${l.entryTime} | Out: ${l.exitTime ?: "Present"} | ${l.durationHours ?: 0.0}h")
                                }
                            }
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Attendance Report"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Summary Metric Strip
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Today's Footfall", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text("${logs.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = NavyPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Currently IN", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text(
                            "${logs.count { it.exitTimestamp == null }}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = EmeraldPresent
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Study Time", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text("$totalHoursToday hrs", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2563EB))
                    }
                }
            }
        }

        // Table Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyPrimary)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SEAT", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(46.dp))
                Text("STUDENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("ENTRY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(66.dp), textAlign = TextAlign.Center)
                Text("EXIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(66.dp), textAlign = TextAlign.Center)
                Text("HOURS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(52.dp), textAlign = TextAlign.End)
            }
        }

        if (logs.isEmpty()) {
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
                        text = "No attendance logs recorded today.\nPoint camera at student QR to log attendance.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(logs) { log ->
                val isPresent = log.exitTimestamp == null
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isPresent) Color(0xFFF0FDF4) else Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPresent) EmeraldDark else NavyPrimary)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${log.seatNumber}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.studentName,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 12.5.sp
                            )
                            if (isPresent) {
                                Text(
                                    text = "Active Now",
                                    color = EmeraldDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = log.entryTime,
                            fontSize = 11.5.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.width(66.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = log.exitTime ?: "—",
                            fontSize = 11.5.sp,
                            color = if (isPresent) EmeraldDark else Color(0xFF475569),
                            fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.width(66.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (log.durationHours != null) "${log.durationHours}h" else "IN",
                            fontWeight = FontWeight.Bold,
                            color = if (isPresent) EmeraldDark else NavyPrimary,
                            fontSize = 11.5.sp,
                            modifier = Modifier.width(52.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Danger Zone: Clear Data
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDue),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonDue.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_clear_all_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚠️ Reset All Students & Attendance Data")
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
