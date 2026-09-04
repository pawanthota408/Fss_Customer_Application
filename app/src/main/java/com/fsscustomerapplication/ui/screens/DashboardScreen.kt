package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.DashboardResponse
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: Int,
    onLogout: () -> Unit = {},
    onRenewClick: (String) -> Unit = {},           // license key / service key
    onRenewServiceClick: (ProductService) -> Unit = {}, // preferred: full service object
    onNavigateToServices: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToTickets: () -> Unit = {},
    onNavigateToItemDetails: (ProductService) -> Unit = {},
    onLicenceClick: (ProductService) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSpocChatClick: () -> Unit = {}
) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState
    val spocState by viewModel.spocState

    val firstLicKey = remember(uiState) {
        if (uiState is DashboardState.Success) {
            val d = (uiState as DashboardState.Success).data
            val lic = (d.mainLicenses?.ifEmpty { d.allLicences } ?: d.products)?.firstOrNull()
            val k = lic?.number ?: lic?.licenseKey ?: lic?.displayKey()
            k?.takeIf { it.isNotBlank() && it != "N/A" }
        } else null
    }

    LaunchedEffect(userId) {
        if (userId != -1) {
            viewModel.fetchDashboardData(userId)
            viewModel.fetchSpoc(customerId = userId)
        }
    }

    LaunchedEffect(firstLicKey) {
        if (userId != -1 && !firstLicKey.isNullOrBlank()) {
            viewModel.fetchSpoc(
                customerId = userId,
                licenseNumber = firstLicKey
            )
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.fsscustomerapplication.data.local.SessionManager(context) }
    var currentLangCode by remember { mutableStateOf(sessionManager.getLanguage()) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF4F7FC),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = FssDarkBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(Icons.Default.Translate, contentDescription = "Language", tint = FssDarkBlue)
                    }
                    IconButton(onClick = { viewModel.fetchDashboardData(userId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = FssDarkBlue)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = FssDarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            DashboardBottomNavigation(
                currentLangCode = currentLangCode,
                onServicesClick = onNavigateToServices,
                onReportsClick = onNavigateToReports,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FssDarkBlue
                    )
                }
                is DashboardState.Success -> {
                    val spocData = (spocState as? com.fsscustomerapplication.ui.viewmodels.SpocState.Success)?.data
                    DashboardContent(
                        data = state.data,
                        spocData = spocData,
                        currentLangCode = currentLangCode,
                        onRenewClick = onRenewClick,
                        onRenewServiceClick = onRenewServiceClick,
                        onServiceClick = onNavigateToItemDetails,
                        onProductClick = onNavigateToItemDetails,
                        onTicketsClick = onNavigateToTickets,
                        onLicenceClick = onLicenceClick,
                        onSpocChatClick = onSpocChatClick
                    )
                }
                is DashboardState.Error -> {
                    ServerErrorCard(
                        message = state.message,
                        onRetry = { viewModel.fetchDashboardData(userId) },
                        onCallSupport = { com.fsscustomerapplication.ui.screens.SpocDetails.callSpoc(context) }
                    )
                }
            }

            if (showLanguageDialog) {
                com.fsscustomerapplication.ui.components.LanguageSelectionDialog(
                    currentLanguageCode = currentLangCode,
                    onDismiss = { showLanguageDialog = false },
                    onLanguageSelected = { selected ->
                        com.fsscustomerapplication.utils.LanguageManager.applyLanguage(context, selected.code)
                        currentLangCode = selected.code
                        showLanguageDialog = false
                        com.fsscustomerapplication.utils.LanguageManager.findActivity(context)?.recreate()
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    data: DashboardResponse,
    spocData: com.fsscustomerapplication.data.remote.model.SpocData? = null,
    currentLangCode: String = "en",
    onRenewClick: (String) -> Unit,
    onRenewServiceClick: (ProductService) -> Unit = {},
    onServiceClick: (ProductService) -> Unit,
    onProductClick: (ProductService) -> Unit,
    onTicketsClick: () -> Unit,
    onLicenceClick: (ProductService) -> Unit,
    onSpocChatClick: () -> Unit = {}
) {
    // Catalogue
    val allItems = (data.products ?: emptyList()) + (data.services ?: emptyList())
    val productsCatalog = allItems.filter {
        val name = it.displayName().lowercase()
        name.contains("gold") || name.contains("silver") || name.contains("server")
    }
    val servicesCatalog = allItems.filter { service -> productsCatalog.none { (it.id == service.id) && (it.displayName() == service.displayName()) } }

    // ---- Main licences only (Tally products) ----
    val mainFromApi = (data.mainLicenses ?: emptyList()) + (data.tallyProducts ?: emptyList())
    val mainLicenses = mainFromApi
        .filter { isMainTallyProduct(it) }
        .distinctBy {
            (it.number ?: it.licenseKey ?: it.displayKey()).lowercase().trim()
        }

    // Fallback: build from flat licence_list if main_licenses empty
    val finalLicenses = mainLicenses.ifEmpty {
        (data.licences ?: emptyList()).map { lic ->
            ProductService(
                id = 0,
                name = lic.productName ?: "License",
                productName = lic.productName,
                number = lic.number,
                licenseKey = lic.number,
                status = lic.status,
                isMain = 1,
                type = "Product",
                iconLink = lic.iconLink
            )
        }.filter { isMainTallyProduct(it) || it.type.equals("Product", true) }
            .distinctBy { (it.number ?: it.licenseKey ?: "").lowercase() }
    }

    // ---- Expiring services (for countdown / renew) – NOT main Tally products ----
    val expiringServices = (data.expiringServices ?: data.ownedServices ?: emptyList())
        .filter { !isMainTallyProduct(it) }
        .filter { !it.expiryDate.isNullOrBlank() && it.expiryDate != "N/A" }
        .sortedBy { it.expiryDate }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderSection(
                name = data.customer?.name ?: "User",
                company = data.customer?.company ?: "",
                currentLangCode = currentLangCode
            )
        }

        item {
            StatsSection(
                activeServices = data.customer?.activeServices ?: 0,
                pendingRequests = data.tickets?.size ?: data.customer?.pendingRequests ?: 0,
                invoicesCount = data.customer?.invoicesCount ?: 0,
                currentLangCode = currentLangCode,
                onTicketsClick = onTicketsClick
            )
        }

        // Countdown for soonest expiring SERVICE
        item {
            val soonestService = expiringServices.minByOrNull { it.expiryDate ?: "9999-12-31" }
            if (soonestService != null) {
                LiveServiceCountdownCard(
                    service = soonestService,
                    onRenewClick = {
                        onRenewServiceClick(soonestService)
                        onRenewClick(soonestService.number ?: soonestService.licenseKey ?: soonestService.displayKey())
                    }
                )
            } else {
                AllSetCard(currentLangCode = currentLangCode)
            }
        }

        if (productsCatalog.isNotEmpty()) {
            item { SectionHeader(com.fsscustomerapplication.utils.LanguageManager.tr("our_products", currentLangCode)) }
            item { ProductServiceRow(productsCatalog.take(8)) { onProductClick(it) } }
        }

        if (servicesCatalog.isNotEmpty()) {
            item { SectionHeader(com.fsscustomerapplication.utils.LanguageManager.tr("our_services", currentLangCode)) }
            item { ProductServiceRow(servicesCatalog.take(8)) { onServiceClick(it) } }
        }

        item { OffersBanner() }

        // ---- Assigned Service Engineer / SPOC Card (Above Licences) ----
        item {
            val spocName = spocData?.getEffectiveName()?.takeIf { it != "Not Assigned" } ?: SpocDetails.NAME
            val spocPhone = spocData?.getEffectivePhone()?.takeIf { it.isNotBlank() } ?: SpocDetails.PHONE

            DashboardSpocCard(
                spocName = spocName,
                spocPhone = spocPhone,
                onChatClick = onSpocChatClick
            )
        }

        // ---- Your Licences (name only, NO expiry) ----
        if (finalLicenses.isNotEmpty()) {
            item {
                SectionHeader(com.fsscustomerapplication.utils.LanguageManager.tr("your_licences", currentLangCode))
                Text(
                    text = "You have ${finalLicenses.size} active licence(s)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    finalLicenses.forEach { lic ->
                        DashboardLicenceCard(licence = lic, onClick = onLicenceClick)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

/**
 * Licence card – name + key + status only (NO expiry)
 */
@Composable
fun DashboardLicenceCard(
    licence: ProductService,
    onClick: (ProductService) -> Unit
) {
    val productName = listOfNotNull(
        licence.productName?.takeIf { it.isNotBlank() },
        licence.name?.takeIf { it.isNotBlank() },
        licence.displayName().takeIf { it.isNotBlank() && it != "N/A" }
    ).firstOrNull() ?: "License"

    val licenseKey = listOfNotNull(
        licence.number?.takeIf { it.isNotBlank() },
        licence.licenseKey?.takeIf { it.isNotBlank() },
        licence.displayKey().takeIf { it.isNotBlank() && it != "N/A" }
    ).firstOrNull() ?: ""

    val isActive = licence.status.equals("Active", true) ||
            licence.status.equals("active", true)

    val statusColor = if (isActive) Color(0xFF2E7D32) else Color.Red
    val statusBgColor = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(licence) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = licence.iconLink ?: licence.displayIcon(),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                error = painterResource(R.drawable.hand),
                placeholder = painterResource(R.drawable.hand),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (licenseKey.isNotBlank()) {
                    Text(
                        text = "Key: $licenseKey",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                color = statusBgColor,
                shape = CircleShape
            ) {
                Text(
                    text = licence.status ?: "Active",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

/**
 * Countdown for an expiring SERVICE (not main Tally product)
 */
@Composable
fun LiveServiceCountdownCard(
    service: ProductService,
    onRenewClick: () -> Unit
) {
    val expiryDateStr = service.expiryDate ?: service.validTill
    if (expiryDateStr.isNullOrBlank() || expiryDateStr == "N/A") {
        AllSetCard()
        return
    }

    // Support yyyy-MM-dd and dd/MM/yyyy
    val expiryDate = remember(expiryDateStr) {
        parseFlexibleDate(expiryDateStr)
    }
    if (expiryDate == null) {
        AllSetCard()
        return
    }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(expiryDate) {
        while (currentTime < expiryDate.time) {
            currentTime = System.currentTimeMillis()
            delay(1000.milliseconds)
        }
    }

    val diff = expiryDate.time - currentTime
    val days = if (diff > 0) diff / (24 * 60 * 60 * 1000) else 0L
    val hours = if (diff > 0) (diff / (60 * 60 * 1000)) % 24 else 0L
    val minutes = if (diff > 0) (diff / (60 * 1000)) % 60 else 0L
    val seconds = if (diff > 0) (diff / 1000) % 60 else 0L

    // Only show countdown if within 30 days (or already expired)
    if (diff > 0 && days > 30) {
        AllSetCard()
        return
    }

    RenewalCountdownCard(
        title = service.displayName(),
        subtitle = "Your service is expiring soon",
        licence = service.number ?: service.licenseKey ?: service.displayKey(),
        days = days,
        hours = hours,
        min = minutes,
        sec = seconds,
        onRenewClick = onRenewClick
    )
}

@Composable
fun HeaderSection(name: String, company: String, currentLangCode: String = "en") {
    val greeting = getGreeting()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greeting 👋",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = name,
                    color = Color.Black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (company.isNotEmpty()) {
                    Text(
                        text = company,
                        color = FssDarkBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = com.fsscustomerapplication.utils.LanguageManager.tr("welcome_dashboard", currentLangCode),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = FssDarkBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.hand),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSection(
    activeServices: Int,
    pendingRequests: Int,
    invoicesCount: Int,
    currentLangCode: String = "en",
    onTicketsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = com.fsscustomerapplication.utils.LanguageManager.tr("active_services", currentLangCode),
            value = activeServices.toString(),
            icon = Icons.Default.Shield,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            title = com.fsscustomerapplication.utils.LanguageManager.tr("support_tickets", currentLangCode),
            value = pendingRequests.toString(),
            icon = Icons.Default.ConfirmationNumber,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f),
            onClick = onTicketsClick
        )
        StatsCard(
            title = com.fsscustomerapplication.utils.LanguageManager.tr("invoices", currentLangCode),
            value = invoicesCount.toString(),
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun DashboardSpocCard(
    spocName: String,
    spocPhone: String,
    onChatClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showActionDialog by remember { mutableStateOf(false) }

    if (showActionDialog) {
        com.fsscustomerapplication.ui.components.WhatsAppActionDialog(
            spocName = spocName,
            spocPhone = spocPhone,
            onDismiss = { showActionDialog = false },
            onOpenInAppChat = onChatClick
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF)),
        border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FssBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = FssDarkBlue,
                        modifier = Modifier.size(26.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .align(Alignment.BottomEnd)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assigned Service Engineer (SPOC)",
                        fontSize = 11.sp,
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
                        text = "Your dedicated FSS engineer for licence support",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { SpocDetails.callSpoc(context, spocPhone) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Engineer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showActionDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.whatsapp),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chat / WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ServerErrorCard(
    message: String,
    onRetry: () -> Unit,
    onCallSupport: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Server Connection Issue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FssDarkBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCallSupport,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FssBlue)
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = null,
                            tint = FssBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Call Support",
                            fontSize = 12.sp,
                            color = FssBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FssDarkBlue)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductServiceRow(items: List<ProductService>, onClick: (ProductService) -> Unit = {}) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items) { item ->
            ProductServiceCard(item, onClick)
        }
    }
}

@Composable
fun ProductServiceCard(item: ProductService, onClick: (ProductService) -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick(item) }
    ) {
        Card(
            modifier = Modifier.size(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = item.displayIcon(),
                    contentDescription = item.displayName(),
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit,
                    error = painterResource(R.drawable.hand),
                    placeholder = painterResource(R.drawable.hand)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.displayName(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = FssDarkBlue,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AllSetCard(currentLangCode: String = "en") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC8E6C9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF2E7D32)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = com.fsscustomerapplication.utils.LanguageManager.tr("all_set", currentLangCode),
                    color = Color(0xFF1B5E20),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = com.fsscustomerapplication.utils.LanguageManager.tr("all_set_sub", currentLangCode),
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun RenewalCountdownCard(
    title: String = "Renewal Countdown",
    subtitle: String = "Your service is expiring soon",
    licence: String,
    days: Long,
    hours: Long,
    min: Long,
    sec: Long,
    onRenewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFDADA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Red
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.Red,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (licence.isNotBlank()) "$subtitle ($licence)" else subtitle,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountdownItem(value = days.toString().padStart(2, '0'), label = "Days")
                        Text(":", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
                        CountdownItem(value = hours.toString().padStart(2, '0'), label = "Hrs")
                        Text(":", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
                        CountdownItem(value = min.toString().padStart(2, '0'), label = "Min")
                        Text(":", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
                        CountdownItem(value = sec.toString().padStart(2, '0'), label = "Sec")
                    }
                    Button(
                        onClick = onRenewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Renew Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.Red,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Text(text = label, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OffersBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1)),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 0f)
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "🌟 EXCLUSIVE OFFERS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Special Offers Just for You!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    lineHeight = 22.sp
                )
                Text(
                    text = "Get up to 30% off on renewals",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DashboardBottomNavigation(
    currentLangCode: String = "en",
    onServicesClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text(com.fsscustomerapplication.utils.LanguageManager.tr("home", currentLangCode), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = FssDarkBlue,
                indicatorColor = FssDarkBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text(com.fsscustomerapplication.utils.LanguageManager.tr("my_licences", currentLangCode), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            selected = false,
            onClick = onServicesClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FssDarkBlue,
                selectedTextColor = FssDarkBlue,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text(com.fsscustomerapplication.utils.LanguageManager.tr("reports", currentLangCode).ifBlank { "Reports" }, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            selected = false,
            onClick = onReportsClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FssDarkBlue,
                selectedTextColor = FssDarkBlue,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text(com.fsscustomerapplication.utils.LanguageManager.tr("profile", currentLangCode), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = FssDarkBlue,
                indicatorColor = FssDarkBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}

// ---------- Helpers ----------

private fun isMainTallyProduct(ps: ProductService): Boolean {
    if ((ps.isMain ?: 0) == 1) return true
    if (ps.type.equals("Product", true)) return true
    val n = ps.displayName()
    return n.contains("Tally", true) && !isServiceOrAddonName(n)
}

private fun isServiceOrAddonName(name: String?): Boolean {
    val n = name.orEmpty()
    return n.contains("AMC", true) ||
            n.contains("TSS", true) ||
            n.contains("Support", true) ||
            n.contains("TDL", true) ||
            n.contains("Website", true) ||
            n.contains("WhatsApp", true) ||
            n.contains("Biz Analyst", true) ||
            n.contains("Cloud", true) ||
            n.startsWith("License ", true)
}

private fun parseFlexibleDate(dateStr: String): Date? {
    val formats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "d/M/yyyy",
        "dd MMM yyyy",
        "dd-MM-yyyy"
    )
    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.US)
            sdf.isLenient = false
            val d = sdf.parse(dateStr.trim()) ?: continue
            // End of day for date-only
            if (fmt.length <= 10) {
                val cal = Calendar.getInstance()
                cal.time = d
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                return cal.time
            }
            return d
        } catch (_: Exception) { }
    }
    return null
}

fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..20 -> "Good Evening"
        else -> "Good Night"
    }
}