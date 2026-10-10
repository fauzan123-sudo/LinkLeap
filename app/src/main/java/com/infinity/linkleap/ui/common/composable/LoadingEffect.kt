package com.infinity.linkleap.ui.common.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Memuat...",
    indicatorSize: Dp = 48.dp,
    indicatorColor: Color = Color(0xFF2196F3),
    strokeWidth: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = Color.Gray,
    spaceBetween: Dp = 16.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(indicatorSize),
            color = indicatorColor,
            strokeWidth = strokeWidth
        )
        Spacer(modifier = Modifier.height(spaceBetween))
        Text(
            text = message,
            style = textStyle,
            color = textColor
        )
    }
}