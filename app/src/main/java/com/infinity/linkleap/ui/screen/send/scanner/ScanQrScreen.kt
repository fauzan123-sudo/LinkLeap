package com.infinity.linkleap.ui.screen.send.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.infinity.linkleap.ui.common.composable.findActivity
import com.infinity.linkleap.ui.viewmodel.SendViewModel
import timber.log.Timber

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrScreen(
    navController: NavHostController,
    sendViewModel: SendViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ Get Activity correctly
    val activity = context.findActivity()

    // States
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isScanning by remember { mutableStateOf(true) }
    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    LaunchedEffect(sendViewModel) {
        Timber.d("📱 ScanQrScreen ViewModel: ${sendViewModel.hashCode()}")
        Timber.d("   Current files: ${sendViewModel.selectedFiles.value.size}")
    }

    // ✅ Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Timber.d("Permission result: $isGranted")
        hasCameraPermission = isGranted

        if (!isGranted) {
            // Check if permanently denied
            activity?.let {
                val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.CAMERA
                )
                permissionDeniedPermanently = !shouldShow
                Timber.w("Permission denied. Should show rationale: $shouldShow")
            }
        }
    }

    // ❌ Jika belum dapat izin kamera
    if (!hasCameraPermission) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Scan QR Code") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = if (permissionDeniedPermanently)
                        "Izin Kamera Ditolak"
                    else
                        "Izin Kamera Diperlukan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (permissionDeniedPermanently) {
                        "Anda telah menolak izin kamera secara permanen. " +
                                "Silakan aktifkan di pengaturan aplikasi."
                    } else {
                        "Aplikasi memerlukan akses kamera untuk memindai QR Code."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                if (permissionDeniedPermanently) {
                    // Tombol ke Settings
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Buka Pengaturan")
                    }
                } else {
                    // Tombol request permission
                    Button(
                        onClick = {
                            Timber.d("Requesting camera permission...")
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Izinkan Kamera")
                    }
                }
            }
        }
        return
    }

    // ✅ Preview kamera dan QR scanner
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        isScanning = false
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Camera Preview
            CameraPreviewWithScanner(
                lifecycleOwner = lifecycleOwner,
                isScanning = isScanning,
                onScanSuccess = { ip, port ->
                    Timber.i("🟢 QR Scanned: $ip:$port")
                    isScanning = false

                    // ✅ CEK DATA SEBELUM NAVIGATE
                    Timber.d("📂 Files before navigate: ${sendViewModel.selectedFiles.value.size}")

                    sendViewModel.setConnection(ip, port)
                    navController.navigate("send_process_screen") {
                        popUpTo("send_qr_screen") { inclusive = true }
                    }
                }
            )

            // Scanning Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(1.dp))

                // Scanning Frame
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(
                            width = 3.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    // Corner indicators
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cornerLength = 40f
                        val strokeWidth = 8f
                        val color = Color.Green

                        // Top-left
                        drawLine(
                            color,
                            Offset(0f, 0f),
                            Offset(cornerLength, 0f),
                            strokeWidth,
                            StrokeCap.Round
                        )
                        drawLine(
                            color,
                            Offset(0f, 0f),
                            Offset(0f, cornerLength),
                            strokeWidth,
                            StrokeCap.Round
                        )

                        // Top-right
                        drawLine(
                            color,
                            Offset(size.width, 0f),
                            Offset(size.width - cornerLength, 0f),
                            strokeWidth,
                            StrokeCap.Round
                        )
                        drawLine(
                            color,
                            Offset(size.width, 0f),
                            Offset(size.width, cornerLength),
                            strokeWidth,
                            StrokeCap.Round
                        )

                        // Bottom-left
                        drawLine(
                            color,
                            Offset(0f, size.height),
                            Offset(cornerLength, size.height),
                            strokeWidth,
                            StrokeCap.Round
                        )
                        drawLine(
                            color,
                            Offset(0f, size.height),
                            Offset(0f, size.height - cornerLength),
                            strokeWidth,
                            StrokeCap.Round
                        )

                        // Bottom-right
                        drawLine(
                            color,
                            Offset(size.width, size.height),
                            Offset(size.width - cornerLength, size.height),
                            strokeWidth,
                            StrokeCap.Round
                        )
                        drawLine(
                            color,
                            Offset(size.width, size.height),
                            Offset(size.width, size.height - cornerLength),
                            strokeWidth,
                            StrokeCap.Round
                        )
                    }
                }

                // Instruction
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Arahkan kamera ke QR Code",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}


