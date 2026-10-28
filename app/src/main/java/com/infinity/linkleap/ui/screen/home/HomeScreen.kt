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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.infinity.linkleap.data.model.SendItem
import com.infinity.linkleap.ui.viewmodel.FilePickerViewModel
import com.infinity.linkleap.ui.viewmodel.SendViewModel
import com.infinity.linkleap.utils.FileUtils
import com.infinity.linkleap.utils.toSafeFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun HomeScreen(
    navController: NavHostController,
    sendViewModel: SendViewModel,                       // dari AppNavGraph, tanpa default
    filePickerViewModel: FilePickerViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("FILE", "VIDEO", "APLIKASI", "FOTO", "MUSIK")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFFF5F5F5),
            contentColor = Color(0xFF424242),
            modifier = Modifier.padding(top = statusBarHeight)
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
                            color = if (selectedTabIndex == index) Color(0xFF424242) else Color(0xFF9E9E9E)
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navigationBarHeight)
        ) {
            when (selectedTabIndex) {
                0 -> FilePickerScreen(viewModel = filePickerViewModel, onSend = { pickedItems ->
                    scope.launch {
                        val sendItems = withContext(Dispatchers.IO) {
                            pickedItems.mapNotNull { item ->
                                try {
                                    when {
                                        item.uri != null -> {
                                            val f = FileUtils.uriToFile(context, item.uri)
                                            SendItem(f.absolutePath, f.name)
                                        }
                                        item.path != null -> SendItem(
                                            item.path,
                                            if (item.type == "app") "${item.name}.apk".toSafeFileName()
                                            else item.name.toSafeFileName()
                                        )
                                        else -> null
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Gagal menyiapkan ${item.name}")
                                    null
                                }
                            }
                        }

                        if (sendItems.isEmpty()) {
                            Toast.makeText(context, "Gagal menyiapkan file", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        sendViewModel.setSelectedFiles(sendItems)
                        filePickerViewModel.clearAll()
                        navController.navigate("send_qr_screen")
                    }
                })

                1 -> VideoListScreen()
                2 -> AppListScreen(sendViewModel = sendViewModel, navController = navController)
                3 -> ImageListScreen()
                4 -> MusicListScreen()
            }
        }
    }
}