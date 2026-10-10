package com.infinity.linkleap.ui.screen.home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FileListScreen() {
    val files = listOf("Document1.pdf", "Notes.txt", "Presentation.pptx")
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(files) { file ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: pilih file */ }
                    .padding(16.dp)
            ) {
                Text(file, fontWeight = FontWeight.Medium)
            }
            Divider()
        }
    }
}