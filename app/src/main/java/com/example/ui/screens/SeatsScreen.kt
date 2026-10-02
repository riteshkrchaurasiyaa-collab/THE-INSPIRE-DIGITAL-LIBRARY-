package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceLogEntity
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import com.example.ui.components.SeatCard
import com.example.ui.theme.CrimsonDue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

enum class SeatFilter {
    ALL,
    OCCUPIED,
    VACANT,
    PRESENT_NOW,
    FEE_DUE
}

enum class SeatViewMode {
    ALL_GRID,
    ZONE_SECTIONS
}

@Composable
fun SeatsScreen(
    students: List<StudentEntity>,
    todayLogs: List<AttendanceLogEntity>,
    payments: List<PaymentEntity>,
    onSeatClick: (seatNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(SeatFilter.ALL) }
    var viewMode by remember { mutableStateOf(SeatViewMode.ALL_GRID) }
    var searchQuery by remember { mutableStateOf("") }

    val studentMap = remember(students) {
        students.associateBy { it.seatNumber }
    }

    val presentSeatSet = remember(todayLogs) {
        todayLogs.filter { it.exitTimestamp == null }.map { it.seatNumber }.toSet()
    }

    val paymentMap = remember(payments) {
        payments.associateBy { it.seatNumber }
    }

    val occupiedCount = students.size
    val vacantCount = (66 - occupiedCount).coerceAtLeast(0)
    val presentCount = presentSeatSet.size
    val occupancyRatio = if (66 > 0) occupiedCount / 66f else 0f

    // 66 total seats list
    val allSeats = (1..66).toList()
    val filteredSeats = allSeats.filter { seatNum ->
        val s = studentMap[seatNum]
        val isPresent = presentSeatSet.contains(seatNum)
        val pay = paymentMap[seatNum]
        val isDue = s != null && pay?.status != "PAID"

        // Search match
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            seatNum.toString() == q || (s != null && (s.name.lowercase().contains(q) || s.phone.contains(q) || s.id.lowercase().contains(q)))
        }

        val matchesFilter = when (selectedFilter) {
            SeatFilter.ALL -> true
            SeatFilter.OCCUPIED -> s != null
            SeatFilter.VACANT -> s == null
            SeatFilter.PRESENT_NOW -> isPresent
            SeatFilter.FEE_DUE -> isDue
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Visual Occupancy Dashboard Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "66 Study Desks Map",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "$occupiedCount Occupied • $vacantCount Available • $presentCount Studying Now",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.5.sp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyPrimary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(occupancyRatio * 100).toInt()}% FULL",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Occupancy Visual Bar
                LinearProgressIndicator(
                    progress = { occupancyRatio },
                    color = EmeraldPresent,
                    trackColor = Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // View Mode Segmented Controls (Full Grid vs Zones)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = viewMode == SeatViewMode.ALL_GRID,
                        onClick = { viewMode = SeatViewMode.ALL_GRID },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    ) {
                        Text("Compact 66 Grid", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    SegmentedButton(
                        selected = viewMode == SeatViewMode.ZONE_SECTIONS,
                        onClick = { viewMode = SeatViewMode.ZONE_SECTIONS },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Default.ViewModule, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    ) {
                        Text("3 Study Zones", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by desk #, student name, or mobile...", fontSize = 12.5.sp) },
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seats_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == SeatFilter.ALL,
                onClick = { selectedFilter = SeatFilter.ALL },
                label = { Text("All (66)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == SeatFilter.VACANT,
                onClick = { selectedFilter = SeatFilter.VACANT },
                label = { Text("⚪ Vacant ($vacantCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF475569),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == SeatFilter.OCCUPIED,
                onClick = { selectedFilter = SeatFilter.OCCUPIED },
                label = { Text("🔵 Occupied ($occupiedCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == SeatFilter.PRESENT_NOW,
                onClick = { selectedFilter = SeatFilter.PRESENT_NOW },
                label = { Text("🟢 Present ($presentCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPresent,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == SeatFilter.FEE_DUE,
                onClick = { selectedFilter = SeatFilter.FEE_DUE },
                label = { Text("🔴 Fee Due") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrimsonDue,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Legend Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = EmeraldPresent, label = "Present (IN)")
            LegendItem(color = NavyPrimary, label = "Occupied (OUT)")
            LegendItem(color = Color(0xFF94A3B8), label = "Vacant Desk")
            LegendItem(color = CrimsonDue, label = "Fee Due")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Seat View (Compact Grid vs 3 Zone Sections)
        if (viewMode == SeatViewMode.ALL_GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("seats_grid")
            ) {
                items(filteredSeats, key = { it }) { seatNumber ->
                    val student = studentMap[seatNumber]
                    val isPresent = presentSeatSet.contains(seatNumber)
                    val payment = paymentMap[seatNumber]

                    SeatCard(
                        seatNumber = seatNumber,
                        student = student,
                        isPresentNow = isPresent,
                        payment = payment,
                        onClick = { onSeatClick(seatNumber) }
                    )
                }
            }
        } else {
            // 3 Library Zones View
            val zones = listOf(
                Triple("Zone A: Silent Reading Hall", 1..22, "Desks 1 – 22 • Deep Focus Zone"),
                Triple("Zone B: Cubicles & Study Bays", 23..44, "Desks 23 – 44 • Power & Laptop Stations"),
                Triple("Zone C: Window & AC Cabinets", 45..66, "Desks 45 – 66 • Natural Light & AC Hall")
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                zones.forEach { (zoneTitle, range, zoneDesc) ->
                    val zoneSeats = filteredSeats.filter { it in range }
                    if (zoneSeats.isNotEmpty()) {
                        item(key = zoneTitle) {
                            val zoneOccupied = zoneSeats.count { studentMap[it] != null }
                            val zonePresent = zoneSeats.count { presentSeatSet.contains(it) }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = zoneTitle,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = zoneDesc,
                                                color = Color(0xFF64748B),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE2E8F0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$zoneOccupied/22 Occupied • $zonePresent IN",
                                                color = NavyPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 4-column subgrid for the zone
                                    val chunkedSeats = zoneSeats.chunked(4)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        chunkedSeats.forEach { rowSeats ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowSeats.forEach { seatNum ->
                                                    val student = studentMap[seatNum]
                                                    val isPresent = presentSeatSet.contains(seatNum)
                                                    val payment = paymentMap[seatNum]

                                                    SeatCard(
                                                        seatNumber = seatNum,
                                                        student = student,
                                                        isPresentNow = isPresent,
                                                        payment = payment,
                                                        onClick = { onSeatClick(seatNum) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                // Fill empty space if row has fewer than 4 items
                                                for (i in 0 until (4 - rowSeats.size)) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.5.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
    }
}
