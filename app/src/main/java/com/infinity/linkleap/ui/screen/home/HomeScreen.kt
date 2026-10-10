package com.infinity.linkleap.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.infinity.linkleap.utils.FileUtils
import com.infinity.linkleap.ui.viewmodel.FilePickerViewModel
import com.infinity.linkleap.ui.viewmodel.SendViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreen(
    navController: NavHostController,
    sendViewModel: SendViewModel = hiltViewModel(),
    filePickerViewModel: FilePickerViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("FILE", "VIDEO", "APLIKASI", "FOTO", "MUSIK")
    val context = LocalContext.current

    // ✅ Get safe area insets
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(modifier = Modifier.fillMaxSize()) {
        // ✅ TabRow dengan safe area top
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFFF5F5F5),
            contentColor = Color(0xFF424242),
            modifier = Modifier.padding(top = statusBarHeight) // Safe area!
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) {
                                Color(0xFF424242)
                            } else {
                                Color(0xFF9E9E9E)
                            }
                        )
                    }
                )
            }
        }

        // ✅ Content dengan safe area bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navigationBarHeight) // Safe area!
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // FILE
                    FilePickerScreen(viewModel = filePickerViewModel, onSend = { pickedItems ->
                        CoroutineScope(Dispatchers.IO).launch {
                            pickedItems.forEach { item ->
                                try {
                                    val fileToSend: File? = when {
                                        item.uri != null -> FileUtils.uriToFile(context, item.uri)
                                        item.path != null -> File(item.path)
                                        else -> null
                                    }
                                    fileToSend?.let { f ->
                                        val targetIp = "192.168.49.1"
                                        val targetPort = 8888
                                        sendViewModel.sendFile(targetIp, targetPort, f)
                                    }
                                } catch (e: Exception) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(
                                            context,
                                            "Gagal kirim ${item.name}: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    })
                }

                1 -> VideoListScreen()
                2 -> AppListScreen(
                    sendViewModel = sendViewModel,
                    navController = navController
                )

                3 -> ImageListScreen()
                4 -> MusicListScreen()
            }
        }
    }
}