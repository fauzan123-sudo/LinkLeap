package com.infinity.linkleap.ui.screen.receive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.infinity.linkleap.data.model.TransferStatus
import com.infinity.linkleap.ui.common.composable.TransferProgressView
import com.infinity.linkleap.ui.viewmodel.ReceiveViewModel
import com.infinity.linkleap.utils.getLocalIpAddress

@Composable
fun ReceiveScreen(
    onDone: () -> Unit,
    receiveViewModel: ReceiveViewModel = hiltViewModel()
) {
    val ipAddress = remember { mutableStateOf(getLocalIpAddress()) }
    val port by receiveViewModel.serverPort.collectAsState(initial = null)
    val status by receiveViewModel.transferStatus.collectAsState()

    LaunchedEffect(Unit) { receiveViewModel.startServer() }

    val qrData = remember(port, ipAddress.value) {
        if (port != null) "${ipAddress.value}:$port" else ""
    }

    val transferStarted = status is TransferStatus.Progress ||
            status is TransferStatus.Completed ||
            status is TransferStatus.Error
    val finished = status is TransferStatus.Completed || status is TransferStatus.Error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            port == null -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Menyiapkan server...")
            }

            !transferStarted -> {
                Text(
                    "Tunggu pengirim untuk scan QR berikut",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))

                AndroidView(
                    modifier = Modifier.size(220.dp),
                    factory = { ctx ->
                        val bitmap = com.journeyapps.barcodescanner.BarcodeEncoder().encodeBitmap(
                            qrData,
                            com.google.zxing.BarcodeFormat.QR_CODE,
                            400,
                            400
                        )
                        android.widget.ImageView(ctx).apply { setImageBitmap(bitmap) }
                    }
                )

                Spacer(Modifier.height(16.dp))
                Text("IP: ${ipAddress.value}\nPort: $port", textAlign = TextAlign.Center)
            }

            else -> {
                TransferProgressView(status = status, isSending = false)
                if (finished) {
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onDone) { Text("Selesai") }
                }
            }
        }
    }
}