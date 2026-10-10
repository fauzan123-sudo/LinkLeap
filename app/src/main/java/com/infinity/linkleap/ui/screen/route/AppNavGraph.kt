package com.infinity.linkleap.ui.screen.route

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.infinity.linkleap.ui.screen.MainScreen
import com.infinity.linkleap.ui.screen.receive.ReceiveScreen
import com.infinity.linkleap.ui.screen.home.AppListScreen
import com.infinity.linkleap.ui.screen.home.HomeScreen
import com.infinity.linkleap.ui.screen.send.SendProcessScreen
import com.infinity.linkleap.ui.screen.send.scanner.ScanQrScreen
import com.infinity.linkleap.ui.viewmodel.SendViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val sendViewModel: SendViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // 🏠 Halaman utama
        composable("main") {
            MainScreen(navController)
        }

        // 📤 Halaman pengiriman awal
        composable("home") {
            HomeScreen(navController)
        }

        // 📥 Halaman penerimaan
        composable("receive") {
            ReceiveScreen()
        }

        // 📱 Daftar aplikasi yang akan dikirim
        composable("app_list_screen") {
            AppListScreen(
                navController = navController,
                sendViewModel = sendViewModel
            )
        }

        // 🔍 Halaman Scan QR (perantara sebelum proses kirim)
        composable("send_qr_screen") {
            ScanQrScreen(
                navController = navController,
                sendViewModel = sendViewModel
            )
        }

        // ⚙️ Proses pengiriman (setelah QR berhasil di-scan)
        composable("send_process_screen") {
            SendProcessScreen(
                navController = navController,
                sendViewModel = sendViewModel
            )
        }
    }
}

