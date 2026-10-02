package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun CameraQrScanner(
    onQrDecoded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Camera controls state
    var isTorchEnabled by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var cameraControlInstance by remember { mutableStateOf<Camera?>(null) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var showScanPulse by remember { mutableStateOf(false) }

    // Cooldown timestamp to prevent duplicate scans in a short burst
    var lastScannedTime by remember { mutableStateOf(0L) }

    // Animated laser beam for scanning visual indicator
    val infiniteTransition = rememberInfiniteTransition(label = "laserScan")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPos"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_qr_scanner_box"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                // CameraX Preview via AndroidView
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val now = System.currentTimeMillis()
                                if (now - lastScannedTime > 2500) {
                                    val rotation = imageProxy.imageInfo.rotationDegrees
                                    val decoded = decodeQrWithRotation(imageProxy, rotation)
                                    if (decoded != null && decoded.isNotBlank()) {
                                        lastScannedTime = now
                                        triggerFeedback(ctx)
                                        previewView.post {
                                            lastScannedCode = decoded
                                            showScanPulse = true
                                            onQrDecoded(decoded)
                                        }
                                    }
                                }
                                imageProxy.close()
                            }

                            try {
                                cameraProvider.unbindAll()
                                val cameraSelector = if (useFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControlInstance = camera
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    update = {
                        // Update torch state when toggled
                        cameraControlInstance?.cameraControl?.enableTorch(isTorchEnabled)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Golden Viewfinder Scanning Reticle with Laser
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (showScanPulse) 3.dp else 1.5.dp,
                            color = if (showScanPulse) EmeraldPresent else GoldPrimary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 4.5.dp.toPx()
                        val cornerLen = 30.dp.toPx()
                        val cornerColor = if (showScanPulse) {
                            androidx.compose.ui.graphics.Color(0xFF10B981)
                        } else {
                            androidx.compose.ui.graphics.Color(0xFFD4AF37)
                        }

                        // Top-Left corner
                        drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
                        drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

                        // Top-Right corner
                        drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeW)
                        drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeW)

                        // Bottom-Left corner
                        drawLine(cornerColor, Offset(0f, size.height), Offset(cornerLen, size.height), strokeW)
                        drawLine(cornerColor, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeW)

                        // Bottom-Right corner
                        drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeW)
                        drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeW)

                        // Moving Emerald Scanning Laser
                        val y = size.height * laserPosition
                        drawLine(
                            color = androidx.compose.ui.graphics.Color(0xFF10B981),
                            start = Offset(8.dp.toPx(), y),
                            end = Offset(size.width - 8.dp.toPx(), y),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                // Top Guidance Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(Color(0xDD0F172A), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldPresent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CameraX Live QR Scanner Active",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Floating Camera Controls (Torch & Flip)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color(0xCC0F172A), RoundedCornerShape(24.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Torch Toggle Button
                    IconButton(
                        onClick = {
                            isTorchEnabled = !isTorchEnabled
                            cameraControlInstance?.cameraControl?.enableTorch(isTorchEnabled)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Torch",
                            tint = if (isTorchEnabled) GoldPrimary else Color.White
                        )
                    }

                    Text(
                        text = if (isTorchEnabled) "Flash ON" else "Flash OFF",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Camera Switch (Front/Back)
                    IconButton(
                        onClick = {
                            useFrontCamera = !useFrontCamera
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }

            } else {
                // Camera Permission Required Box
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Permission",
                        tint = GoldPrimary,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Camera Access Required",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan student QR barcodes in real time with CameraX to record attendance timestamps in local Room database.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Decodes QR code from CameraX ImageProxy using ZXing, with buffer rotation support.
 */
private fun decodeQrWithRotation(imageProxy: ImageProxy, rotationDegrees: Int): String? {
    return try {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val origWidth = imageProxy.width
        val origHeight = imageProxy.height

        val (rotatedData, finalWidth, finalHeight) = rotateYuv(bytes, origWidth, origHeight, rotationDegrees)

        val source = PlanarYUVLuminanceSource(
            rotatedData, finalWidth, finalHeight, 0, 0, finalWidth, finalHeight, false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        val result = reader.decodeWithState(binaryBitmap)
        result.text
    } catch (e: Exception) {
        null
    }
}

/**
 * Rotates raw YUV data based on the sensor's rotation degrees (90, 180, 270)
 * for optimal ZXing recognition in portrait and landscape orientations.
 */
private fun rotateYuv(
    data: ByteArray,
    width: Int,
    height: Int,
    rotationDegrees: Int
): Triple<ByteArray, Int, Int> {
    return when (rotationDegrees) {
        90 -> {
            val rotated = ByteArray(data.size)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    rotated[x * height + (height - y - 1)] = data[y * width + x]
                }
            }
            Triple(rotated, height, width)
        }
        180 -> {
            val rotated = ByteArray(data.size)
            val total = width * height
            for (i in 0 until total) {
                rotated[total - 1 - i] = data[i]
            }
            Triple(rotated, width, height)
        }
        270 -> {
            val rotated = ByteArray(data.size)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    rotated[(width - x - 1) * height + y] = data[y * width + x]
                }
            }
            Triple(rotated, height, width)
        }
        else -> Triple(data, width, height)
    }
}

/**
 * Triggers audio tone beep and haptic feedback when attendance is punched.
 */
private fun triggerFeedback(context: Context) {
    try {
        // Haptic vibration
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(140, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(140)
            }
        }

        // Crisp kiosk confirmation beep
        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 160)
    } catch (e: Exception) {
        // Ignore audio/vibration errors
    }
}
