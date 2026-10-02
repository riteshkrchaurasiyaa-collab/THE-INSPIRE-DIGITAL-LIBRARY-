package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.CrimsonDue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.util.DateUtils
import com.example.util.ExportUtils

enum class StudentFilter {
    ALL,
    FULL_DAY,
    MORNING,
    EVENING,
    TWENTY_FOUR_SEVEN,
    PAID,
    DUE
}

@Composable
fun StudentsScreen(
    students: List<StudentEntity>,
    payments: List<PaymentEntity>,
    onStudentClick: (StudentEntity) -> Unit,
    onEditStudent: (StudentEntity) -> Unit,
    onShowQrCard: (StudentEntity) -> Unit,
    onEnrollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(StudentFilter.ALL) }

    val currentMonth = DateUtils.getCurrentMonthName()

    val paymentMap = remember(payments) {
        payments.associateBy { it.studentId }
    }

    val filteredList = students.filter { student ->
        val payment = paymentMap[student.id]
        val isPaid = payment?.status == "PAID"

        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            student.name.lowercase().contains(q) ||
                    student.phone.contains(q) ||
                    student.email.lowercase().contains(q) ||
                    student.seatNumber.toString() == q ||
                    student.examTarget.lowercase().contains(q)
        }

        val matchesFilter = when (selectedFilter) {
            StudentFilter.ALL -> true
            StudentFilter.FULL_DAY -> student.shift.contains("Full", ignoreCase = true)
            StudentFilter.MORNING -> student.shift.contains("Morning", ignoreCase = true)
            StudentFilter.EVENING -> student.shift.contains("Evening", ignoreCase = true)
            StudentFilter.TWENTY_FOUR_SEVEN -> student.shift.contains("24", ignoreCase = true)
            StudentFilter.PAID -> isPaid
            StudentFilter.DUE -> !isPaid
        }

        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Screen Header with Export Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "👥 Student Management",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Text(
                        text = "Directory & direct desk allocations",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Export Clearly to PDF Button
                    Button(
                        onClick = {
                            ExportUtils.exportStudentsPdf(
                                context = context,
                                students = students,
                                payments = payments,
                                monthName = currentMonth
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Export to CSV Button
                    OutlinedButton(
                        onClick = {
                            ExportUtils.exportPaymentsCsv(
                                context = context,
                                students = students,
                                payments = payments,
                                monthName = currentMonth
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 11.5.sp)
                    }

                    // Enroll Action Button
                    Button(
                        onClick = onEnrollClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        // Summary Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enrolled Students", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text("${students.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = NavyPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Free Desks", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text("${66 - students.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldPresent)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Capacity", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text("66", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name, phone, seat #, or exam...", fontSize = 12.5.sp) },
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

        // Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == StudentFilter.ALL,
                    onClick = { selectedFilter = StudentFilter.ALL },
                    label = { Text("All (${students.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedFilter == StudentFilter.FULL_DAY,
                    onClick = { selectedFilter = StudentFilter.FULL_DAY },
                    label = { Text("Full Day") }
                )
                FilterChip(
                    selected = selectedFilter == StudentFilter.MORNING,
                    onClick = { selectedFilter = StudentFilter.MORNING },
                    label = { Text("Morning") }
                )
                FilterChip(
                    selected = selectedFilter == StudentFilter.EVENING,
                    onClick = { selectedFilter = StudentFilter.EVENING },
                    label = { Text("Evening") }
                )
                FilterChip(
                    selected = selectedFilter == StudentFilter.PAID,
                    onClick = { selectedFilter = StudentFilter.PAID },
                    label = { Text("🟢 Paid") }
                )
                FilterChip(
                    selected = selectedFilter == StudentFilter.DUE,
                    onClick = { selectedFilter = StudentFilter.DUE },
                    label = { Text("🔴 Due") }
                )
            }
        }

        if (filteredList.isEmpty()) {
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
                        text = "No students found matching your criteria.\nTap '+ Enroll' to admit a student.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { student ->
                val payment = paymentMap[student.id]
                val isPaid = payment?.status == "PAID"

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStudentClick(student) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Linked Seat Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NavyPrimary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "SEAT #${student.seatNumber}",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Enrolled: ${DateUtils.formatDate(student.assignedDate)}",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Fee Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isPaid) "PAID" else "DUE",
                                    color = if (isPaid) EmeraldDark else CrimsonDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Student Details Strip (Phone, Email, Exam)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = student.examTarget,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NavyPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "Shift: ${student.shift}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        if (student.phone.isNotBlank() || student.email.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (student.phone.isNotBlank()) "📞 ${student.phone}" else "✉️ ${student.email}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569)
                                )

                                Text(
                                    text = "₹${student.monthlyFee.toInt()} / mo",
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Actions: Edit, QR Card, Call
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onShowQrCard(student) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR Card", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onEditStudent(student) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Info", fontSize = 11.sp)
                            }

                            if (student.phone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phone}"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
