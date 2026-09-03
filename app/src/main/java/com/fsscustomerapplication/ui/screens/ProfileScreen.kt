package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.local.SessionManager
import com.fsscustomerapplication.data.remote.model.CustomerDetails
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.data.remote.model.ProfileUpdateRequest
import com.fsscustomerapplication.ui.components.LanguageSelectionDialog
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.theme.FssGradientEnd
import com.fsscustomerapplication.ui.theme.FssGradientStart
import com.fsscustomerapplication.ui.theme.FssLightBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.ProfileUpdateState
import com.fsscustomerapplication.utils.LanguageManager

enum class ProfileView {
    Main, PersonalInfo, CompanyInfo, ManageAdmins, ChangePassword, AboutUs
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Int,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToTickets: () -> Unit,
    onSpocChatClick: () -> Unit = {},
    onLogout: () -> Unit
) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState
    val updateState by viewModel.profileUpdateState
    var currentView by remember { mutableStateOf(ProfileView.Main) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    var currentLangCode by remember { mutableStateOf(sessionManager.getLanguage()) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        if (updateState is ProfileUpdateState.Success) {
            android.widget.Toast.makeText(context, (updateState as ProfileUpdateState.Success).message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetProfileUpdateState()
            currentView = ProfileView.Main
        } else if (updateState is ProfileUpdateState.Error) {
            android.widget.Toast.makeText(context, (updateState as ProfileUpdateState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetProfileUpdateState()
        }
    }

    LaunchedEffect(userId) {
        viewModel.fetchDashboardData(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentView) {
                            ProfileView.Main -> "My Profile"
                            ProfileView.PersonalInfo -> "Personal Info"
                            ProfileView.CompanyInfo -> "Company Info"
                            ProfileView.ManageAdmins -> "Manage Admins"
                            ProfileView.ChangePassword -> "Security"
                            ProfileView.AboutUs -> "About FSS"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentView == ProfileView.Main) onBack() else currentView = ProfileView.Main
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentView == ProfileView.Main) {
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(Icons.Default.Translate, contentDescription = "Language", tint = FssBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (currentView == ProfileView.Main) {
                ProfileBottomNavigation(
                    onHome = onNavigateToHome,
                    onServices = onNavigateToServices,
                    onReports = onNavigateToReports
                )
            }
        },
        containerColor = Color(0xFFF8FAFF)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FssBlue
                    )
                }
                is DashboardState.Success -> {
                    val customer = state.data.customer
                    // Filter to ONLY count main Tally product licenses (exclude services/addons)
                    val rawMain = (state.data.mainLicenses ?: state.data.tallyProducts ?: state.data.allLicences ?: emptyList())
                    val mainLicenses = rawMain
                        .filter { isMainTallyProduct(it) }
                        .distinctBy { (it.number ?: it.licenseKey ?: it.displayKey()).lowercase().trim() }
                    val mainLicencesCount = mainLicenses.size
                    when (currentView) {
                        ProfileView.Main -> ProfileMainContent(
                            customer = customer,
                            licencesCount = mainLicencesCount,
                            currentLangCode = currentLangCode,
                            onLogout = onLogout,
                            onTicketsClick = onNavigateToTickets,
                            onSpocChatClick = onSpocChatClick,
                            onOpenLanguageDialog = { showLanguageDialog = true },
                            onViewChange = { currentView = it }
                        )
                        ProfileView.PersonalInfo -> PersonalInfoEdit(
                            userId = userId,
                            customer = customer,
                            isLoading = updateState is ProfileUpdateState.Loading,
                            onSave = { viewModel.updateProfile(it) }
                        )
                        ProfileView.CompanyInfo -> CompanyInfoEdit(
                            userId = userId,
                            customer = customer,
                            isLoading = updateState is ProfileUpdateState.Loading,
                            onSave = { viewModel.updateProfile(it) }
                        )
                        ProfileView.ManageAdmins -> ManageAdminsView { currentView = ProfileView.Main }
                        ProfileView.ChangePassword -> ChangePasswordView(
                            userId = userId,
                            isLoading = updateState is ProfileUpdateState.Loading,
                            onSave = { viewModel.updateProfile(it) }
                        )
                        ProfileView.AboutUs -> AboutUsView()
                    }
                }
                is DashboardState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }

            if (showLanguageDialog) {
                LanguageSelectionDialog(
                    currentLanguageCode = currentLangCode,
                    onDismiss = { showLanguageDialog = false },
                    onLanguageSelected = { selected ->
                        LanguageManager.applyLanguage(context, selected.code)
                        currentLangCode = selected.code
                        showLanguageDialog = false
                        LanguageManager.findActivity(context)?.recreate()
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileMainContent(
    customer: CustomerDetails?,
    licencesCount: Int,
    currentLangCode: String,
    onLogout: () -> Unit,
    onTicketsClick: () -> Unit,
    onSpocChatClick: () -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onViewChange: (ProfileView) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FssDarkBlue, FssBlue)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, FssLightBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = FssDarkBlue
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = customer?.name ?: "User",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = customer?.email ?: "",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = licencesCount.toString(),
                        label = "Main Licences",
                        color = FssBlue
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFE0E0E0))
                    )
                    StatItem(
                        value = "${customer?.activeServices ?: 0}",
                        label = "Services",
                        color = Color(0xFF4CAF50)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color(0xFFE0E0E0))
                    )
                    StatItem(
                        value = "${customer?.pendingRequests ?: 0}",
                        label = "Tickets",
                        color = Color(0xFFFF9800)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Contact Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ContactInfoItem(
                        icon = Icons.Default.Business,
                        label = "Company",
                        value = customer?.company ?: "N/A"
                    )
                    ContactInfoItem(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = customer?.phone ?: "N/A"
                    )
                    ContactInfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = customer?.email ?: "N/A"
                    )
                    if (!customer?.address.isNullOrBlank()) {
                        ContactInfoItem(
                            icon = Icons.Default.LocationOn,
                            label = "Address",
                            value = customer?.address ?: "N/A"
                        )
                    }
                }
            }

            Text(
                text = "Account Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ModernMenuItem(
                        icon = Icons.Default.Person,
                        title = "Personal Information",
                        subtitle = "Update your name, email, phone",
                        onClick = { onViewChange(ProfileView.PersonalInfo) }
                    )
                    MenuDivider()
                    ModernMenuItem(
                        icon = Icons.Default.Business,
                        title = "Company Information",
                        subtitle = "Update business details",
                        onClick = { onViewChange(ProfileView.CompanyInfo) }
                    )
                    MenuDivider()
                    ModernMenuItem(
                        icon = Icons.Default.SupportAgent,
                        title = "Chat with Support",
                        subtitle = "Connect with FSS AI Agent",
                        onClick = onSpocChatClick
                    )
                    MenuDivider()
                    ModernMenuItem(
                        icon = Icons.Default.ConfirmationNumber,
                        title = "My Tickets",
                        subtitle = "Track your service requests",
                        onClick = onTicketsClick
                    )
                }
            }

            Text(
                text = "Preferences",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ModernMenuItem(
                        icon = Icons.Default.Translate,
                        title = "Language",
                        subtitle = "${LanguageManager.getLanguageByCode(currentLangCode).flagEmoji} ${LanguageManager.getLanguageByCode(currentLangCode).nativeName}",
                        onClick = onOpenLanguageDialog
                    )
                    MenuDivider()
                    ModernMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        subtitle = "Update your security",
                        onClick = { onViewChange(ProfileView.ChangePassword) }
                    )
                    MenuDivider()
                    ModernMenuItem(
                        icon = Icons.Default.People,
                        title = "Manage Team",
                        subtitle = "Add or remove admins",
                        onClick = { onViewChange(ProfileView.ManageAdmins) }
                    )
                    MenuDivider()
                    ModernMenuItem(
                        icon = Icons.Default.Info,
                        title = "About FSS",
                        subtitle = "Know more about us",
                        onClick = { onViewChange(ProfileView.AboutUs) }
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color(0xFFDE3151),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDE3151)
                        )
                        Text(
                            text = "Sign out of your account",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

private fun isMainTallyProduct(ps: ProductService): Boolean {
    if ((ps.isMain ?: 0) == 1) return true
    if (ps.type.equals("Product", true) && !isServiceOrAddonName(ps.displayName())) return true
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

@Composable
fun ContactInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = FssBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FssDarkBlue
            )
        }
    }
}

