package com.imnotndesh.yubalkt.ui.utils

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(showShimmer: Boolean = true): Brush {
    if (!showShimmer) return Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "shimmer"
    )
    return Brush.linearGradient(
        colors = listOf(Color.LightGray.copy(alpha = 0.3f), Color.LightGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.3f)),
        start = Offset(translateAnim - 200f, 0f), end = Offset(translateAnim, 0f)
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(shimmerBrush()))
}

@Composable
fun KpiCardSkeleton() {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        SkeletonBox(modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            SkeletonBox(modifier = Modifier.size(60.dp, 8.dp))
            Spacer(Modifier.height(4.dp))
            SkeletonBox(modifier = Modifier.size(80.dp, 6.dp))
        }
    }
}

@Composable
fun JobCardSkeleton() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        SkeletonBox(modifier = Modifier.size(48.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
            Spacer(Modifier.height(6.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp))
        }
        SkeletonBox(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun SubscriptionCardSkeleton() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        SkeletonBox(modifier = Modifier.size(56.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
            Spacer(Modifier.height(6.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp))
        }
        SkeletonBox(modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(8.dp))
        SkeletonBox(modifier = Modifier.size(28.dp))
    }
}
