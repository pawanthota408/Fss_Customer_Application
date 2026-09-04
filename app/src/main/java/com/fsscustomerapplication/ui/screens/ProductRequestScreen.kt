package com.fsscustomerapplication.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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

    fun normalizeProductName(rawName: String?): String {
        val n = rawName.orEmpty().lowercase()
        return when {
            n.contains("silver") -> "Tally Prime Silver"
            n.contains("gold") -> "Tally Prime Gold"
            n.contains("server") -> "Tally Prime Server"
            else -> "Tally Prime Gold"
        }
    }

    val initialProduct = normalizeProductName(item?.displayName())
    var selectedProduct by remember { mutableStateOf(initialProduct) }
    var requestType by remember { mutableStateOf("New License") }
    var selectedLicense by remember { mutableStateOf("") }
    var upgradeTargetProduct by remember { mutableStateOf("Tally Prime Server") }

    LaunchedEffect(userId) {
        viewModel.fetchDashboardData(userId)
    }

    val licences = (uiState as? DashboardState.Success)?.data?.licences ?: emptyList()
    val customer = (uiState as? DashboardState.Success)?.data?.customer

    val allProducts = listOf("Tally Prime Silver", "Tally Prime Gold", "Tally Prime Server")
    
    // Upgrade available products based on the opened product (selectedProduct)
    val availableProducts = remember(selectedProduct, requestType) {
        if (requestType == "Upgrade") {
            when {
                selectedProduct.contains("Silver", ignoreCase = true) ->
                    listOf("Tally Prime Gold", "Tally Prime Server")
                selectedProduct.contains("Gold", ignoreCase = true) ->
                    listOf("Tally Prime Server")
                selectedProduct.contains("Server", ignoreCase = true) ->
                    emptyList()
                else -> listOf("Tally Prime Gold", "Tally Prime Server")
            }
        } else {
            allProducts
        }
    }

    LaunchedEffect(availableProducts) {
        if (availableProducts.isNotEmpty() && !availableProducts.contains(upgradeTargetProduct)) {
            upgradeTargetProduct = availableProducts.first()
        }
    }

    var callMethod by remember { mutableStateOf(value = false) }
    var whatsappMethod by remember { mutableStateOf(value = false) }

    val context = LocalContext.current

    LaunchedEffect(submitState) {
        if (submitState is TicketSubmitState.Success) {
            Toast.makeText(context, (submitState as TicketSubmitState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.resetTicketSubmitState()
            onBack()
        } else if (submitState is TicketSubmitState.Error) {
            Toast.makeText(context, (submitState as TicketSubmitState.Error).message, Toast.LENGTH_SHORT).show()
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
            // Product Header (Always displays selectedProduct)
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
                        Text(if (requestType == "Upgrade" && availableProducts.isEmpty()) "Tally Prime Server" else selectedProduct, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("100% Secure & Official License", fontSize = 10.sp, color = Color(0xFF138808))
                        Text("Selected product for your business", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            // Request Type
            SectionTitle("Request Type")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = requestType == "New License",
                    onClick = { 
                        requestType = "New License"
                        selectedLicense = ""
                    },
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

            // Select License to Upgrade (Choose License) - Placed at the top right below Request Type when Upgrade
            if (requestType == "Upgrade") {
                SectionTitle("Select License to Upgrade")
                val tallyLicences = licences.filter { lic ->
                    val pName = lic.productName.orEmpty().lowercase()
                    pName.contains("tally") || pName.contains("silver") || pName.contains("gold") || pName.contains("server")
                }
                LicenseDropdown(tallyLicences, selectedLicense, showProduct = false) { 
                    selectedLicense = it 
                }
            }

            // Products dropdown (or highest tier message)
            if (requestType == "New License" || (requestType == "Upgrade" && availableProducts.isNotEmpty())) {
                LabelText("Products *")
                SimpleDropdown(
                    options = if (requestType == "Upgrade") availableProducts else allProducts,
                    selected = if (requestType == "Upgrade") upgradeTargetProduct else selectedProduct,
                    onSelect = { 
                        if (requestType == "Upgrade") upgradeTargetProduct = it 
                        else selectedProduct = it 
                    }
                )
            } else if (availableProducts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
                ) {
                    Text(
                        text = "Tally Prime Server is already the highest tier license available. No higher upgrades possible.",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF856404),
                        fontSize = 12.sp
                    )
                }
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
                    val finalProduct = if (requestType == "Upgrade") upgradeTargetProduct else selectedProduct
                    viewModel.submitTicket(TicketRequest(
                        userId = userId,
                        name = fullName,
                        subject = "$requestType: $finalProduct",
                        category = "Product",
                        description = "Requested $finalProduct ($requestType). License: $selectedLicense. Users: $numUsers, Branches: $numBranches.",
                        mobile = mobileNumber,
                        email = emailAddress,
                        company = companyName,
                        licenseNo = if(requestType == "Upgrade") selectedLicense else null
                    ))
                }, 
                modifier = Modifier.fillMaxWidth().height(56.dp), 
                shape = RoundedCornerShape(12.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = FssBlue),
                enabled = submitState !is TicketSubmitState.Loading && !(requestType == "Upgrade" && availableProducts.isEmpty())
            ) {
                if (submitState is TicketSubmitState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Submit Product Request", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
