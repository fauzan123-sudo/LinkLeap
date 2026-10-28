package com.infinity.linkleap.ui.screen.send.scanner

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber
import java.util.concurrent.Executors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import java.util.concurrent.atomic.AtomicBoolean

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithScanner(
    lifecycleOwner: LifecycleOwner,
    isScanning: Boolean,
    onScanSuccess: (String, Int) -> Unit
) {
    val context = LocalContext.current

    // Remember resources untuk cleanup
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    val handled = remember { AtomicBoolean(false) }
    val scanning by rememberUpdatedState(isScanning)
    val currentOnScan by rememberUpdatedState(onScanSuccess)

    DisposableEffect(Unit) {
        onDispose {
            Timber.d("Cleaning up camera resources")
            try {
                cameraExecutor.shutdown()
                scanner.close()
            } catch (e: Exception) {
                Timber.e(e, "Error cleaning up camera")
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (!scanning || handled.get()) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { value ->
                                    Timber.i("QR Code detected: $value")

                                    // Format: "192.168.49.1:8888"
                                    if (value.contains(":")) {
                                        val parts = value.split(":")
                                        val ip = parts.getOrNull(0)
                                        val port = parts.getOrNull(1)?.toIntOrNull()

                                        if (ip != null && port != null) {
                                            if (handled.compareAndSet(false, true)) {
                                                currentOnScan(ip, port)
                                            }
                                        } else {
                                            Timber.w("Invalid QR format: $value")
                                        }
                                    } else {
                                        Timber.w("QR does not contain ':' separator: $value")
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "Barcode scanning failed")
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            // Bind camera lifecycle
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )

                    Timber.i("Camera bound successfully")

                } catch (e: Exception) {
                    Timber.e(e, "Camera binding failed")
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}