package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.RenewalDetails
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.RenewalState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewalSummaryScreen(
    licenseNumber: String,
    serviceId: Int? = null,
    productId: Int? = null,
    onBack: () -> Unit,
    onPayNow: (RenewalDetails) -> Unit = {},
    onCallClick: () -> Unit = {},
    onWhatsAppClick: () -> Unit = {},
) {
    val viewModel: DashboardViewModel = viewModel()
    val renewalState by viewModel.renewalState

    LaunchedEffect(licenseNumber, serviceId, productId) {
        viewModel.fetchRenewalDetails(
            licenseNumber = licenseNumber,
            id = serviceId,
            productId = productId
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Renewal Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FssDarkBlue
                        )
                        Text(
                            "Review your service renewal",
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
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.NotificationsNone, null, tint = FssDarkBlue)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, null, tint = FssDarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            when (val state = renewalState) {
                is RenewalState.Success -> {
                    val data = state.data.data
                    if (data != null && data.renewable != false) {
                        RenewalBottomBar(
                            onCallClick = onCallClick,
                            onWhatsAppClick = onWhatsAppClick,
                            onPayClick = { onPayNow(data) }
                        )
                    }
                }
                else -> {}
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFF))
        ) {
            when (val state = renewalState) {
                is RenewalState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FssDarkBlue
                    )
                }
                is RenewalState.Success -> {
                    val data = state.data.data
                    if (data != null) {
                        if (data.renewable == false) {
                            NotRenewableContent(
                                message = data.message
                                    ?: "This item cannot be renewed. Only services and addons are renewable.",
                                productName = data.productName,
                                onBack = onBack
                            )
                        } else {
                            RenewalSummaryContent(data)
                        }
                    } else {
                        Text(
                            text = "No renewal data available",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is RenewalState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            null,
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(state.message, color = Color.Red, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onBack) { Text("Go Back") }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun NotRenewableContent(
    message: String,
    productName: String?,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Block, null, tint = Color(0xFFE65100), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(productName ?: "Item", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = FssDarkBlue)
        Spacer(modifier = Modifier.height(6.dp))
        Text(message, fontSize = 13.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)) {
            Text("Go Back")
        }
    }
}

@Composable
fun RenewalSummaryContent(data: RenewalDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LicenseInfoCard(data)
        RenewalCountdownCardSmall(expiryStr = data.displayCurrentExpiry(), daysLeft = data.displayDaysLeft())
        PricingSummaryCard(data)

        data.message?.let { msg ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Icon(Icons.Default.Info, null, tint = FssDarkBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(msg, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 14.sp)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF138808), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Renewed for 1 Year after successful payment.",
                fontSize = 10.sp,
                color = Color.DarkGray,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LicenseInfoCard(data: RenewalDetails) {
    val name = data.displayProductName()
    val days = data.displayDaysLeft()
    val isExpired = (data.isExpired == true) || (days < 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F7FA)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = data.displayIcon(),
                        contentDescription = name,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit,
                        error = painterResource(R.drawable.hand)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FssDarkBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isExpired) Color.Red else Color(0xFF138808))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                DetailRowSummary("License", data.displayLicenseNumber())
                DetailRowSummary("Plan", data.displayPlanType())
                DetailRowSummary("Valid Till", data.displayCurrentExpiry())
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Days badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isExpired) Color(0xFFFFEBEE) else Color(0xFFF2F2F2))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (isExpired) "Expired" else "Expires",
                        fontSize = 8.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = kotlin.math.abs(days).toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Red
                    )
                    Text("Days", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRowSummary(label: String, value: String) {
    Row(
        modifier = Modifier.padding(top = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.width(62.dp)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            color = FssDarkBlue,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun parseExpiryToMillis(expiryStr: String?): Long {
    if (expiryStr.isNullOrBlank() || expiryStr == "N/A" || expiryStr == "—") return System.currentTimeMillis() + 864000000L
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("dd MMM yyyy", Locale.US)
    )
    for (fmt in formats) {
        try {
            val date = fmt.parse(expiryStr.trim())
            if (date != null) return date.time
        } catch (_: Exception) {}
    }
    return System.currentTimeMillis() + 864000000L
}

@Composable
fun RenewalCountdownCardSmall(expiryStr: String?, daysLeft: Int = 0) {
    val targetMillis = remember(expiryStr) { parseExpiryToMillis(expiryStr) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(targetMillis) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000.milliseconds)
        }
    }

    val diff = (targetMillis - currentTime).coerceAtLeast(0L)
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    val hours = ((diff / (1000 * 60 * 60)) % 24).toInt()
    val minutes = ((diff / (1000 * 60)) % 60).toInt()
    val sec = ((diff / 1000) % 60).toInt()

    val isUrgent = days <= 7 || diff <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) Color(0xFFFFF0F1) else Color(0xFFFFF8E1)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isUrgent) Color.Red else Color(0xFFF57C00)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (diff <= 0) "Service Expired" else "Live Renewal Countdown",
                    color = if (isUrgent) Color.Red else Color(0xFFE65100),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (diff <= 0) "Renew to restore access" else "Don't miss your renewal date",
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CountdownText(days.toString(), "Days")
                    CountdownText(hours.toString().padStart(2, '0'), "Hrs")
                    CountdownText(minutes.toString().padStart(2, '0'), "Min")
                    CountdownText(sec.toString().padStart(2, '0'), "Sec")
                }
            }
        }
    }
}

@Composable
fun CountdownText(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.Red, fontWeight = FontWeight.Black, fontSize = 15.sp)
        Text(label, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PricingSummaryCard(data: RenewalDetails) {
    val subtotal = data.subtotal
    val gst = data.gst
    val total = data.total
    val discount = data.discount ?: 0.0

    Column {
        Text(
            "Renewal Summary",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = FssDarkBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                PriceRow("Service", data.productName ?: data.serviceName ?: "—")
                PriceRow("Plan Type", data.planType ?: "Service | 1 Year")
                PriceRow("New Valid Till", data.nextExpiry ?: "—", highlight = true)

                data.proformaNo?.let {
                    PriceRow("Proforma No.", it)
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFFF1F1F1)
                )

                PriceRow("Subtotal", "₹ ${String.format(Locale.US, "%.2f", subtotal)}", isBold = true)
                if (discount > 0) {
                    PriceRow("Discount", "- ₹ ${String.format(Locale.US, "%.2f", discount)}", color = Color(0xFF138808))
                }
                PriceRow(
                    "GST (${data.gstPercent ?: 18}%)",
                    "₹ ${String.format(Locale.US, "%.2f", gst)}",
                    color = Color.Gray
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFFF1F1F1)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FssDarkBlue)
                    Text(
                        "₹ ${String.format(Locale.US, "%.2f", total)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun PriceRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.DarkGray,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = if (highlight) Color(0xFF138808) else color,
            fontSize = 12.sp,
            fontWeight = if (isBold || highlight) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RenewalBottomBar(
    onCallClick: () -> Unit = {},
    onWhatsAppClick: () -> Unit = {},
    onPayClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCallClick,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)
            ) {
                Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Button(
                onClick = onWhatsAppClick,
                modifier = Modifier
                    .weight(1.15f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(
                    painter = painterResource(R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color.White)
            }

            Button(
                onClick = onPayClick,
                modifier = Modifier
                    .weight(1.15f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF528FF0))
            ) {
                Icon(Icons.Default.Payment, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pay Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}