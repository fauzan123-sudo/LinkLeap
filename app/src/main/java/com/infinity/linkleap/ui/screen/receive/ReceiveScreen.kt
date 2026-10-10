package com.infinity.linkleap.ui.screen.receive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.infinity.linkleap.ui.viewmodel.ReceiveViewModel
import com.infinity.linkleap.utils.getLocalIpAddress

@Composable
fun ReceiveScreen(
    receiveViewModel: ReceiveViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ipAddress = remember { mutableStateOf(getLocalIpAddress()) }
    val port by receiveViewModel.serverPort.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        receiveViewModel.startServer()
    }

    val qrData = remember(port) {
        if (port != null) "${ipAddress.value}:$port" else ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tunggu pengirim untuk scan QR berikut", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        if (port != null) {
            AndroidView(
                modifier = Modifier.size(220.dp),
                factory = { ctx ->
                    val barcodeEncoder = com.journeyapps.barcodescanner.BarcodeEncoder()
                    val bitmap = barcodeEncoder.encodeBitmap(
                        qrData,
                        com.google.zxing.BarcodeFormat.QR_CODE,
                        400,
                        400
                    )
                    android.widget.ImageView(ctx).apply {
                        setImageBitmap(bitmap)
                    }
                }
            )

            Spacer(Modifier.height(16.dp))
            Text("IP: ${ipAddress.value}\nPort: $port", textAlign = TextAlign.Center)
        } else {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Menyiapkan server...")
        }
    }
}

