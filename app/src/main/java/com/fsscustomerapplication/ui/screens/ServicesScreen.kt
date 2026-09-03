package com.fsscustomerapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.LicensesUiState
import com.fsscustomerapplication.ui.viewmodels.LicensesViewModel
import com.fsscustomerapplication.ui.viewmodels.SpocState
import com.fsscustomerapplication.utils.tr
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    userId: Int,
    selectedProductId: Int? = null,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onGetServiceSupport: (ProductService) -> Unit,
    onPayWithRazorpay: (ProductService) -> Unit = {},   // ← wire to your Razorpay flow
) {
    val viewModel: LicensesViewModel = viewModel()
    val uiState by viewModel.uiState
    val spocState by viewModel.spocState
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedLicence by remember { mutableStateOf<ProductService?>(null) }
    var showLicenceDetails by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<ProductService?>(null) }  // service detail

    LaunchedEffect(userId) {
        viewModel.fetchLicensesServices(userId)
    }

    LaunchedEffect(selectedLicence) {
        selectedLicence?.let { lic ->
            viewModel.fetchSpoc(
                customerId = userId,
                licenseNumber = lic.displayKey(),
                id = lic.id
            )
        }
    }

    LaunchedEffect(uiState, selectedProductId) {
        if (uiState is LicensesUiState.Success && (selectedProductId != null) && (selectedProductId > 0)) {
            val state = uiState as LicensesUiState.Success
            val found = state.mainProducts.find { it.id == selectedProductId }
            if (found != null) {
                selectedLicence = found
                showLicenceDetails = true
            }
        }
    }

    BackHandler(enabled = selectedService != null || selectedLicence != null || showLicenceDetails) {
        when {
            selectedService != null -> selectedService = null
            showLicenceDetails && selectedLicence != null -> showLicenceDetails = false
            selectedLicence != null -> selectedLicence = null
            else -> onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val title = when {
                            selectedService != null -> "Service Details"
                            showLicenceDetails && selectedLicence != null -> "Licence Details"
                            selectedLicence != null -> selectedLicence?.displayName() ?: "My Licences"
                            else -> "My Licences"
                        }
                        val subtitle = when {
                            selectedService != null -> "View service information"
                            selectedLicence == null -> "View all your licences"
                            showLicenceDetails -> "View your license information"
                            else -> "Services for this licence"
                        }
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            selectedService != null -> selectedService = null
                            showLicenceDetails && selectedLicence != null -> showLicenceDetails = false
                            selectedLicence != null -> selectedLicence = null
                            else -> onBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FssDarkBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = FssDarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            when {
                selectedService != null -> {
                    // Service detail bottom bar – Call / WhatsApp / Pay if expiring
                    ServiceDetailsBottomBar(
                        service = selectedService!!,
                        onCallClick = { SpocDetails.callSpoc(context) },
                        onWhatsAppClick = { SpocDetails.openWhatsAppSpoc(context, "Inquiry for service ${selectedService!!.displayName()}") },
                        onPayClick = { onPayWithRazorpay(selectedService!!) }
                    )
                }
                else -> {
                    ServicesBottomNavigation(
                        onHome = onNavigateToHome,
                        onReports = onNavigateToReports,
                        onProfile = onNavigateToProfile
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFF))
        ) {
            when (val state = uiState) {
                is LicensesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FssDarkBlue
                    )
                }
                is LicensesUiState.Success -> {
                    when {
                        selectedService != null -> {
                            ServiceDetailsScreen(
                                service = selectedService!!,
                                onPayWithRazorpay = onPayWithRazorpay
                            )
                        }
                        selectedLicence == null -> {
                            LicencesList(licences = state.mainProducts) { licence ->
                                selectedLicence = licence
                                showLicenceDetails = true
                            }
                        }
                        showLicenceDetails -> {
                            val spocData = (spocState as? SpocState.Success)?.data
                            LicenceDetailsScreen(
                                licence = selectedLicence!!,
                                spocData = spocData,
                                onViewServices = { showLicenceDetails = false },
                                onServiceClick = { service ->
                                    selectedService = service
                                },
                                onPayWithRazorpay = onPayWithRazorpay
                            )
                        }
                        else -> {
                            ServicesForLicenceScreen(
                                licence = selectedLicence!!,
                                allCatalogServices = state.catalogServices,
                                attachedServicesForLicence = selectedLicence!!.nestedServices(),
                                onSupportClick = { service ->
                                    if (service.isOwned == true || selectedLicence!!.nestedServices()
                                            .any { it.id == service.id || it.displayName() == service.displayName() }
                                    ) {
                                        selectedService = service
                                    } else {
                                        onGetServiceSupport(service)
                                    }
                                },
                                onBack = { selectedLicence = null },
                                onViewDetails = { showLicenceDetails = true }
                            )
                        }
                    }
                }
                is LicensesUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------
 * Helpers – expiry within 30 days
 * ------------------------------------------------------------------ */
fun parseExpiryDate(dateStr: String?): Calendar? {
    if (dateStr.isNullOrBlank()) return null
    val formats = listOf(
        "dd/MM/yyyy",
        "d/M/yyyy",
        "yyyy-MM-dd",
        "dd MMM yyyy",
        "dd-MM-yyyy"
    )
    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
            sdf.isLenient = false
            val date = sdf.parse(dateStr.trim()) ?: continue
            return Calendar.getInstance().apply { time = date }
        } catch (_: Exception) { }
    }
    return null
}

