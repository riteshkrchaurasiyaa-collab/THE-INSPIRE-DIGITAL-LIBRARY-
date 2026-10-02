package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.LibraryRateList
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import com.example.ui.theme.CrimsonDark
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.util.ExportUtils

@Composable
fun FeeReceiptDialog(
    student: StudentEntity,
    payment: PaymentEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptId = if (payment.receiptNumber.isNotBlank()) payment.receiptNumber else "INS-RCPT-${student.seatNumber}-${payment.paymentId}"
    val plan = LibraryRateList.findPlanByShiftName(student.shift)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFF0F172A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("fee_receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // 1. Auspicious Invocations in Red
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("श्री सरस्वत्यै नमः", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    Text("श्री गणेशाय नमः", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    Text("श्री सरस्वत्यै नमः", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Yellow Header Capsule: "THE INSPIRE DIGITAL LIBRARY"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFDE047))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(24.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "THE INSPIRE DIGITAL LIBRARY",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Address & Subheading
                Text(
                    text = "NEAR MAA DURGA TREDERS DESRI BIBHUTIPUR",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "THE INSPIRE DIGITAL LIBRARY RATE LIST & PAYMENT SLIP",
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Student & Receipt Meta
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Receipt #: $receiptId", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = NavyPrimary)
                        Text("Date: ${payment.paymentDate.ifBlank { "Today" }}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Student: ${student.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavyPrimary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("SEAT #${student.seatNumber}", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mobile: ${student.phone}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                        Text("Billing Month: ${payment.monthName}", fontSize = 11.5.sp, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Official Shift & Hours Rate Card Table
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Category Header (e.g. "FOUR HOURS SHIFTS", "EIGHT HOURS SHIFTS")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFBEB))
                                .border(1.dp, Color.Black)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = plan?.category ?: "HOURLY SHIFTS & LIBRARY TIMINGS",
                                color = Color(0xFF9A3412),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Column Titles
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color.Black)
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SL NO.", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = Color(0xFFDC2626), modifier = Modifier.width(44.dp))
                            Text("SHIFT", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
                            Text("TIME & PRICE", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, color = Color(0xFFDC2626), textAlign = TextAlign.End)
                        }

                        // Selected Shift Details Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${plan?.slNo ?: 1}.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier.width(44.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan?.shiftName ?: student.shift.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = plan?.timings ?: "Full Day",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${payment.amount.toInt()}/-",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "MODE: ${payment.paymentMode}",
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Bottom Status Bar inside Table
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDCFCE7))
                                .border(1.dp, Color.Black)
                                .padding(vertical = 5.dp, horizontal = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("STATUS: PAYMENT RECEIVED", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6. Mentor / Author Signature Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(1.dp)
                                .background(Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MENTOR/AUTHOR SIGNATURE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Download PDF & Share WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            ExportUtils.exportPaymentSlipPdf(context, student, payment)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val slipMessage = buildString {
                                appendLine("श्री सरस्वत्यै नमः | श्री गणेशाय नमः")
                                appendLine("🏛️ THE INSPIRE DIGITAL LIBRARY")
                                appendLine("📍 Near Maa Durga Treders Desri Bibhutipur")
                                appendLine("🧾 OFFICIAL PAYMENT SLIP & RECEIPT")
                                appendLine("----------------------------------------")
                                appendLine("Receipt No: $receiptId")
                                appendLine("Student: ${student.name}")
                                appendLine("Seat Number: #${student.seatNumber}")
                                appendLine("Category: ${plan?.category ?: "Hourly Shift"}")
                                appendLine("Shift: ${plan?.shiftName ?: student.shift} (${plan?.timings ?: ""})")
                                appendLine("Amount Paid: ₹${payment.amount.toInt()}/-")
                                appendLine("Payment Mode: ${payment.paymentMode}")
                                appendLine("Status: SUCCESS / PAID")
                                appendLine("Date: ${payment.paymentDate}")
                                appendLine("----------------------------------------")
                                appendLine("MENTOR/AUTHOR SIGNATURE: Verified")
                                appendLine("Thank you for choosing The Inspire Digital Library!")
                            }
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, slipMessage)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Payment Slip"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPresent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
