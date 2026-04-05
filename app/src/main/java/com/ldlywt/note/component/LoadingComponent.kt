package com.ldlywt.note.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LoadingComponent(
    isLoading: Boolean,
    isSuccess: Boolean = false,
    onFinished: () -> Unit = {}
) {
    var showSuccess by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(isLoading || isSuccess) }

    LaunchedEffect(isLoading, isSuccess) {
        if (!isLoading && isSuccess) {
            showSuccess = true
            delay(1000) // 显示打钩 1 秒
            showSuccess = false
            visible = false
            onFinished()
        } else if (!isLoading && !isSuccess) {
            visible = false
        } else {
            visible = true
            showSuccess = false
        }
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = true) { /* 拦截点击 */ },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showSuccess) {
                                val scale by animateFloatAsState(
                                    targetValue = 1.2f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scale(scale),
                                    tint = Color(0xFF4CAF50)
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(64.dp),
                                    color = Color(0xFF2196F3),
                                    strokeWidth = 6.dp,
                                    trackColor = Color(0xFFE0E0E0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
