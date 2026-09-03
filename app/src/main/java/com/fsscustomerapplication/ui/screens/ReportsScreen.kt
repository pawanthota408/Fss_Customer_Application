package com.fsscustomerapplication.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.fsscustomerapplication.R
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue

data class UpcomingReportFeature(
    val title: String,
    val category: String,
    val description: String,
    val badgeText: String,
    val icon: ImageVector,
    val iconColor: Color
)

val upcomingReportFeatures = listOf(
    UpcomingReportFeature(
        title = "Vouchers Created & Manipulated Log",
        category = "Audit & Activity",
        description = "Track daily voucher creation, modified entries, and employee manipulation logs in real-time.",
        badgeText = "Live Audit Trail",
        icon = Icons.Default.Receipt,
        iconColor = Color(0xFF2196F3)
    ),
    UpcomingReportFeature(
        title = "Sales & Invoices Analytics",
        category = "Revenue",
        description = "Detailed breakdown of monthly sales, invoices generated, and revenue growth charts.",
        badgeText = "Sales Dashboard",
        icon = Icons.Default.TrendingUp,
        iconColor = Color(0xFF4CAF50)
    ),
    UpcomingReportFeature(
        title = "Outstanding Ledger Balances",
        category = "Receivables",
        description = "Customer ledger balance tracking, pending receivables, and automated payment reminders.",
        badgeText = "Aging Analysis",
        icon = Icons.Default.AccountBalanceWallet,
        iconColor = Color(0xFFFF9800)
    ),
    UpcomingReportFeature(
        title = "GST & Tax Filing Summary",
        category = "Tax Compliance",
        description = "GSTR-1, GSTR-3B return status, e-Invoicing reconciliation, and e-Way bill logs.",
        badgeText = "Taxation",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        iconColor = Color(0xFF9C27B0)
    ),
    UpcomingReportFeature(
        title = "Biz Analyst Mobile Sync",
        category = "Mobile Integration",
        description = "24/7 direct Tally business reports and live analytics on your smartphone.",
        badgeText = "Tally API Sync",
        icon = Icons.Default.Analytics,
        iconColor = Color(0xFF00BCD4)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    userId: Int,
    onBack: () -> Unit,
    onRequestEarlyAccess: () -> Unit = {}
) {
    val context = LocalContext.current

    // Lottie Composition
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://assets9.lottiefiles.com/packages/lf20_qvq5v63x.json")
    )

    // Pulsing transition animation as fallback
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tally & Business Reports",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FssDarkBlue
                        )
                        Text(
                            text = "Multi-software & Tally analytics",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FssDarkBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFF)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Animated Lottie / Hero Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(pulseScale),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Lottie Animation Box
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(FssBlue.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (lottieComposition != null) {
                                LottieAnimation(
                                    composition = lottieComposition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier.size(120.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = FssBlue,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "🚀 COMING SOON WITH LIVE TALLY API SYNC",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FssBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Real-Time Reports & Voucher Analytics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FssDarkBlue,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "We are integrating direct API synchronization with Tally & accounting software to bring live reports, voucher creation logs, and sales insights directly to your app.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onRequestEarlyAccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Early Access via SPOC", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Upcoming Report Modules",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            // 2. Upcoming Report Cards List
            items(upcomingReportFeatures) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRequestEarlyAccess() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(report.iconColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(report.icon, contentDescription = null, tint = report.iconColor, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = report.iconColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = report.badgeText.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = report.iconColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = report.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FssDarkBlue
                            )

                            Text(
                                text = report.description,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
