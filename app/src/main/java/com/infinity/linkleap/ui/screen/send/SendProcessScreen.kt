package com.infinity.linkleap.ui.screen.send

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.infinity.linkleap.ui.viewmodel.SendViewModel
import com.infinity.linkleap.utils.Resource
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun SendProcessScreen(
    navController: NavHostController,
    sendViewModel: SendViewModel
) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    var message by remember { mutableStateOf("Menyiapkan pengiriman...") }
    val selectedFiles by sendViewModel.selectedFiles.collectAsState()
    val connection by sendViewModel.connection.collectAsState()

    // ✅ Log hashCode ViewModel
    LaunchedEffect(sendViewModel) {
        Timber.d("📱 SendProcessScreen ViewModel: ${sendViewModel.hashCode()}")
    }

    // ✅ HANYA SATU LaunchedEffect untuk kirim file
    LaunchedEffect(Unit) {
        Timber.d("🚀 SendProcessScreen started")
        Timber.d("   Files: ${selectedFiles.size}")
        selectedFiles.forEach { Timber.d("    - $it") }
        Timber.d("   Connection: $connection")

        if (selectedFiles.isEmpty()) {
            message = "❌ Tidak ada file yang dipilih"
            Timber.e("❌ No files selected!")
            return@LaunchedEffect
        }

        if (connection == null) {
            message = "❌ Koneksi belum diatur"
            Timber.e("❌ No connection info!")
            return@LaunchedEffect
        }

        // Kirim file
        sendViewModel.sendAllFiles().collect { result ->
            when (result) {
                is Resource.Loading -> {
                    message = "Mengirim file..."
                    Timber.d("⏳ Loading...")
                }

                is Resource.Success -> {
                    message = "✅ Berhasil dikirim!"
                    progress = 1f
                    Timber.d("✅ Success: ${result.data}")
                    delay(1000)
                    // navController.popBackStack("app_list_screen", false)
                }

                is Resource.Error -> {
                    message = result.message ?: "Terjadi kesalahan"
                    Timber.e("❌ Error: ${result.message}")
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(80.dp),
                color = ProgressIndicatorDefaults.circularColor,
                strokeWidth = 6.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Dibatalkan", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Batalkan", color = Color.White)
            }
        }
    }
}




