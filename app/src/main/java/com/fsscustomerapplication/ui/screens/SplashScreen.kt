package com.fsscustomerapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fsscustomerapplication.R
import com.fsscustomerapplication.ui.theme.*
import kotlinx.coroutines.delay

import kotlin.time.Duration.Companion.seconds

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit = {}) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // High-end entrance animation
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(2.seconds)
        onNavigateToLogin()
    }

    SplashContent(alpha = alpha.value, scale = scale.value)
}

@Composable
private fun SplashContent(alpha: Float, scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(FssGradientStart, FssGradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        SplashBackgroundWaves()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .alpha(alpha)
                .scale(scale)
        ) {
            // Hand Logo from Drawables as requested
            Image(
                painter = painterResource(R.drawable.hand),
                contentDescription = "FSS Logo",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "FRIENDS",
                fontSize = 54.sp,
                fontWeight = FontWeight.Black, // Strong/Bold as requested
                color = FssDarkBlue,
                letterSpacing = 2.sp,
                lineHeight = 54.sp
            )

            Text(
                text = "SOFTWARE SOLUTIONS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FssRed,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = "Your Friendly IT Partner",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = FssDarkBlue.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .alpha(alpha)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FSSCUSTOMERAPPLICATIONTheme {
        SplashContent(alpha = 1f, scale = 1f)
    }
}

@Composable
private fun SplashBackgroundWaves() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top decorative waves
        for (i in 0..10) {
            val offset = i * 35f
            val path = Path().apply {
                moveTo(0f, height * 0.1f + offset)
                cubicTo(
                    width * 0.3f, height * 0.05f + offset,
                    width * 0.7f, height * 0.2f + offset,
                    width, height * 0.15f + offset
                )
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.1f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Bottom decorative waves
        for (i in 0..15) {
            val offset = i * 25f
            val path = Path().apply {
                moveTo(0f, height * 0.8f + offset)
                cubicTo(
                    width * 0.4f, height * 0.7f + offset,
                    width * 0.6f, height * 0.9f + offset,
                    width, height * 0.85f + offset
                )
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.08f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
