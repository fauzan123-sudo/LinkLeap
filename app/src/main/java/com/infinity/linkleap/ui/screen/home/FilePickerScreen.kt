package com.infinity.linkleap.ui.screen.home

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.infinity.linkleap.data.model.PickedItem
import com.infinity.linkleap.ui.viewmodel.FilePickerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilePickerScreen(
    viewModel: FilePickerViewModel,
    onSend: (List<PickedItem>) -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.pickedItems.collectAsState()

    val tabs = listOf("Aplikasi", "Gambar", "Video", "File")
    var selectedTab by remember { mutableIntStateOf(0) }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.addItem(
                PickedItem(
                    name = uri.lastPathSegment ?: "File",
                    uri = uri,
                    path = null,
                    type = when (selectedTab) {
                        1 -> "image"
                        2 -> "video"
                        else -> "file"
                    }
                )
            )
        }
    }

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Pilih file untuk dikirim", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (selectedTab) {
            0 -> { // APP
                val apps = remember { viewModel.getInstalledApps(context) }
                LazyVerticalGrid(columns = GridCells.Adaptive(120.dp)) {
                    items(apps) { app ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .height(80.dp),
                            onClick = { viewModel.addItem(app) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = app.name,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                val type = when (selectedTab) {
                    1 -> "image/*"
                    2 -> "video/*"
                    else -> "*/*"
                }
                Button(
                    onClick = { pickFileLauncher.launch(type) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Pilih ${tabs[selectedTab]}")
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Dipilih: ${items.size} item")

        LazyVerticalGrid(columns = GridCells.Adaptive(120.dp)) {
            items(items) { item ->
                Card(Modifier.padding(6.dp)) {
                    Text(
                        item.name,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (items.isNotEmpty()) {
                    onSend(items)
                } else {
                    Toast.makeText(context, "Belum ada file dipilih", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Kirim Sekarang")
        }
    }
}