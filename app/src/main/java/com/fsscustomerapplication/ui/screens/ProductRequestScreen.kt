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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.ui.components.*
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel

import com.fsscustomerapplication.data.remote.model.TicketRequest
import com.fsscustomerapplication.ui.viewmodels.TicketSubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRequestScreen(
    userId: Int,
    item: ProductService?,
    onBack: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState
    val submitState by viewModel.ticketSubmitState
    
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var numUsers by remember { mutableStateOf("") }
    var numBranches by remember { mutableStateOf("10") }
    var serviceRequired by remember { mutableStateOf("Yes") }
    var selectedProduct by remember { mutableStateOf(item?.name ?: "Tally Prime Gold") }
    var requestType by remember { mutableStateOf("New License") }
    var selectedLicense by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.fetchDashboardData(userId)
    }

    val licences = (uiState as? DashboardState.Success)?.data?.licences ?: emptyList()
    val customer = (uiState as? DashboardState.Success)?.data?.customer

    // Logic for available products based on current license (Upgrade Path)
    val allProducts = listOf("Tally Prime Silver", "Tally Prime Gold", "Tally Prime Server", "Tally Cloud")
    val availableProducts = remember(selectedLicense, requestType, licences) {
        if ((requestType == "Upgrade") && selectedLicense.isNotEmpty()) {
            val currentProduct = licences.find { it.number == selectedLicense }?.productName ?: ""
            when {
                currentProduct.contains("Silver", ignoreCase = true) -> 
                    listOf("Tally Prime Gold", "Tally Prime Server", "Tally Cloud")
                currentProduct.contains("Gold", ignoreCase = true) -> 
                    listOf("Tally Prime Server", "Tally Cloud")
                currentProduct.contains("Server", ignoreCase = true) -> 
                    listOf("Tally Cloud")
                else -> allProducts
            }
        } else {
            allProducts
        }
    }

    // Reset selected product if it's no longer available in the upgrade path
    LaunchedEffect(availableProducts) {
        if (!availableProducts.contains(selectedProduct)) {
            selectedProduct = availableProducts.firstOrNull() ?: "Tally Prime Gold"
        }
    }

    var callMethod by remember { mutableStateOf(value = false) }
    var whatsappMethod by remember { mutableStateOf(value = false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(submitState) {
        if (submitState is TicketSubmitState.Success) {
            android.widget.Toast.makeText(context, (submitState as TicketSubmitState.Success).message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetTicketSubmitState()
            onBack()
        } else if (submitState is TicketSubmitState.Error) {
            android.widget.Toast.makeText(context, (submitState as TicketSubmitState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetTicketSubmitState()
        }
    }

    // Pre-fill from database
    LaunchedEffect(customer) {
        customer?.let {
            if (fullName.isEmpty()) fullName = it.name
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
                        Text(if(requestType == "Upgrade") "Upgrade Request" else "New Product Request", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Tell Us Your Requirement", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchDashboardData(userId) }) { Icon(Icons.Default.Refresh, contentDescription = null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            // Product Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = item?.iconLink,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        error = painterResource(R.drawable.hand),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(selectedProduct, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("100% Secure & Official License", fontSize = 10.sp, color = Color(0xFF138808))
                        Text("Selected product for your business", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            SectionTitle("Request Type")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = requestType == "New License",
                    onClick = { requestType = "New License" },
                    label = { Text("New License") }
                )
                if (licences.isNotEmpty()) {
                    FilterChip(
                        selected = requestType == "Upgrade",
                        onClick = { requestType = "Upgrade" },
                        label = { Text("Upgrade License") }
                    )
                }
            }

            if (requestType == "Upgrade") {
                LabelText("Select License to Upgrade *")
                LicenseDropdown(licences, selectedLicense) { selectedLicense = it }
            }

            SectionTitle("Your Details")
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    LabelText("Full Name *")
                    OutlinedTextField(
                        value = fullName, 
                        onValueChange = { fullName = it }, 
                        placeholder = { Text("Enter Name", fontSize = 12.sp) }, 
                        leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if(fullName.isNotEmpty()) IconButton(onClick = { fullName = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp)) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                Column {
                    LabelText("Mobile Number *")
                    OutlinedTextField(
                        value = mobileNumber, 
                        onValueChange = { mobileNumber = it }, 
                        placeholder = { Text("Mobile", fontSize = 12.sp) }, 
                        leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if(mobileNumber.isNotEmpty()) IconButton(onClick = { mobileNumber = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp)) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column {
                    LabelText("Email Address *")
                    OutlinedTextField(
                        value = emailAddress, 
                        onValueChange = { emailAddress = it }, 
                        modifier = Modifier.fillMaxWidth(), 
                        placeholder = { Text("Enter email", fontSize = 12.sp) }, 
                        leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if(emailAddress.isNotEmpty()) IconButton(onClick = { emailAddress = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp)) } },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            SectionTitle("Company Details")
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    LabelText("Company Name *")
                    OutlinedTextField(
                        value = companyName, 
                        onValueChange = { companyName = it }, 
                        placeholder = { Text("Company", fontSize = 12.sp) }, 
                        leadingIcon = { Icon(Icons.Default.Business, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if(companyName.isNotEmpty()) IconButton(onClick = { companyName = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp)) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                Column {
                    LabelText("Number of users *")
                    OutlinedTextField(
                        value = numUsers, 
                        onValueChange = { numUsers = it }, 
                        placeholder = { Text("Users", fontSize = 12.sp) }, 
                        leadingIcon = { Icon(Icons.Default.Group, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { if(numUsers.isNotEmpty()) IconButton(onClick = { numUsers = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp)) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("No.of Branches *")
                    SimpleDropdown(listOf("1", "5", "10", "20+"), numBranches) { numBranches = it }
                }
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("Service required *")
                    SimpleDropdown(listOf("Yes", "No"), serviceRequired) { serviceRequired = it }
                }
            }

            LabelText("Products *")
            SimpleDropdown(availableProducts, selectedProduct) { selectedProduct = it }

            SectionTitle("Preferred Contact Method")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ContactMethodItem("Call", Icons.Default.Phone, callMethod, Modifier.weight(1f)) { callMethod = it }
                ContactMethodItem("Whatsapp", Icons.AutoMirrored.Filled.Message, whatsappMethod, Modifier.weight(1f)) { whatsappMethod = it }
            }

            // Why FSS card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EFFF))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, null, tint = Color(0xFF138808), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Why FSS is Best?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

            Button(
                onClick = { 
                    viewModel.submitTicket(TicketRequest(
                        userId = userId,
                        name = fullName,
                        subject = "$requestType: $selectedProduct",
                        category = "Product",
                        description = "Requested $selectedProduct ($requestType). Users: $numUsers, Branches: $numBranches. Contact: " + (if(callMethod) "Call " else "") + (if(whatsappMethod) "Whatsapp" else ""),
                        mobile = mobileNumber,
                        email = emailAddress,
                        company = companyName,
                        licenseNo = if(requestType == "Upgrade") selectedLicense else null
                    ))
                }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                shape = RoundedCornerShape(12.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                enabled = submitState !is TicketSubmitState.Loading
            ) {
                if (submitState is TicketSubmitState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Submit Product Request", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
