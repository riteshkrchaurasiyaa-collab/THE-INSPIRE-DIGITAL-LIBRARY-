package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LibraryPlan
import com.example.data.LibraryRateList
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary

@Composable
fun RateListDialog(onDismiss: () -> Unit) {
    val plansByCategory = LibraryRateList.PLANS.groupBy { it.category }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color.Black),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .testTag("rate_list_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // Auspicious Invocations in Red
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("श्री सरस्वत्यै नमः", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("श्री गणेशाय नमः", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("श्री सरस्वत्यै नमः", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Yellow Capsule
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFDE047))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(24.dp))
                        .padding(vertical = 6.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "THE INSPIRE DIGITAL LIBRARY",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "NEAR MAA DURGA TREDERS DESRI BIBHUTIPUR",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "THE INSPIRE DIGITAL LIBRARY RATE LIST",
                    color = Color(0xFF9A3412),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Table of Shifts
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Subheaders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color.Black)
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SL NO.", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFDC2626), modifier = Modifier.width(42.dp))
                            Text("SHIFT", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
                            Text("TIME AND PRICE", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFDC2626), textAlign = TextAlign.End)
                        }

                        // Categories and Plans
                        plansByCategory.forEach { (category, plans) ->
                            // Category Row
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFFBEB))
                                    .border(0.5.dp, Color.Black)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    color = Color(0xFF9A3412),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            plans.forEach { plan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, Color(0xFFE2E8F0))
                                        .padding(vertical = 7.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${plan.slNo}.",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = Color.Black,
                                        modifier = Modifier.width(42.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = plan.shiftName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color.Black
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = plan.timings,
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "₹${plan.price.toInt()}/-",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.5.sp,
                                            color = EmeraldDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mentor/Author Signature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(1.dp)
                                .background(Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "MENTOR/AUTHOR SIGNATURE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Rate Card")
                }
            }
        }
    }
}
