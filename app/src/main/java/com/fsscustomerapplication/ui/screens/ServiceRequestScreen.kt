package com.fsscustomerapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.data.remote.model.TicketRequest
import com.fsscustomerapplication.ui.components.*
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.TicketSubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceRequestScreen(
    userId: Int,
    item: ProductService?,
    tdlName: String? = null,
    onBack: () -> Unit
) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState
    val submitState by viewModel.ticketSubmitState

    var selectedLicense by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf(tdlName ?: item?.name ?: "AMC") }
    var callMethod by remember { mutableStateOf(true) }      // default selected
    var whatsappMethod by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Handle success / error
    LaunchedEffect(submitState) {
        when (submitState) {
            is TicketSubmitState.Success -> {
                android.widget.Toast.makeText(
                    context,
                    (submitState as TicketSubmitState.Success).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetTicketSubmitState()
                onBack()
            }
            is TicketSubmitState.Error -> {
                android.widget.Toast.makeText(
                    context,
                    (submitState as TicketSubmitState.Error).message,
                    android.widget.Toast.LENGTH_LONG
                ).show()
                viewModel.resetTicketSubmitState()
            }
            else -> {}
        }
    }

    // Load dashboard data
    LaunchedEffect(userId) {
        viewModel.fetchDashboardData(userId)
    }

    val licences = (uiState as? DashboardState.Success)?.data?.licences ?: emptyList()
    val customer = (uiState as? DashboardState.Success)?.data?.customer

    // Pre-fill form from customer data
    LaunchedEffect(customer) {
        customer?.let {
            if (fullName.isEmpty()) fullName = it.name ?: ""
            if (mobileNumber.isEmpty()) mobileNumber = it.phone ?: ""
            if (emailAddress.isEmpty()) emailAddress = it.email ?: ""
            if (companyName.isEmpty()) companyName = it.company ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Services Request",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tell Us Your Requirement",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchDashboardData(userId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFF))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = item?.iconLink,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        error = painterResource(R.drawable.hand),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = selectedService,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FssDarkBlue
                        )
                        Text(
                            text = "Premium Support & Assistance",
                            fontSize = 11.sp,
                            color = Color(0xFF138808),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Requesting support for your business",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ---------- Your Details ----------
            SectionTitle("Your Details")

            LabelText("Current License Number *")
            LicenseDropdown(
                licences = licences,
                selected = selectedLicense,
                onSelect = { selectedLicense = it }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Full Name
                Column {
                    LabelText("Full Name *")
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Name", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (fullName.isNotEmpty()) {
                                IconButton(onClick = { fullName = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Mobile
                Column {
                    LabelText("Mobile Number *")
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        placeholder = { Text("Mobile", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (mobileNumber.isNotEmpty()) {
                                IconButton(onClick = { mobileNumber = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Email
                Column {
                    LabelText("Email Address *")
                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = { Text("Email", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (emailAddress.isNotEmpty()) {
                                IconButton(onClick = { emailAddress = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ---------- Company Details ----------
            SectionTitle("Company Details", color = FssBlue)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    LabelText("Company Name *")
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        placeholder = { Text("Company", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (companyName.isNotEmpty()) {
                                IconButton(onClick = { companyName = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column {
                    LabelText("Service *")
                    SimpleDropdown(
                        options = listOf("AMC", "TDL", "Cloud", "WhatsApp"),
                        selected = selectedService,
                        onSelect = { selectedService = it }
                    )
                }
            }

            // ---------- Preferred Contact Method ----------
            SectionTitle("Preferred Contact Method", color = FssBlue)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContactMethodItem(
                    label = "Call",
                    icon = Icons.Default.Phone,
                    isChecked = callMethod,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { callMethod = it }
                )
                ContactMethodItem(
                    label = "Whatsapp",
                    icon = Icons.AutoMirrored.Filled.Message,
                    isChecked = whatsappMethod,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { whatsappMethod = it }
                )
            }

            // Why FSS is Best card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EFFF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF138808),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Why FSS is Best?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                BenefitItem("Official Tally Partner")
                                BenefitItem("Fast & Hassle-free Renewal")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BenefitItem("Best Prices & Offers")
                                BenefitItem("Dedicated Support")
                            }
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    // Basic validation
                    if (fullName.isBlank() || mobileNumber.isBlank() ||
                        emailAddress.isBlank() || companyName.isBlank()
                    ) {
                        android.widget.Toast.makeText(
                            context,
                            "Please fill all required fields",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val preferredContact = buildString {
                        if (callMethod) append("Call ")
                        if (whatsappMethod) append("Whatsapp")
                    }.ifBlank { "None" }

                    viewModel.submitTicket(
                        TicketRequest(
                            userId = userId,
                            name = fullName.trim(),
                            subject = "Service Request: $selectedService",
                            category = "Service",
                            description = "Requesting $selectedService. Preferred contact: $preferredContact",
                            mobile = mobileNumber.trim(),
                            email = emailAddress.trim(),
                            company = companyName.trim(),
                            licenseNo = selectedLicense.ifBlank { "N/A" }
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                enabled = submitState !is TicketSubmitState.Loading
            ) {
                if (submitState is TicketSubmitState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Submit Service Request",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}