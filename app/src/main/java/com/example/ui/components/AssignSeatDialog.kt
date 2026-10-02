package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyPrimary
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignSeatDialog(
    seatNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        phone: String,
        email: String,
        enrollmentDate: Long,
        fee: Double,
        shift: String,
        examTarget: String,
        notes: String,
        isPaid: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var feeStr by remember { mutableStateOf("800") }
    var shift by remember { mutableStateOf("FULL DAY SHIFT (6AM - 6AM)") }
    var examTarget by remember { mutableStateOf("UPSC / State PCS") }
    var notes by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val enrollmentTimestamp = remember { System.currentTimeMillis() }

    val officialPlans = com.example.data.LibraryRateList.PLANS
    var shiftExpanded by remember { mutableStateOf(false) }

    val examOptions = listOf(
        "UPSC / State PCS",
        "SSC CGL / CHSL",
        "Banking PO / Clerk",
        "NEET / Medical",
        "JEE / Engineering",
        "CA / CS / Commerce",
        "Judiciary / Law",
        "Defense / CDS / NDA",
        "General Self Study"
    )
    var examExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("assign_seat_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Enroll Student",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "Link student directly to Seat #$seatNumber",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(NavyPrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SEAT $seatNumber",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enrollment Date Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enrollment Date: ${DateUtils.formatDate(enrollmentTimestamp)}",
                        fontSize = 11.5.sp,
                        color = NavyPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) errorText = null
                    },
                    label = { Text("Student Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = {
                        if (errorText != null) {
                            Text(errorText!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assign_name_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Mobile Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Contact Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assign_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Exam Target Dropdown
                ExposedDropdownMenuBox(
                    expanded = examExpanded,
                    onExpandedChange = { examExpanded = !examExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = examTarget,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Exam / Study Goal") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = examExpanded,
                        onDismissRequest = { examExpanded = false }
                    ) {
                        examOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    examTarget = opt
                                    examExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shift Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = shiftExpanded,
                    onExpandedChange = { shiftExpanded = !shiftExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = shift,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Study Shift / Timings") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = shiftExpanded,
                        onDismissRequest = { shiftExpanded = false }
                    ) {
                        officialPlans.forEach { plan ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${plan.slNo}. ${plan.shiftName} (${plan.timings})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "${plan.category} • ₹${plan.price.toInt()}/-",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                },
                                onClick = {
                                    shift = "${plan.shiftName} (${plan.timings})"
                                    feeStr = plan.price.toInt().toString()
                                    shiftExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Monthly Fee input
                OutlinedTextField(
                    value = feeStr,
                    onValueChange = { feeStr = it },
                    label = { Text("Monthly Fee (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assign_fee_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks / Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Status Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isPaid) "Fee Status: PAID" else "Fee Status: DUE",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) EmeraldPresent else Color(0xFFEF4444)
                            )
                        )
                        Text(
                            text = if (isPaid) "First month fee collected" else "Mark pending for collection",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                        )
                    }

                    Switch(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldPresent,
                            checkedTrackColor = Color(0xFFD1FAE5)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorText = "Student name is required"
                                return@Button
                            }
                            val fee = feeStr.toDoubleOrNull() ?: 800.0
                            onConfirm(
                                name,
                                phone,
                                email,
                                enrollmentTimestamp,
                                fee,
                                shift,
                                examTarget,
                                notes,
                                isPaid
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("assign_save_button")
                    ) {
                        Text("Allot Seat #$seatNumber", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
