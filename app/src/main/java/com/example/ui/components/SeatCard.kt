package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun SeatCard(
    seatNumber: Int,
    student: StudentEntity?,
    isPresentNow: Boolean,
    payment: PaymentEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOccupied = student != null
    val isPaid = payment?.status == "PAID"

    // Pulsing subtle glow for actively studying students
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isPresentNow -> Color(0xFFF0FDF4) // Soft mint green tint
            isOccupied -> Color(0xFFFFFFFF) // Crisp white with slate tint
            else -> Color(0xFFF8FAFC) // Very light neutral grey
        },
        label = "containerColor"
    )

    val borderColor = when {
        isPresentNow -> EmeraldPresent
        isOccupied -> NavyPrimary.copy(alpha = 0.45f)
        else -> Color(0xFFCBD5E1)
    }

    val borderWidth = if (isPresentNow) 2.dp else 1.dp

    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .testTag("seat_item_$seatNumber")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPresentNow) 3.dp else 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            // Top Row: Seat Number & Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seat Number Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isPresentNow -> EmeraldDark
                                isOccupied -> NavyPrimary
                                else -> Color(0xFF64748B)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "#$seatNumber",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // IN / AWAY Live Status Pill
                if (isPresentNow) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldPresent)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "IN",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else if (isOccupied) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "OUT",
                            color = Color(0xFF64748B),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Center Content: Student Name & Desk Icon or Vacant Prompt
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isOccupied) {
                    // Avatar Initials Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isPresentNow) Color(0xFFDCFCE7) else Color(0xFFEFF6FF))
                            .border(
                                1.dp,
                                if (isPresentNow) EmeraldPresent else Color(0xFF93C5FD),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(2).uppercase(),
                            color = if (isPresentNow) EmeraldDark else NavyPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Empty Desk Illustration
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chair,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "VACANT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "+ Assign",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Bottom Footer Row
            if (isOccupied) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shift Mini Tag
                    Text(
                        text = student.shift.split(" ").firstOrNull() ?: "Full",
                        fontSize = 8.5.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Fee Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isPaid) "PAID" else "DUE",
                            color = if (isPaid) EmeraldDark else CrimsonDark,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "FREE",
                        fontSize = 7.5.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
