package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Main Brand Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_brand_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF101D36)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_inspire_emblem),
                            contentDescription = "The Inspire Digital Library Emblem",
                            modifier = Modifier.size(92.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "THE INSPIRE DIGITAL LIBRARY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "SELF STUDY POINT • 66 SEATS",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "A premier calm study space designed for serious aspirants of UPSC, State PCS, SSC, Banking, NEET, JEE & CA exams.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "Key Amenities & Facilities",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FacilityItem(icon = Icons.Default.Chair, title = "66 Ergonomic Study Cabins", desc = "Spacious personal wooden desks with soft lighting & charging sockets")
                FacilityItem(icon = Icons.Default.Wifi, title = "High-Speed 5G Optical Fiber", desc = "Unlimited seamless Wi-Fi for video lectures & study tests")
                FacilityItem(icon = Icons.Default.AcUnit, title = "Fully Air Conditioned", desc = "Temperature controlled silent environment for deep focus")
                FacilityItem(icon = Icons.Default.Power, title = "24x7 Power Backup", desc = "Uninterrupted inverter & silent generator setup")
                FacilityItem(icon = Icons.Default.LocalDrink, title = "RO Purified Water", desc = "Hot, cold and normal pure drinking water dispenser")
                FacilityItem(icon = Icons.Default.Videocam, title = "CCTV & QR Attendance", desc = "24/7 security surveillance with automated QR student attendance")
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Study Shifts & Timing",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Shift 1: Morning (06:00 AM – 02:00 PM)", fontSize = 12.5.sp, color = Color(0xFF475569))
                    Text("• Shift 2: Evening (02:00 PM – 10:00 PM)", fontSize = 12.5.sp, color = Color(0xFF475569))
                    Text("• Full Day: 06:00 AM – 11:00 PM", fontSize = 12.5.sp, color = Color(0xFF475569))
                    Text("• 24x7 Self Study: Round the clock access", fontSize = 12.5.sp, color = Color(0xFF475569))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun FacilityItem(icon: ImageVector, title: String, desc: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                Text(desc, fontSize = 11.5.sp, color = Color(0xFF64748B))
            }
        }
    }
}