fun isExpiringWithinDays(expiryDateStr: String?, days: Int = 30): Boolean {
    val expiry = parseExpiryDate(expiryDateStr) ?: return false
    val now = Calendar.getInstance()
    val diffMillis = expiry.timeInMillis - now.timeInMillis
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return diffDays in 0..days.toLong()
}

fun daysUntilExpiry(expiryDateStr: String?): Long? {
    val expiry = parseExpiryDate(expiryDateStr) ?: return null
    val now = Calendar.getInstance()
    val diffMillis = expiry.timeInMillis - now.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(diffMillis)
}

/* ------------------------------------------------------------------
 * Licence Details Screen
 * ------------------------------------------------------------------ */
@Composable
fun LicenceDetailsScreen(
    licence: ProductService,
    spocData: com.fsscustomerapplication.data.remote.model.SpocData? = null,
    onViewServices: () -> Unit,
    onServiceClick: (ProductService) -> Unit = {},
    onPayWithRazorpay: (ProductService) -> Unit = {}
) {
    val isActive = licence.status.equals("Active", true) || licence.status.equals("active", true)
    val attachedServices = licence.services ?: emptyList()
    val attachedAddons = licence.addons ?: emptyList()
    val allAttached = (attachedServices + attachedAddons).distinctBy { it.id to it.displayName() }
    val totalAttached = allAttached.size

    // Only services expiring within next 30 days
    val expiringServices = remember(allAttached) {
        allAttached.filter { isExpiringWithinDays(it.expiryDate, 30) }
    }

    val assistanceHistory = emptyList<AssistanceItem>()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LicenceInfoCard(
                licence = licence,
                totalAttached = totalAttached,
                isActive = isActive,
                onViewServices = onViewServices
            )
        }

        if (expiringServices.isNotEmpty()) {
            item {
                ExpiringServicesCard(
                    services = expiringServices,
                    onPayClick = onPayWithRazorpay
                )
            }
        }

        item {
            AssistanceHistorySection(
                history = assistanceHistory,
                onViewAll = { }
            )
        }

        if (allAttached.isNotEmpty()) {
            item {
                AddonServicesSection(
                    services = allAttached,
                    onViewAll = onViewServices,
                    onServiceClick = onServiceClick
                )
            }
        }

        // Assigned Engineer / SPOC Card
        item {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val spocName = spocData?.getEffectiveName()?.takeIf { it != "Not Assigned" }
                ?: licence.getEffectiveSpocName()
            val spocPhone = spocData?.getEffectivePhone()?.takeIf { it.isNotBlank() }
                ?: licence.getEffectiveSpocPhone()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FssBlue.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(46.dp).clip(CircleShape).background(FssBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = FssDarkBlue, modifier = Modifier.size(26.dp))
                            Box(
                                modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF4CAF50)).align(Alignment.BottomEnd)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Assigned Service Engineer (SPOC)", fontSize = 10.sp, color = FssBlue, fontWeight = FontWeight.Bold)
                            Text(text = spocName, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = FssDarkBlue)
                            Text(text = "Assigned engineer for your licence support", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { SpocDetails.callSpoc(ctx, spocPhone) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Engineer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { SpocDetails.openWhatsAppSpoc(ctx, "Hello $spocName, I need support for my Licence #${licence.displayKey()}", spocPhone) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp SPOC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun LicenceInfoCard(
    licence: ProductService,
    totalAttached: Int,
    isActive: Boolean,
    onViewServices: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = licence.displayIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    error = painterResource(R.drawable.hand),
                    placeholder = painterResource(R.drawable.hand),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = licence.displayName(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Text(text = "Licence Number", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = licence.displayKey(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FssDarkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Purchase Date", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = licence.purchaseDate ?: licence.validTill ?: "N/A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FssDarkBlue
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onViewServices() }
                ) {
                    Text(text = "Services/Addons", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = totalAttached.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssBlue
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Status", fontSize = 11.sp, color = Color.Gray)
                    Surface(
                        color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = CircleShape
                    ) {
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color(0xFF2E7D32) else Color.Red
                        )
                    }
                }
            }

            licence.expiryDate?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "Expires: $it",
                        fontSize = 12.sp,
                        color = Color.Red.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewServices,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "View All Services & Addons",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ExpiringServicesCard(
    services: List<ProductService>,
    onPayClick: (ProductService) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF57C00),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Services Expiring Soon",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            services.take(5).forEach { service ->
                val daysLeft = daysUntilExpiry(service.expiryDate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = service.displayIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        error = painterResource(R.drawable.hand),
                        placeholder = painterResource(R.drawable.hand),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.displayName(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FssDarkBlue
                        )
                        Text(
                            text = "Expires: ${service.expiryDate}" +
                                    if (daysLeft != null) " ($daysLeft days left)" else "",
                            fontSize = 11.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                // Razorpay Pay button for this service
                Button(
                    onClick = { onPayClick(service) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF528FF0)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay Now with Razorpay",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (service != services.take(5).last()) {
                    HorizontalDivider(
                        color = Color(0xFFFFE082).copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AssistanceHistorySection(
    history: List<AssistanceItem>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assistance History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = onViewAll, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text(text = "View All", color = FssBlue, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = FssBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (history.isEmpty()) {
                Text(
                    text = "No assistance history yet",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                history.take(3).forEach { item ->
                    AssistanceHistoryItem(item = item)
                    if (history.indexOf(item) < minOf(2, history.size - 1)) {
                        HorizontalDivider(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssistanceHistoryItem(item: AssistanceItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FssBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SupportAgent, null, tint = FssBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FssDarkBlue)
            Text(item.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.date, fontSize = 10.sp, color = Color.LightGray)
        }
        Surface(
            color = when (item.status.lowercase()) {
                "resolved" -> Color(0xFFE8F5E9)
                "pending" -> Color(0xFFFFF3E0)
                else -> Color(0xFFFFEBEE)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = item.status,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when (item.status.lowercase()) {
                    "resolved" -> Color(0xFF2E7D32)
                    "pending" -> Color(0xFFE65100)
                    else -> Color.Red
                }
            )
        }
    }
}

@Composable
fun AddonServicesSection(
    services: List<ProductService>,
    onViewAll: () -> Unit,
    onServiceClick: (ProductService) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Addon Services",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )
                TextButton(onClick = onViewAll, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text(text = "View All", color = FssBlue, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = FssBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            services.take(5).forEach { service ->
                AddonServiceItem(
                    service = service,
                    onClick = { onServiceClick(service) }
                )
                if (service != services.take(5).last()) {
                    HorizontalDivider(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddonServiceItem(
    service: ProductService,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = service.displayIcon(),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            error = painterResource(R.drawable.hand),
            placeholder = painterResource(R.drawable.hand),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = service.displayName(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue
            )
            Text(
                text = service.category ?: "Addon Service",
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            service.expiryDate?.let { exp ->
                Text(
                    text = "Expires: $exp",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Active",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            null,
            tint = Color.LightGray,
            modifier = Modifier.size(18.dp)
        )
    }
}

/* ------------------------------------------------------------------
 * SERVICE DETAILS SCREEN (same style as Licence Details)
 * ------------------------------------------------------------------ */
@Composable
fun ServiceDetailsScreen(
    service: ProductService,
    onPayWithRazorpay: (ProductService) -> Unit = {}
) {
    val isActive = service.status.equals("Active", true) ||
            service.status.equals("active", true) ||
            service.isOwned == true

    val isExpiringSoon = isExpiringWithinDays(service.expiryDate, 30)
    val daysLeft = daysUntilExpiry(service.expiryDate)

    // Placeholder lists – replace with real API data later
    val invoices = emptyList<InvoiceItem>()
    val payments = emptyList<PaymentItem>()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main info card
        item {
            ServiceInfoCard(service = service, isActive = isActive)
        }

        // Expiring warning + Razorpay
        if (isExpiringSoon) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (daysLeft != null && daysLeft <= 0)
                                    "Service Expired"
                                else
                                    "Service Expiring Soon",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Expires: ${service.expiryDate}" +
                                    if (daysLeft != null) " ($daysLeft days left)" else "",
                            fontSize = 13.sp,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onPayWithRazorpay(service) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF528FF0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payment, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pay Now with Razorpay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // About Service
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About Service",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = service.category ?: "Service / Addon",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This is your ${service.displayName()} service linked to the licence. " +
                                "You can renew before expiry to avoid interruption.",
                        fontSize = 13.sp,
                        color = FssDarkBlue
                    )
                }
            }
        }

        // Dates card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Important Dates",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Purchase Date", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = service.purchaseDate ?: "N/A",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = FssDarkBlue
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Expiry Date", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = service.expiryDate ?: service.validTill ?: "N/A",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isExpiringSoon) Color(0xFFE65100) else FssDarkBlue
                            )
                        }
                    }
                }
            }
        }

        // Invoices
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Invoices",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (invoices.isEmpty()) {
                        Text("No invoices available", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        invoices.forEach { inv ->
                            // render invoice row
                        }
                    }
                }
            }
        }

        // Payments
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Payments",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (payments.isEmpty()) {
                        Text("No payment records yet", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        payments.forEach { pay ->
                            // render payment row
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun ServiceInfoCard(service: ProductService, isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = service.displayIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    error = painterResource(R.drawable.hand),
                    placeholder = painterResource(R.drawable.hand),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.displayName(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Text(
                        text = service.category ?: "Service",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF2E7D32) else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceDetailsBottomBar(
    service: ProductService,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val showPay = isExpiringWithinDays(service.expiryDate, 30)

    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showPay) {
                Button(
                    onClick = onPayClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF528FF0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Payment, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onCallClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onWhatsAppClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun LicenceDetailsBottomBar(
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCallClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call FSS Team", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onWhatsAppClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp Us", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/* ------------------------------------------------------------------
 * Rest of screens (LicencesList, ServicesForLicence, ServiceCard, etc.)
 * ------------------------------------------------------------------ */
@Composable
fun LicencesList(
    licences: List<ProductService>,
    onLicenceClick: (ProductService) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(licences, searchQuery) {
        licences.filter {
            it.displayName().contains(searchQuery, ignoreCase = true) ||
                    it.displayKey().contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            placeholder = { Text("Search licences...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No Tally products found for your account", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { "${it.id}_${it.displayName()}" }) { licence ->
                    LicenceCard(licence, onLicenceClick)
                }
            }
        }
    }
}

@Composable
fun LicenceCard(licence: ProductService, onClick: (ProductService) -> Unit) {
    val attachedServices = licence.services ?: emptyList()
    val attachedAddons = licence.addons ?: emptyList()
    val totalAttached = attachedServices.size + attachedAddons.size
    val isActive = licence.status.equals("Active", true) || licence.status.equals("active", true)

    // Show badge if any nested service is expiring in 30 days
    val hasExpiring = (attachedServices + attachedAddons).any { isExpiringWithinDays(it.expiryDate, 30) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(licence) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = licence.displayIcon(),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                error = painterResource(R.drawable.hand),
                placeholder = painterResource(R.drawable.hand),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = licence.displayName(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FssDarkBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Key: ${licence.displayKey()}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (totalAttached > 0) {
                    Text(
                        text = "$totalAttached services/addons attached",
                        fontSize = 11.sp,
                        color = FssBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "No services attached",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (hasExpiring) {
                    Text(
                        text = "⚠ Service expiring soon",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = CircleShape
                ) {
                    Text(
                        text = licence.status ?: "Active",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF2E7D32) else Color.Red
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = licence.validTill ?: licence.expiryDate ?: "N/A",
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ServicesForLicenceScreen(
    licence: ProductService,
    allCatalogServices: List<ProductService>,
    attachedServicesForLicence: List<ProductService>,
    onSupportClick: (ProductService) -> Unit,
    onBack: () -> Unit,
    onViewDetails: () -> Unit
) {
    val licenceAttached = attachedServicesForLicence
    val attachedNames = licenceAttached.map { it.displayName().lowercase().trim() }.filter { it.isNotEmpty() }.toSet()
    val attachedKeys = licenceAttached.map { it.displayKey().lowercase().trim() }.filter { it != "n/a" && it.isNotEmpty() }.toSet()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Support", "Service", "Web", "Cloud", "Mobile")

    val filteredCatalog = remember(allCatalogServices, searchQuery, selectedTab) {
        allCatalogServices.filter { service ->
            val matchesSearch = service.displayName().contains(searchQuery, ignoreCase = true)
            val matchesTab = selectedTab == "All" ||
                    (service.category?.contains(selectedTab, ignoreCase = true) == true) ||
                    service.displayName().contains(selectedTab, ignoreCase = true)
            matchesSearch && matchesTab
        }
    }

    val attachedServices = remember(licenceAttached, searchQuery, selectedTab) {
        licenceAttached.filter { service ->
            val matchesSearch = service.displayName().contains(searchQuery, ignoreCase = true)
            val matchesTab = selectedTab == "All" ||
                    (service.category?.contains(selectedTab, ignoreCase = true) == true) ||
                    service.displayName().contains(selectedTab, ignoreCase = true)
            matchesSearch && matchesTab
        }
    }

    val availableServices = remember(filteredCatalog, attachedNames, attachedKeys) {
        filteredCatalog.filter { service ->
            val ownedFlag = service.isOwned
            if (ownedFlag != null) {
                ownedFlag == false
            } else {
                val serviceName = service.displayName().lowercase().trim()
                val serviceKey = service.displayKey().lowercase().trim()
                !attachedNames.contains(serviceName) &&
                        !attachedKeys.contains(serviceKey) &&
                        !attachedKeys.contains(serviceName)
            }
        }
    }

    val attachedCount = attachedServices.size
    val availableCount = availableServices.size

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FssBlue.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FssDarkBlue)
                }
                AsyncImage(
                    model = licence.displayIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    error = painterResource(R.drawable.hand),
                    placeholder = painterResource(R.drawable.hand),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(licence.displayName(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FssDarkBlue)
                    Text("Key: ${licence.displayKey()}", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "Attached: $attachedCount | Available: $availableCount",
                        fontSize = 11.sp,
                        color = FssBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onViewDetails) {
                    Icon(Icons.Default.Info, "Details", tint = FssBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            placeholder = { Text("Search services & addons...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = FssDarkBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(tabs) { tab ->
                FilterTab(label = tab, isSelected = selectedTab == tab) { selectedTab = tab }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (attachedServices.isEmpty() && availableServices.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No matching services found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (attachedServices.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp), tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("✅ Attached to this Licence", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                            }
                            Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                                Text(
                                    attachedCount.toString(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                    items(attachedServices, key = { "attached_${it.id}_${it.displayName()}_${it.expiryDate}" }) { service ->
                        ServiceCard(service = service, isAttached = true, onSupportClick = onSupportClick)
                    }
                }

                if (availableServices.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (attachedServices.isNotEmpty()) 8.dp else 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(18.dp), tint = FssBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("📦 Available to Add", fontSize = 15.sp, fontWeight = FontWeight.Black, color = FssBlue)
                            }
                            Surface(color = FssBlue.copy(alpha = 0.1f), shape = CircleShape) {
                                Text(
                                    "$availableCount",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FssBlue
                                )
                            }
                        }
                    }
                    val grouped = availableServices.groupBy { it.category ?: "Others" }
                    grouped.forEach { (category, categoryServices) ->
                        item {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FssBlue,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                            )
                        }
                        items(categoryServices, key = { "avail_${it.id}_${it.displayName()}" }) { service ->
                            ServiceCard(service = service, isAttached = false, onSupportClick = onSupportClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: ProductService,
    isAttached: Boolean,
    onSupportClick: (ProductService) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAttached) Color(0xFFF5FFF5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAttached) 0.dp else 1.dp),
        border = if (isAttached)
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = service.displayIcon(),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                error = painterResource(R.drawable.hand),
                placeholder = painterResource(R.drawable.hand),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = service.displayName(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FssDarkBlue,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        color = if (isAttached) Color(0xFF4CAF50) else FssBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isAttached) " ACTIVE " else " AVAILABLE ",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAttached) Color.White else FssBlue
                        )
                    }
                }
                if (isAttached && !service.expiryDate.isNullOrBlank()) {
                    Text("Expires: ${service.expiryDate}", fontSize = 10.sp, color = Color.Gray)
                } else if (!isAttached) {
                    Text("Add to this licence", fontSize = 10.sp, color = FssBlue, fontWeight = FontWeight.Medium)
                }
                service.category?.let {
                    Text("Category: $it", fontSize = 9.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onSupportClick(service) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAttached) FssBlue else Color.White,
                    contentColor = if (isAttached) Color.White else FssBlue
                ),
                border = if (!isAttached) androidx.compose.foundation.BorderStroke(1.dp, FssBlue) else null,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (isAttached) "View" else "Add",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FilterTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) FssBlue else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ServicesBottomNavigation(onHome: () -> Unit, onReports: () -> Unit = {}, onProfile: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text(tr("Home")) },
            selected = false,
            onClick = onHome
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
            label = { Text(tr("My Licences")) },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assessment, null) },
            label = { Text(tr("Reports")) },
            selected = false,
            onClick = onReports
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, null) },
            label = { Text(tr("Profile")) },
            selected = false,
            onClick = onProfile
        )
    }
}

data class AssistanceItem(
    val title: String,
    val description: String,
    val date: String,
    val status: String
)

data class InvoiceItem(
    val id: String,
    val date: String,
    val amount: String,
    val status: String
)

data class PaymentItem(
    val id: String,
    val date: String,
    val amount: String,
    val mode: String
)