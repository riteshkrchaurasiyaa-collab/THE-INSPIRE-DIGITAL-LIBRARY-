package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AttendanceLogEntity
import com.example.data.PaymentEntity
import com.example.data.StudentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    /**
     * Exports attendance logs to a CSV file in device storage and triggers share/open.
     */
    fun exportAttendanceCsv(
        context: Context,
        logs: List<AttendanceLogEntity>,
        reportTitle: String = "Attendance_Report"
    ): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${reportTitle}_$timeStamp.csv"

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val csvContent = buildString {
                appendLine("Log ID,Date,Seat Number,Student ID,Student Name,Entry Time,Exit Time,Study Duration (Hours),Status")
                logs.forEach { log ->
                    val status = if (log.exitTimestamp == null) "PRESENT_IN" else "COMPLETED"
                    val duration = log.durationHours?.toString() ?: "0.0"
                    val exit = log.exitTime ?: "--"
                    // Escape quotes and commas in student name
                    val safeName = "\"${log.studentName.replace("\"", "\"\"")}\""
                    appendLine("${log.logId},${log.dateStr},${log.seatNumber},${log.studentId},$safeName,${log.entryTime},$exit,$duration,$status")
                }
            }

            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            shareFile(context, file, "text/csv", "Export Attendance CSV")
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting CSV: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Exports monthly fee payments to a CSV file in device storage and triggers share/open.
     */
    fun exportPaymentsCsv(
        context: Context,
        students: List<StudentEntity>,
        payments: List<PaymentEntity>,
        monthName: String
    ): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val cleanMonth = monthName.replace(" ", "_")
            val fileName = "Monthly_Payments_${cleanMonth}_$timeStamp.csv"

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val paymentMap = payments.associateBy { it.studentId }

            val csvContent = buildString {
                appendLine("Seat Number,Student ID,Student Name,Mobile Phone,Email,Enrollment Date,Shift,Target Exam,Fee Amount (INR),Status,Payment Date,Payment Mode,Receipt Number")
                students.forEach { s ->
                    val p = paymentMap[s.id]
                    val status = if (p?.status == "PAID") "PAID" else "DUE"
                    val amount = (p?.amount ?: s.monthlyFee).toInt()
                    val pDate = p?.paymentDate ?: ""
                    val pMode = p?.paymentMode ?: "UPI"
                    val receipt = p?.receiptNumber ?: ""
                    val enrollDate = DateUtils.formatDate(s.assignedDate)
                    val safeName = "\"${s.name.replace("\"", "\"\"")}\""
                    val safeExam = "\"${s.examTarget.replace("\"", "\"\"")}\""
                    appendLine("${s.seatNumber},${s.id},$safeName,${s.phone},${s.email},$enrollDate,${s.shift},$safeExam,$amount,$status,$pDate,$pMode,$receipt")
                }
            }

            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            shareFile(context, file, "text/csv", "Export Payment Report CSV")
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting payments CSV: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Exports a comprehensive, beautifully formatted PDF report of students data using Android's native PdfDocument.
     */
    fun exportStudentsPdf(
        context: Context,
        students: List<StudentEntity>,
        payments: List<PaymentEntity>,
        monthName: String
    ): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val cleanMonth = monthName.replace(" ", "_")
            val fileName = "Students_Directory_${cleanMonth}_$timeStamp.pdf"

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val paymentMap = payments.associateBy { it.studentId }

            // A4 page dimensions in PostScript points: 595 x 842 points
            val pageWidth = 595
            val pageHeight = 842
            val pdfDocument = PdfDocument()

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rowsPerPage = 16
            val studentChunks = if (students.isEmpty()) listOf(emptyList()) else students.chunked(rowsPerPage)
            val totalPages = studentChunks.size

            for ((pageIndex, chunk) in studentChunks.withIndex()) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // 1. Navy Blue Header Banner
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

                // Gold Accent Line
                paint.color = Color.parseColor("#D4AF37")
                canvas.drawRect(0f, 90f, pageWidth.toFloat(), 94f, paint)

                // Header Title
                paint.color = Color.WHITE
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("THE INSPIRE DIGITAL LIBRARY", 24f, 40f, paint)

                // Subtitle
                paint.color = Color.parseColor("#D4AF37")
                paint.textSize = 11f
                paint.isFakeBoldText = false
                canvas.drawText("Self Study Point • 66 Desks • Student Directory & Enrollment Report", 24f, 60f, paint)

                paint.color = Color.parseColor("#94A3B8")
                paint.textSize = 9.5f
                val todayFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                canvas.drawText("Generated: $todayFormatted | Billing Period: $monthName", 24f, 76f, paint)

                // 2. Summary Box on First Page
                var currentY = 115f
                if (pageIndex == 0) {
                    val occupied = students.size
                    val vacant = (66 - occupied).coerceAtLeast(0)
                    val paidCount = students.count { paymentMap[it.id]?.status == "PAID" }
                    val dueCount = occupied - paidCount

                    paint.color = Color.parseColor("#F1F5F9")
                    val rect = RectF(20f, currentY, (pageWidth - 20).toFloat(), currentY + 45f)
                    canvas.drawRoundRect(rect, 8f, 8f, paint)

                    paint.color = Color.parseColor("#0F172A")
                    paint.textSize = 10f
                    paint.isFakeBoldText = true
                    canvas.drawText("Total Seats: 66 Desks", 32f, currentY + 18f, paint)
                    canvas.drawText("Occupied: $occupied ($paidCount Paid • $dueCount Due)", 32f, currentY + 34f, paint)

                    canvas.drawText("Vacant Desks: $vacant", 280f, currentY + 18f, paint)
                    val totalRev = students.sumOf { (paymentMap[it.id]?.amount ?: it.monthlyFee) }
                    val collRev = students.sumOf { if (paymentMap[it.id]?.status == "PAID") (paymentMap[it.id]?.amount ?: it.monthlyFee) else 0.0 }
                    canvas.drawText("Revenue: ₹${collRev.toInt()} Coll. / ₹${totalRev.toInt()} Exp.", 280f, currentY + 34f, paint)

                    currentY += 60f
                }

                // 3. Table Header Row
                paint.color = Color.parseColor("#1E293B")
                canvas.drawRect(20f, currentY, (pageWidth - 20).toFloat(), currentY + 24f, paint)

                paint.color = Color.WHITE
                paint.textSize = 9.5f
                paint.isFakeBoldText = true

                canvas.drawText("Seat", 28f, currentY + 16f, paint)
                canvas.drawText("Student Name", 65f, currentY + 16f, paint)
                canvas.drawText("Contact Phone", 195f, currentY + 16f, paint)
                canvas.drawText("Enrolled", 295f, currentY + 16f, paint)
                canvas.drawText("Shift", 365f, currentY + 16f, paint)
                canvas.drawText("Target Goal", 440f, currentY + 16f, paint)
                canvas.drawText("Fee Status", 525f, currentY + 16f, paint)

                currentY += 24f

                // 4. Student Rows
                paint.isFakeBoldText = false
                paint.textSize = 9f

                for ((i, s) in chunk.withIndex()) {
                    val p = paymentMap[s.id]
                    val isPaid = p?.status == "PAID"

                    // Zebra Background
                    paint.color = if (i % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                    canvas.drawRect(20f, currentY, (pageWidth - 20).toFloat(), currentY + 28f, paint)

                    // Row divider line
                    paint.color = Color.parseColor("#E2E8F0")
                    canvas.drawLine(20f, currentY + 28f, (pageWidth - 20).toFloat(), currentY + 28f, paint)

                    // Text values
                    paint.color = Color.parseColor("#0F172A")
                    paint.isFakeBoldText = true
                    canvas.drawText("#${s.seatNumber}", 28f, currentY + 18f, paint)

                    paint.isFakeBoldText = false
                    val displayName = if (s.name.length > 20) s.name.take(18) + ".." else s.name
                    canvas.drawText(displayName, 65f, currentY + 18f, paint)

                    val phoneText = if (s.phone.isNotBlank()) s.phone else "--"
                    canvas.drawText(phoneText, 195f, currentY + 18f, paint)

                    canvas.drawText(DateUtils.formatDate(s.assignedDate), 295f, currentY + 18f, paint)

                    val shiftText = s.shift.split(" ").firstOrNull() ?: s.shift
                    canvas.drawText(shiftText, 365f, currentY + 18f, paint)

                    val examShort = if (s.examTarget.length > 14) s.examTarget.take(13) + ".." else s.examTarget
                    canvas.drawText(examShort, 440f, currentY + 18f, paint)

                    // Fee Status Badge
                    if (isPaid) {
                        paint.color = Color.parseColor("#10B981")
                        paint.isFakeBoldText = true
                        canvas.drawText("PAID", 530f, currentY + 18f, paint)
                    } else {
                        paint.color = Color.parseColor("#EF4444")
                        paint.isFakeBoldText = true
                        canvas.drawText("DUE", 530f, currentY + 18f, paint)
                    }

                    currentY += 28f
                }

                // 5. Page Footer
                paint.color = Color.parseColor("#64748B")
                paint.textSize = 8.5f
                paint.isFakeBoldText = false
                val footerY = (pageHeight - 20).toFloat()
                canvas.drawText("The Inspire Digital Library • Contact: +91 98765 43210 • inspirelibrary.com", 24f, footerY, paint)
                canvas.drawText("Page ${pageIndex + 1} of $totalPages", (pageWidth - 80).toFloat(), footerY, paint)

                pdfDocument.finishPage(page)
            }

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            shareFile(context, file, "application/pdf", "Open / Share Student Data PDF")
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting PDF: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Exports an official library branded payment slip PDF matching the user's rate chart and slip format.
     */
    fun exportPaymentSlipPdf(
        context: Context,
        student: StudentEntity,
        payment: PaymentEntity
    ): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val receiptNo = if (payment.receiptNumber.isNotBlank()) payment.receiptNumber else "INS-RCPT-${student.seatNumber}-${payment.paymentId}"
            val fileName = "Payment_Slip_${student.seatNumber}_$timeStamp.pdf"

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val pageWidth = 595
            val pageHeight = 842
            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            // Outer Border
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawRect(25f, 25f, (pageWidth - 25).toFloat(), (pageHeight - 25).toFloat(), paint)

            paint.style = Paint.Style.FILL

            // 1. Auspicious Invocations in Red
            paint.color = Color.parseColor("#DC2626") // Deep Red
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("श्री सरस्वत्यै नमः", 45f, 60f, paint)

            val ganeshText = "श्री गणेशाय नमः"
            val ganeshWidth = paint.measureText(ganeshText)
            canvas.drawText(ganeshText, (pageWidth - ganeshWidth) / 2f, 60f, paint)

            val rightSaraswati = "श्री सरस्वत्यै नमः"
            val rightWidth = paint.measureText(rightSaraswati)
            canvas.drawText(rightSaraswati, (pageWidth - 45f - rightWidth), 60f, paint)

            // 2. Yellow Header Capsule: "THE INSPIRE DIGITAL LIBRARY"
            paint.color = Color.parseColor("#FDE047") // Bright Golden Yellow
            val bannerRect = RectF(45f, 85f, (pageWidth - 45).toFloat(), 155f)
            canvas.drawRoundRect(bannerRect, 35f, 35f, paint)

            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(bannerRect, 35f, 35f, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 24f
            paint.isFakeBoldText = true
            val titleText = "THE INSPIRE DIGITAL LIBRARY"
            val titleWidth = paint.measureText(titleText)
            canvas.drawText(titleText, (pageWidth - titleWidth) / 2f, 130f, paint)

            // 3. Address
            paint.textSize = 13f
            paint.isFakeBoldText = true
            val addressText = "NEAR MAA DURGA TREDERS DESRI BIBHUTIPUR"
            val addressWidth = paint.measureText(addressText)
            canvas.drawText(addressText, (pageWidth - addressWidth) / 2f, 185f, paint)

            // 4. Subtitle: Payment Slip
            paint.textSize = 15f
            val slipTitle = "THE INSPIRE DIGITAL LIBRARY PAYMENT SLIP"
            val slipWidth = paint.measureText(slipTitle)
            canvas.drawText(slipTitle, (pageWidth - slipWidth) / 2f, 215f, paint)

            // 5. Student & Receipt Meta Box
            paint.color = Color.parseColor("#F8FAFC")
            val metaRect = RectF(45f, 235f, (pageWidth - 45).toFloat(), 315f)
            canvas.drawRoundRect(metaRect, 8f, 8f, paint)

            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(metaRect, 8f, 8f, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 11.5f
            paint.isFakeBoldText = true

            canvas.drawText("Receipt No: $receiptNo", 60f, 260f, paint)
            canvas.drawText("Student Name: ${student.name}", 60f, 280f, paint)
            canvas.drawText("Assigned Seat: SEAT #${student.seatNumber}", 60f, 300f, paint)

            canvas.drawText("Date: ${if (payment.paymentDate.isNotBlank()) payment.paymentDate else SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}", 330f, 260f, paint)
            canvas.drawText("Contact: ${student.phone}", 330f, 280f, paint)
            canvas.drawText("Month: ${payment.monthName}", 330f, 300f, paint)

            // 6. Shift Hours & Fee Table
            val tableTop = 335f
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRect(45f, tableTop, (pageWidth - 45).toFloat(), tableTop + 220f, paint)

            // Table Header: Hours category banner
            val plan = com.example.data.LibraryRateList.findPlanByShiftName(student.shift)
            val category = plan?.category ?: "HOURLY SHIFTS & LIBRARY TIMINGS"

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#9A3412") // Deep Rust
            paint.textSize = 13.5f
            paint.isFakeBoldText = true
            val catWidth = paint.measureText(category)
            canvas.drawText(category, (pageWidth - catWidth) / 2f, tableTop + 24f, paint)

            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawLine(45f, tableTop + 35f, (pageWidth - 45).toFloat(), tableTop + 35f, paint)

            // Subheaders: SL NO. | SHIFT | TIME AND PRICE
            canvas.drawLine(105f, tableTop + 35f, 105f, tableTop + 220f, paint)
            canvas.drawLine(340f, tableTop + 35f, 340f, tableTop + 220f, paint)
            canvas.drawLine(45f, tableTop + 70f, (pageWidth - 45).toFloat(), tableTop + 70f, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 12f
            paint.color = Color.parseColor("#B91C1C")
            paint.isFakeBoldText = true
            canvas.drawText("SL NO.", 52f, tableTop + 56f, paint)
            canvas.drawText("SHIFT", 190f, tableTop + 56f, paint)
            canvas.drawText("THE I. D. LIBRARY TIME AND PRICE", 348f, tableTop + 56f, paint)

            // Table Body
            paint.color = Color.BLACK
            paint.textSize = 13f
            paint.isFakeBoldText = true
            val slNumber = "${plan?.slNo ?: 1}."
            canvas.drawText(slNumber, 68f, tableTop + 120f, paint)

            val shiftDisplay = plan?.shiftName ?: student.shift.uppercase()
            canvas.drawText(shiftDisplay, 130f, tableTop + 120f, paint)

            val timingDisplay = plan?.timings ?: "6AM - 10PM"
            canvas.drawText(timingDisplay, 390f, tableTop + 105f, paint)

            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawLine(340f, tableTop + 130f, (pageWidth - 45).toFloat(), tableTop + 130f, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 17f
            paint.color = Color.parseColor("#0F172A")
            paint.isFakeBoldText = true
            canvas.drawText("₹${payment.amount.toInt()}/-", 410f, tableTop + 165f, paint)

            // Status strip inside table
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawLine(45f, tableTop + 185f, (pageWidth - 45).toFloat(), tableTop + 185f, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 11f
            paint.color = Color.parseColor("#047857") // Emerald
            canvas.drawText("PAYMENT STATUS: PAID • MODE: ${payment.paymentMode.uppercase()}", 60f, tableTop + 206f, paint)

            // 7. Signature Block
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 11f
            paint.isFakeBoldText = true
            val sigText = "MENTOR/AUTHOR SIGNATURE"
            val sigWidth = paint.measureText(sigText)
            canvas.drawText(sigText, pageWidth - 65f - sigWidth, 680f, paint)

            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawLine(pageWidth - 75f - sigWidth, 660f, pageWidth - 55f, 660f, paint)

            // Footer note
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 9f
            paint.isFakeBoldText = false
            canvas.drawText("This slip is a valid proof of seat reservation at The Inspire Digital Library.", 45f, (pageHeight - 40).toFloat(), paint)

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            shareFile(context, file, "application/pdf", "Open / Share Official Payment Slip PDF")
            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting payment slip PDF: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Dispatches standard Android Intent to view or share the generated CSV/PDF file using FileProvider.
     */
    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Toast.makeText(context, "Exported successfully: ${file.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "File saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }
}
