package com.fsscustomerapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.AssistanceHistoryItem
import com.fsscustomerapplication.data.remote.model.RenewalDetails
import com.fsscustomerapplication.data.remote.model.SpocData
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.AssistanceHistoryState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.RenewalState
import com.fsscustomerapplication.ui.viewmodels.SpocState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicencesDetailsScreen(
    licenseNo: String,
    customerId: Int? = null,          // ← pass customerId if available
    userId: Int? = null,              // ← pass userId if available
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLicences: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSpocChat: ((String) -> Unit)? = null
) {
    val viewModel: DashboardViewModel = viewModel()
    val renewalState by viewModel.renewalState
    val spocState by viewModel.spocState
    val assistanceState by viewModel.assistanceHistoryState

    LaunchedEffect(licenseNo) {
        viewModel.fetchRenewalDetails(licenseNo)
        viewModel.fetchSpoc(licenseNumber = licenseNo)

        // Fetch Assistance History
        viewModel.fetchAssistanceHistory(
            customerId = customerId,
            userId = userId
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Licences Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("View your license information", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            LicenceDetailsBottomNavigation(
                onHome = onNavigateToHome,
                onLicences = onNavigateToLicences,
                onReports = onNavigateToReports,
                onProfile = onNavigateToProfile
            )
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
                    val spocData = (spocState as? SpocState.Success)?.data
                    val historyList = (assistanceState as? AssistanceHistoryState.Success)?.data ?: emptyList()

                    if (data != null) {
                        LicenceDetailsContent(
                            data = data,
                            spocData = spocData,
                            historyList = historyList,
                            onNavigateToSpocChat = onNavigateToSpocChat,
                            onNavigateToReports = onNavigateToReports
                        )
                    } else {
                        Text(
                            text = "No data found",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is RenewalState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
fun LicenceDetailsContent(
    data: RenewalDetails,
    spocData: SpocData? = null,
    historyList: List<AssistanceHistoryItem> = emptyList(),
    onNavigateToSpocChat: ((String) -> Unit)? = null,
    onNavigateToReports: () -> Unit = {}
) {
    val context = LocalContext.current

    // ---- Dynamic SPOC values ----
    val spocName = spocData?.getEffectiveName()?.takeIf { it != "Not Assigned" }
        ?: data.getEffectiveSpocName().takeIf { it != "Not Assigned" }
        ?: "Not Assigned"

    val spocPhone = spocData?.getEffectivePhone()?.takeIf { it.isNotBlank() }
        ?: data.getEffectiveSpocPhone().takeIf { it.isNotBlank() }
        ?: ""

    val hasSpoc = spocPhone.isNotBlank() && !spocName.equals("Not Assigned", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Top Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = data.displayIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        error = painterResource(R.drawable.hand)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.displayProductName(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FssDarkBlue
                    )
                    Text(
                        text = data.displayPlanType(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "License Number",
                        fontSize = 10.sp,
                        color = FssBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = data.displayLicenseNumber(),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF138808))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = FssBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Authorized\nPartner",
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        color = FssBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Secondary Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailInfoItem("Purchase Date", "—", Modifier.weight(1f))
                    DetailInfoItem("Expiry Date", data.displayCurrentExpiry(), Modifier.weight(1f))
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFFF1F1F1)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailInfoItem("License Type", data.displayProductName(), Modifier.weight(1f))
                    DetailInfoItem("User Type", "Multi User", Modifier.weight(1f))
                }
            }
        }

        // 3. Assistance History (DYNAMIC)
        SectionHeaderDetails("Assistance History")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assistance history yet",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column {
                    historyList.forEachIndexed { index, item ->
                        val icon = when (item.type) {
                            "call" -> Icons.Default.Call
                            "ticket" -> Icons.Default.ConfirmationNumber
                            else -> Icons.Default.Info
                        }

                        val completedText = item.completedAt?.takeIf { it.isNotBlank() }?.let { "Done: $it" } ?: ""

                        HistoryItem(
                            icon = icon,
                            title = item.displayTitle(),
                            desc = item.displayDescription(),
                            date = item.displayDateTime(),
                            status = item.displayStatus(),
                            completedAt = completedText
                        )

                        if (index < historyList.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF1F1F1))
                        }
                    }
                }
            }
        }

        // 4. Addon Services
        SectionHeaderDetails("Addon Services")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                AddonItem(
                    Icons.Default.Cloud,
                    "TallyPrime Cloud Access",
                    "Access your Tally from anywhere"
                )
                HorizontalDivider(color = Color(0xFFF1F1F1))
                AddonItem(
                    Icons.Default.Shield,
                    "Annual Maintenance (AMC)",
                    "Onsite & Remote Support"
                )
            }
        }

        // 5. Assigned Engineer / SPOC Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF)),
            border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(FssBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = FssDarkBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        if (hasSpoc) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Assigned Service Engineer (SPOC)",
                            fontSize = 10.sp,
                            color = FssBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = spocName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FssDarkBlue
                        )
                        Text(
                            text = if (hasSpoc) "Handles your license & service support"
                            else "No engineer assigned yet",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (hasSpoc) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                SpocDetails.callSpoc(context, spocPhone, data.displayLicenseNumber())
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Engineer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val encName = Uri.encode(spocName)
                                    val encPhone = Uri.encode(spocPhone)
                                    val licKey = data.displayLicenseNumber()
                                    onNavigateToSpocChat?.invoke("spoc_chat/$licKey?spocName=$encName&spocPhone=$encPhone")
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat SPOC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Reports Section
        SectionHeaderDetails("Reports")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToReports() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(FssBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = null,
                        tint = FssBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "🚀 COMING SOON",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssBlue
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tally & Multi-Software Reports",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We are integrating direct API sync with Tally & accounting software to bring live reports, voucher creation logs, and sales insights directly to your app. Tap to learn more!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ===================== Helper Composables =====================

@Composable
fun ReportCardItem(
    title: String,
    category: String,
    description: String,
    status: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = iconColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2
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

@Composable
fun DetailInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = FssBlue)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

@Composable
fun SectionHeaderDetails(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = "View All >", fontSize = 12.sp, color = FssBlue, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistoryItem(
    icon: ImageVector,
    title: String,
    desc: String,
    date: String,
    status: String,
    completedAt: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    when (status.lowercase()) {
                        "resolved", "completed", "closed", "done" -> Color(0xFFE8F5E9)
                        "in progress" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE3F2FD)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = when (status.lowercase()) {
                    "resolved", "completed", "closed", "done" -> Color(0xFF4CAF50)
                    "in progress" -> Color(0xFFFF9800)
                    else -> FssBlue
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FssDarkBlue)
            Text(text = desc, fontSize = 11.sp, color = Color.Gray, maxLines = 2)
            if (completedAt.isNotBlank()) {
                Text(text = completedAt, fontSize = 9.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Medium)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = date, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = when (status.lowercase()) {
                    "resolved", "completed", "closed", "done" -> Color(0xFFE8F5E9)
                    "in progress" -> Color(0xFFFFF3E0)
                    else -> Color(0xFFE3F2FD)
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    color = when (status.lowercase()) {
                        "resolved", "completed", "closed", "done" -> Color(0xFF388E3C)
                        "in progress" -> Color(0xFFE65100)
                        else -> FssBlue
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddonItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FssBlue, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, fontSize = 10.sp, color = Color.Gray)
        }
        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
            Text(
                text = "Active",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 10.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
fun LicenceDetailsBottomNavigation(
    onHome: () -> Unit,
    onLicences: () -> Unit,
    onReports: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = false,
            onClick = onHome
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
            label = { Text("Licences") },
            selected = true,
            onClick = onLicences
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
            label = { Text("Reports") },
            selected = false,
            onClick = onReports
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Profile") },
            selected = false,
            onClick = onProfile
        )
    }
}