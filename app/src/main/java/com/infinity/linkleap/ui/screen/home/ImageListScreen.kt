package com.infinity.linkleap.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun ImageListScreen() {
    val context = LocalContext.current
    var images by remember { mutableStateOf(listOf<String>()) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) images = loadImages(context)
        }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            images = loadImages(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(images) { image ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: pilih gambar */ }
                    .padding(16.dp)
            ) {
                Text(image)
            }
            Divider()
        }
    }
}

private fun loadImages(context: Context): List<String> {
    val imageList = mutableListOf<String>()
    val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection, null, null, null
    )
    cursor?.use {
        val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        while (it.moveToNext()) {
            imageList.add(it.getString(nameIndex))
        }
    }
    return imageList
}
