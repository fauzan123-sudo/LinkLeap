package com.infinity.linkleap.ui.common.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.infinity.linkleap.data.model.TransferStatus
import com.infinity.linkleap.utils.formatBytes
import com.infinity.linkleap.utils.formatEta

@Composable
fun TransferProgressView(
    status: TransferStatus,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (status) {
            is TransferStatus.Progress -> ProgressContent(status, isSending)
            is TransferStatus.Completed -> SuccessContent(isSending)
            is TransferStatus.Error -> ErrorContent(status.message)
            else -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    if (isSending) "Menghubungkan ke penerima..." else "Menunggu pengirim...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ProgressContent(s: TransferStatus.Progress, isSending: Boolean) {
    val target = if (s.total > 0) s.sent.toFloat() / s.total else 0f
    val animated by animateFloatAsState(target, animationSpec = tween(300), label = "progress")

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animated },
            modifier = Modifier.size(160.dp),
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            "${(animated * 100).toInt()}%",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(Modifier.height(20.dp))
    Text(
        if (isSending) "Mengirim file..." else "Menerima file...",
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "${formatBytes(s.sent)} / ${formatBytes(s.total)}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (s.speedBps > 0) {
        Spacer(Modifier.height(2.dp))
        Text(
            "${formatBytes(s.speedBps)}/dtk" +
                    if (s.etaSeconds >= 0) " • sisa ${formatEta(s.etaSeconds)}" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessContent(isSending: Boolean) {
    val haptic = LocalHapticFeedback.current
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Icon(
        Icons.Default.CheckCircle,
        contentDescription = null,
        tint = Color(0xFF4CAF50),
        modifier = Modifier
            .size(120.dp)
            .scale(scale.value)
    )
    Spacer(Modifier.height(16.dp))
    Text(
        if (isSending) "Berhasil terkirim!" else "File berhasil diterima!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    if (!isSending) {
        Spacer(Modifier.height(4.dp))
        Text(
            "Tersimpan di Download/LinkLeap",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Icon(
        Icons.Default.Error,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(96.dp)
    )
    Spacer(Modifier.height(16.dp))
    Text(
        "Transfer gagal",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}