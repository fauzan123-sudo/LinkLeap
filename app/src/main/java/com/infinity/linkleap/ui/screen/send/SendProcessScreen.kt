package com.infinity.linkleap.ui.screen.send

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.infinity.linkleap.data.model.TransferStatus
import com.infinity.linkleap.ui.common.composable.TransferProgressView
import com.infinity.linkleap.ui.viewmodel.SendViewModel
import com.infinity.linkleap.utils.Resource
import timber.log.Timber

@Composable
fun SendProcessScreen(
    navController: NavHostController,
    sendViewModel: SendViewModel
) {
    val repoStatus by sendViewModel.transferStatus.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Error yang muncul sebelum transfer dimulai (mis. koneksi belum diset)
    val status: TransferStatus = errorMessage?.let { TransferStatus.Error(it) } ?: repoStatus
    val finished = status is TransferStatus.Completed || status is TransferStatus.Error

    LaunchedEffect(Unit) {
        sendViewModel.sendAllFiles().collect { result ->
            if (result is Resource.Error) {
                Timber.e("Send error: ${result.message}")
                errorMessage = result.message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TransferProgressView(status = status, isSending = true)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (finished) navController.popBackStack("home", false)
                else navController.popBackStack()
            },
            colors = if (finished) ButtonDefaults.buttonColors()
            else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(if (finished) "Selesai" else "Batalkan")
        }
    }
}