package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.StudentEntity
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEditDialog(
    student: StudentEntity,
    availableSeats: List<Int>,
    onDismiss: () -> Unit,
    onSave: (updatedStudent: StudentEntity, newSeatNumber: Int) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var phone by remember { mutableStateOf(student.phone) }
    var email by remember { mutableStateOf(student.email) }
    var feeStr by remember { mutableStateOf(student.monthlyFee.toInt().toString()) }
    var shift by remember { mutableStateOf(student.shift) }
    var examTarget by remember { mutableStateOf(student.examTarget) }
    var notes by remember { mutableStateOf(student.notes) }
    var selectedSeat by remember { mutableIntStateOf(student.seatNumber) }

    val seatOptions = remember {
        (listOf(student.seatNumber) + availableSeats).distinct().sorted()
    }
    var seatExpanded by remember { mutableStateOf(false) }

    val officialPlans = com.example.data.LibraryRateList.PLANS
    var shiftExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .testTag("student_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Student Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "Update student details, contact info, and linked seat",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Assigned Seat Switcher
                ExposedDropdownMenuBox(
                    expanded = seatExpanded,
                    onExpandedChange = { seatExpanded = !seatExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "Seat #$selectedSeat" + if (selectedSeat == student.seatNumber) " (Current)" else " (Reassign)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Study Seat") },
                        leadingIcon = { Icon(Icons.Default.Chair, contentDescription = null, tint = NavyPrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seatExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = seatExpanded,
                        onDismissRequest = { seatExpanded = false }
                    ) {
                        seatOptions.forEach { sNum ->
                            DropdownMenuItem(
                                text = { Text("Seat #$sNum" + if (sNum == student.seatNumber) " (Current Seat)" else "") },
                                onClick = {
                                    selectedSeat = sNum
                                    seatExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Contact") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Contact") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Exam Goal
                OutlinedTextField(
                    value = examTarget,
                    onValueChange = { examTarget = it },
                    label = { Text("Exam / Course Goal") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = NavyPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Shift
                ExposedDropdownMenuBox(
                    expanded = shiftExpanded,
                    onExpandedChange = { shiftExpanded = !shiftExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = shift,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Study Shift") },
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

                // Monthly Fee
                OutlinedTextField(
                    value = feeStr,
                    onValueChange = { feeStr = it },
                    label = { Text("Monthly Fee (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = NavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks & Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            val fee = feeStr.toDoubleOrNull() ?: student.monthlyFee
                            val updated = student.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                monthlyFee = fee,
                                shift = shift,
                                examTarget = examTarget.trim(),
                                notes = notes.trim()
                            )
                            onSave(updated, selectedSeat)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