@Composable
fun ModernMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FssLightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = FssBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = FssDarkBlue
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Color(0xFFEEEEEE)
    )
}

@Composable
fun PersonalInfoEdit(
    userId: Int,
    customer: CustomerDetails?,
    isLoading: Boolean,
    onSave: (ProfileUpdateRequest) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit your personal details",
            fontSize = 14.sp,
            color = Color.Gray
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onSave(
                    ProfileUpdateRequest(
                        userId = userId,
                        updateType = "personal",
                        name = name,
                        email = email,
                        phone = phone
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && name.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FssBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CompanyInfoEdit(
    userId: Int,
    customer: CustomerDetails?,
    isLoading: Boolean,
    onSave: (ProfileUpdateRequest) -> Unit
) {
    var company by remember { mutableStateOf(customer?.company ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Update your business information",
            fontSize = 14.sp,
            color = Color.Gray
        )

        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Business, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Business Address") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onSave(
                    ProfileUpdateRequest(
                        userId = userId,
                        updateType = "company",
                        companyName = company,
                        address = address
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && company.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FssBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Update Company Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ManageAdminsView(onBack: () -> Unit) {
    var showAddForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        if (!showAddForm) {
            Text(
                text = "Team Members",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue
            )
            Text(
                text = "Manage access to your account",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FssLightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            null,
                            tint = FssBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "You (Primary Owner)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FssDarkBlue
                        )
                        Text(
                            "Full Access",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            "Admin",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showAddForm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FssBlue)
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Member", fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = "Add New Member",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Member Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FssBlue)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Member Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FssBlue)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Access Level",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = FssDarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = true,
                    onClick = {},
                    colors = RadioButtonDefaults.colors(selectedColor = FssBlue)
                )
                Text("Limited (Viewing Only)", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showAddForm = false },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FssDarkBlue)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { showAddForm = false },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FssBlue)
                ) {
                    Text("Invite", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChangePasswordView(
    userId: Int,
    isLoading: Boolean,
    onSave: (ProfileUpdateRequest) -> Unit
) {
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Update your password",
            fontSize = 14.sp,
            color = Color.Gray
        )

        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = FssBlue) },
            shape = RoundedCornerShape(12.dp),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FssBlue,
                focusedLabelColor = FssBlue
            )
        )

        if (newPass != confirmPass && confirmPass.isNotEmpty()) {
            Text(
                text = "Passwords do not match",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (newPass == confirmPass) {
                    onSave(
                        ProfileUpdateRequest(
                            userId = userId,
                            updateType = "password",
                            newPassword = newPass
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && newPass.isNotEmpty() && newPass == confirmPass,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FssBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun AboutUsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FssDarkBlue, FssBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.hand),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "FRIENDS SOFTWARE SOLUTIONS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "Your Trusted Tally Partner",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Our Story",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue
            )
            Text(
                text = "Since our inception, FSS has been dedicated to providing top-tier Tally solutions and custom software development. We help businesses streamline their accounting and operations.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FssLightBlue)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        null,
                        tint = FssBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "10+ Years of Excellence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FssDarkBlue
                        )
                        Text(
                            "Trusted by thousands of businesses",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = "What We Offer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue
            )

            val offerings = listOf(
                "Custom TDL Development",
                "Tally Cloud Services",
                "Corporate Training",
                "AMC & Technical Support"
            )
            offerings.forEach { title ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, fontSize = 14.sp, color = FssDarkBlue)
                }
            }
        }
    }
}

@Composable
fun ProfileBottomNavigation(
    onHome: () -> Unit,
    onServices: () -> Unit,
    onReports: () -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onHome,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FssBlue,
                selectedTextColor = FssBlue,
                indicatorColor = FssLightBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) },
            label = { Text("Licences", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onServices,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FssBlue,
                selectedTextColor = FssBlue,
                indicatorColor = FssLightBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assessment, null) },
            label = { Text("Reports", fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onReports,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FssBlue,
                selectedTextColor = FssBlue,
                indicatorColor = FssLightBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, null) },
            label = { Text("Profile", fontWeight = FontWeight.Bold) },
            selected = true,
            onClick = {},
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = FssBlue,
                indicatorColor = FssBlue,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            )
        )
    }
}
