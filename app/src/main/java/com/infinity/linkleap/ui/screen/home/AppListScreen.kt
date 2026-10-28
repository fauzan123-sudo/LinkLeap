package com.infinity.linkleap.ui.screen.home

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.infinity.linkleap.data.model.AppInfo
import com.infinity.linkleap.data.model.SendItem
import com.infinity.linkleap.ui.common.composable.LoadingIndicator
import com.infinity.linkleap.ui.screen.send.installed.AppItem
import com.infinity.linkleap.ui.screen.send.installed.BottomActionBar
import com.infinity.linkleap.ui.screen.send.installed.CategoryHeader
import com.infinity.linkleap.ui.viewmodel.SendViewModel
import com.infinity.linkleap.utils.categorizeApps
import com.infinity.linkleap.utils.findLocalApkFiles
import com.infinity.linkleap.utils.loadInstalledApps
import com.infinity.linkleap.utils.toSafeFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppListScreen(
    sendViewModel: SendViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    // Permission handler
    val permissions = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        else listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    )

    // State untuk loading
    var isLoading by remember { mutableStateOf(true) }

    // State untuk toggle Terinstal/Tak terpasang
    var showInstalled by remember { mutableStateOf(true) }

    // State untuk expand/collapse sections
    var expandedSections by remember { mutableStateOf(setOf("Baru")) }

    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var categorized by remember { mutableStateOf<Map<String, List<AppInfo>>>(emptyMap()) }
    var apkFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedApps by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) {
            isLoading = true
            // Load data di background
            withContext(Dispatchers.IO) {
                apps = loadInstalledApps(context)
                categorized = categorizeApps(apps, context)
                apkFiles = findLocalApkFiles(context)
            }
            isLoading = false
        } else {
            permissions.launchMultiplePermissionRequest()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Toggle Button: Terinstal / Tak terpasang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Button Terinstal
            Button(
                onClick = { showInstalled = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showInstalled) Color.White else Color.Transparent,
                    contentColor = if (showInstalled) Color.Black else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (showInstalled) 2.dp else 0.dp
                )
            ) {
                Text(
                    "Terinstal",
                    fontWeight = if (showInstalled) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Button Tak terpasang
            Button(
                onClick = { showInstalled = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showInstalled) Color.White else Color.Transparent,
                    contentColor = if (!showInstalled) Color.Black else Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (!showInstalled) 2.dp else 0.dp
                )
            ) {
                Text(
                    "Tak terpasang",
                    fontWeight = if (!showInstalled) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Content dengan Loading State
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    LoadingIndicator()
                }

                // Tampilkan konten setelah loading selesai
                showInstalled -> {
                    // Tampilan Aplikasi Terinstal dengan Expandable Sections
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        listOf("Baru", "Aplikasi", "Lainnya", "Widget").forEach { category ->
                            val appsInCategory = categorized[category] ?: emptyList()
                            val isExpanded = category in expandedSections

                            // Section Header
                            item {
                                CategoryHeader(
                                    title = category,
                                    count = appsInCategory.size,
                                    isExpanded = isExpanded,
                                    onToggle = {
                                        expandedSections = if (isExpanded) {
                                            expandedSections - category
                                        } else {
                                            expandedSections + category
                                        }
                                    }
                                )
                            }

                            // Apps Grid (hanya tampil jika expanded)
                            if (isExpanded) {
                                items(appsInCategory.chunked(4)) { rowApps ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowApps.forEach { app ->
                                            AppItem(
                                                app = app,
                                                isSelected = app.packageName in selectedApps,
                                                onToggleSelect = {
                                                    selectedApps =
                                                        if (app.packageName in selectedApps) {
                                                            selectedApps - app.packageName
                                                        } else {
                                                            selectedApps + app.packageName
                                                        }
                                                },
                                                onSend = {
                                                    val apk = File(app.apkPath)
//                                                    sendViewModel.dataFile(
//                                                        apk
//                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        // Fill empty spaces
                                        repeat(4 - rowApps.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }

                else -> {
                    // Tampilan APK Tidak Terinstal
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(apkFiles.chunked(4)) { rowFiles ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowFiles.forEach { file ->
//                                    ApkFileItem(
//                                        file = file,
//                                        onSend = {
//                                            sendViewModel.sendFile(targetIp, targetPort, file)
//                                            Toast.makeText(
//                                                context,
//                                                "Mengirim ${file.name}",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        },
//                                        modifier = Modifier.weight(1f)
//                                    )
                                }
                                repeat(4 - rowFiles.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Bar (jika ada item yang dipilih)
        if (selectedApps.isNotEmpty()) {
            BottomActionBar(
                selectedCount = selectedApps.size,
                onSendAll = {
                    val items = selectedApps.mapNotNull { pkg ->
                        apps.find { it.packageName == pkg }?.let {
                            SendItem(path = it.apkPath, name = "${it.name}.apk".toSafeFileName())
                        }
                    }
                    Timber.d("AppListScreen VM ${sendViewModel.hashCode()} -> ${items.size} file")

                    sendViewModel.setSelectedFiles(items)
                    navController.navigate("send_qr_screen")
                    selectedApps = emptySet()
                }
            )
        }
    }
}



