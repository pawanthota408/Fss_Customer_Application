package com.fsscustomerapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsscustomerapplication.R
import com.fsscustomerapplication.data.local.SessionManager
import com.fsscustomerapplication.data.remote.model.ProductService
import com.fsscustomerapplication.data.remote.model.TicketRequest
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.SpocState
import com.fsscustomerapplication.utils.tr
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val senderName: String = "",
    val messageStatus: MessageStatus = MessageStatus.SENT,
    val showQuickReplies: Boolean = false,
    val quickReplies: List<String> = emptyList(),
    val showLicenceList: Boolean = false
)

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val locale: Locale
)

val supportedLanguages = listOf(
    Language("en", "English", "English", Locale.ENGLISH),
    Language("hi", "Hindi", "हिन्दी", Locale("hi")),
    Language("ta", "Tamil", "தமிழ்", Locale("ta")),
    Language("te", "Telugu", "తెలుగు", Locale("te")),
    Language("kn", "Kannada", "ಕನ್ನಡ", Locale("kn")),
    Language("ml", "Malayalam", "മലയാളം", Locale("ml")),
    Language("mr", "Marathi", "मराठी", Locale("mr")),
    Language("bn", "Bengali", "বাংলা", Locale("bn")),
    Language("gu", "Gujarati", "ગુજરાતી", Locale("gu")),
    Language("pa", "Punjabi", "ਪੰਜਾਬੀ", Locale("pa"))
)

data class AiReply(
    val text: String,
    val options: List<String>
)

object SpocDetails {
    const val NAME = "Friends Software Solutions Team"
    const val ROLE = "Assigned FSS Service Engineer & SPOC"
    const val PHONE = "+919848012345"
    const val EMAIL = "support@friendssoftware.in"
    const val COMPANY = "Friends Software Solutions"

    fun callSpoc(
        context: android.content.Context,
        phoneNum: String = PHONE,
        licenseNo: String? = null
    ) {
        val target = phoneNum.ifBlank { PHONE }
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$target"))
            context.startActivity(intent)
        } catch (_: Exception) {}

        // Automatically log call into call_history in IST timezone
        try {
            val sessionManager = SessionManager(context)
            val userId = sessionManager.getUserId()
            if (userId > 0) {
                val istTimeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = istTimeZone }
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US).apply { timeZone = istTimeZone }
                val now = Date()

                val callDate = dateFormat.format(now)
                val callTime = timeFormat.format(now)

                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        com.fsscustomerapplication.data.remote.RetrofitClient.apiService.logCall(
                            customerId = userId,
                            userId = userId,
                            name = sessionManager.getUserName().ifBlank { "Customer" },
                            mobile = target,
                            email = sessionManager.getUserEmail().ifBlank { null },
                            licenseNumber = licenseNo,
                            callDate = callDate,
                            callTime = callTime
                        )
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    fun openWhatsAppSpoc(context: android.content.Context, message: String = "", phoneNum: String = PHONE) {
        try {
            val target = phoneNum.ifBlank { PHONE }.replace("+", "").replace(" ", "").replace("-", "")
            val url = "https://api.whatsapp.com/send?phone=$target&text=${Uri.encode(message.ifEmpty { "Hello, I need support for my FSS Tally Licence." })}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpocChatScreen(
    userId: Int,
    licenseNo: String = "",
    spocNameParam: String? = null,
    spocPhoneParam: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val sessionManager = remember { SessionManager(context) }
    val spocState by viewModel.spocState
    val dashboardState by viewModel.uiState

    val liveCustomer = (dashboardState as? DashboardState.Success)?.data?.customer

    val customerName = liveCustomer?.name?.takeIf { it.isNotBlank() }
        ?: sessionManager.getUserName().takeIf { it.isNotBlank() }
        ?: "Customer"

    val customerPhone = liveCustomer?.phone?.takeIf { it.isNotBlank() }
        ?: sessionManager.getUserPhone().takeIf { it.isNotBlank() }
        ?: ""

    val customerEmail = liveCustomer?.email?.takeIf { it.isNotBlank() }
        ?: sessionManager.getUserEmail().takeIf { it.isNotBlank() }
        ?: ""

    val customerCompany = liveCustomer?.company?.takeIf { it.isNotBlank() }
        ?: sessionManager.getUserCompany().takeIf { it.isNotBlank() }
        ?: "N/A"

    // Save live customer details into SessionManager as soon as loaded
    LaunchedEffect(liveCustomer) {
        if (liveCustomer != null) {
            sessionManager.saveUserData(
                name = customerName,
                email = customerEmail,
                phone = customerPhone,
                company = customerCompany
            )
        }
    }

    LaunchedEffect(userId, licenseNo) {
        viewModel.fetchSpoc(
            licenseNumber = licenseNo.ifBlank { null },
            customerId = if (licenseNo.isBlank()) userId else null
        )
        viewModel.fetchDashboardData(userId)
    }

    val spocData = (spocState as? SpocState.Success)?.data
    val spocName = spocNameParam?.takeIf { it.isNotBlank() && it != "Not Assigned" }
        ?: spocData?.getEffectiveName()?.takeIf { it != "Not Assigned" }
        ?: SpocDetails.NAME

    val spocPhone = spocPhoneParam?.takeIf { it.isNotBlank() }
        ?: spocData?.getEffectivePhone()?.takeIf { it.isNotBlank() }
        ?: SpocDetails.PHONE

    val customerLicences = remember(dashboardState) {
        if (dashboardState is DashboardState.Success) {
            val data = (dashboardState as DashboardState.Success).data
            val rawList = data.allLicences?.ifEmpty { data.products } ?: emptyList()
            rawList.filter { it.displayKey().isNotBlank() && it.displayKey() != "N/A" }
                .distinctBy { it.displayKey().lowercase().trim() }
        } else {
            emptyList()
        }
    }

    var messageText by remember { mutableStateOf("") }
    var activeLicenceNo by remember { mutableStateOf(licenseNo) }
    var selectedLanguage by remember { mutableStateOf(supportedLanguages[0]) }
    var isTyping by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val messages = remember { mutableStateListOf<ChatMessage>() }

    fun getGeneralKnowledgeResponse(query: String, language: Language): String {
        val q = query.lowercase()
        return when {
            q.contains("tally") -> {
                "TallyPrime is India's premier accounting and business management software.\n\nKey Capabilities:\n• Financial Accounting & Bookkeeping\n• Inventory Management & Stock Tracking\n• GST Billing, GSTR-1 & GSTR-3B Return Filing\n• Automated e-Invoicing & e-Way Bills\n• Banking & Multi-currency Support\n\nFSS provides Authorized Tally Sales, Custom TDLs, and Tally Cloud services."
            }
            q.contains("bizanylst") || q.contains("biz analyst") || q.contains("biz") -> {
                "Biz Analyst is a mobile analytics app for Tally users that gives 24/7 access to Tally reports on your phone.\n\nFeatures:\n• Real-time Sales & Profit Dashboards\n• Outstanding Payment Tracking & Reminders\n• Customer Ledger Reports & Stock Status\n• Instant PDF Invoice Sharing via WhatsApp"
            }
            q.contains("gst") -> {
                "TallyPrime offers complete automated GST compliance for Indian businesses.\n\nCapabilities:\n• Automatic CGST, SGST & IGST Calculation\n• One-click e-Invoicing & e-Way Bill Generation\n• GSTR-1, GSTR-3B, and GSTR-2A/2B Reconciliation"
            }
            q.contains("cloud") -> {
                "Tally Cloud by FSS allows you to access your Tally software from anywhere on any device (Windows, Mac, Android).\n\nBenefits:\n• 24/7 Remote Access with high speed\n• Automated Cloud Backups & High Data Security\n• Multi-user concurrent access without server hardware"
            }
            else -> {
                "Hello $customerName! I am your FSS AI Support Agent. How can I assist you today?\n\nI can help you with:\n• Tally Software & Licence Info\n• Biz Analyst & Tally Cloud\n• GST & Technical Support\n• Renewal & Account Status"
            }
        }
    }

    val greetingText = remember(selectedLanguage, customerName, spocName) {
        if (activeLicenceNo.isNotBlank()) {
            "Hello $customerName! I am $spocName, your assigned FSS Service Specialist for License #$activeLicenceNo. How can I assist you today?"
        } else {
            "Hello $customerName! I am $spocName, your assigned FSS Service Specialist. How can I assist you with your software services today?"
        }
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    id = "greeting",
                    text = greetingText,
                    timestamp = timeFormatter.format(Date()),
                    isFromMe = false,
                    senderName = spocName,
                    messageStatus = MessageStatus.DELIVERED,
                    showQuickReplies = true,
                    quickReplies = listOf(
                        "🚀 Inquire / Buy New Software",
                        "🔑 Existing Licence Support",
                        "📋 Account & Licence Overview",
                        "❓ General Info (Tally, GST, Cloud, Biz Analyst)"
                    )
                )
            )
        }
    }

    LaunchedEffect(spocName) {
        if (spocName.isNotBlank() && spocName != "Friends Software Solutions Team" && messages.isNotEmpty() && !messages[0].isFromMe) {
            messages[0] = messages[0].copy(
                text = greetingText,
                senderName = spocName
            )
        }
    }

    fun generateAiResponse(userQuery: String): AiReply {
        val q = userQuery.lowercase()
        val dashes = (dashboardState as? DashboardState.Success)?.data

        val isGeneralInfo = q.contains("what is") || q.contains("tell me") || q.contains("explain") ||
                q.contains("tally") || q.contains("biz") || q.contains("gst") || q.contains("cloud")

        val isLicenceQuery = q.contains("account") || q.contains("licence") || q.contains("license") ||
                q.contains("how many") || q.contains("my software") || q.contains("overview")

        val isRenewalQuery = q.contains("renew") || q.contains("renewal") || q.contains("expiry") || q.contains("expire")

        return when {
            isGeneralInfo && !q.contains("technical support") && !q.contains("ticket") -> {
                val answer = getGeneralKnowledgeResponse(q, selectedLanguage)
                AiReply(
                    text = "$answer\n\nWould you like me to connect you with our FSS Representative ($spocName) for further details or demonstration?",
                    options = listOf(
                        "✅ Yes, Connect Me / Create Support Request",
                        "❌ No, Just Browsing / Thanks"
                    )
                )
            }

            isLicenceQuery -> {
                if (dashes != null) {
                    val licCount = customerLicences.size
                    val company = dashes.customer?.company ?: customerCompany
                    if (licCount > 0) {
                        val details = customerLicences.joinToString("\n• ") {
                            "${it.displayName()} (#${it.displayKey()}) - Expiry: ${it.expiryDate ?: "Active"}"
                        }
                        AiReply(
                            text = "Hello $customerName, under $company you have $licCount registered licence(s):\n• $details\n\nWould you like me to connect you with $spocName regarding your licences?",
                            options = listOf(
                                "🔑 Existing Licence Support",
                                "🔄 Renew Expiring Licence / AMC",
                                "✅ Yes, Connect Me / Create Support Request",
                                "❌ No, Just Browsing / Thanks"
                            )
                        )
                    } else {
                        AiReply(
                            text = "You currently have active registered licences with FSS. Please select your licence below to get support.",
                            options = listOf("🔑 Existing Licence Support", "📞 Call SPOC Engineer")
                        )
                    }
                } else {
                    AiReply(
                        text = "You currently have active registered licences with FSS. Please select your licence below to get support.",
                        options = listOf("🔑 Existing Licence Support", "📞 Call SPOC Engineer")
                    )
                }
            }

            isRenewalQuery -> {
                if (dashes != null) {
                    val expServices = dashes.expiringServices ?: emptyList()
                    if (expServices.isNotEmpty()) {
                        val expList = expServices.joinToString("\n• ") { "${it.displayName()} (#${it.displayKey()}) - Expiring: ${it.expiryDate ?: "Soon"}" }
                        AiReply(
                            text = "Here are your expiring services:\n• $expList\n\nWould you like me to connect you with $spocName to process renewal or issue a Proforma Invoice?",
                            options = listOf(
                                "✅ Yes, Connect Me / Create Support Request",
                                "💳 Process Renewal Now",
                                "❌ No, Just Browsing / Thanks"
                            )
                        )
                    } else {
                        AiReply(
                            text = "Your software licences are active. For renewal assistance, please select your licence below.",
                            options = listOf("🔑 Existing Licence Support", "📞 Call SPOC Engineer")
                        )
                    }
                } else {
                    AiReply(
                        text = "Your software licences are active. For renewal assistance, please select your licence below.",
                        options = listOf("🔑 Existing Licence Support", "📞 Call SPOC Engineer")
                    )
                }
            }

            else -> {
                val licText = if (activeLicenceNo.isNotBlank()) " for Licence #$activeLicenceNo" else ""
                AiReply(
                    text = "I understand your query$licText. Would you like me to connect you with our FSS Representative / SPOC Engineer $spocName (+${spocPhone.replace("+", "")}) by creating a support request/lead for you?",
                    options = listOf(
                        "✅ Yes, Connect Me / Create Support Request",
                        "❌ No, Just Browsing / Thanks"
                    )
                )
            }
        }
    }

    fun sendMessage(textToSend: String) {
        if (textToSend.isBlank()) return
        val now = timeFormatter.format(Date())
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = textToSend.trim(),
            timestamp = now,
            isFromMe = true,
            senderName = customerName,
            messageStatus = MessageStatus.SENT
        )
        messages.add(userMsg)
        messageText = ""

        scope.launch {
            val userMsgIndex = messages.indexOfFirst { it.id == userMsg.id }
            if (userMsgIndex >= 0) {
                messages[userMsgIndex] = messages[userMsgIndex].copy(messageStatus = MessageStatus.DELIVERED)
            }

            isTyping = true
            delay(800)
            isTyping = false

            val aiReplyObj = generateAiResponse(textToSend)
            val spocReply = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                text = aiReplyObj.text,
                timestamp = timeFormatter.format(Date()),
                isFromMe = false,
                senderName = spocName,
                messageStatus = MessageStatus.DELIVERED,
                showQuickReplies = true,
                quickReplies = aiReplyObj.options
            )
            messages.add(spocReply)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun handleQuickReply(reply: String) {
        when {
            reply.contains("Yes, Connect Me") || reply.contains("Create Support Request") -> {
                val now = timeFormatter.format(Date())
                messages.add(
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        text = "✅ Yes, please connect me with a representative",
                        timestamp = now,
                        isFromMe = true,
                        senderName = customerName
                    )
                )

                scope.launch {
                    // Create Lead/Ticket ONLY after explicit customer consent!
                    try {
                        val lastQuery = messages.findLast { it.isFromMe }?.text ?: "Support Request"
                        viewModel.submitTicket(
                            TicketRequest(
                                userId = userId,
                                name = customerName,
                                subject = "Lead: ${lastQuery.take(40)}",
                                category = if (activeLicenceNo.isNotBlank()) "Service" else "New Licence",
                                description = "Customer requested lead creation for: $lastQuery",
                                mobile = customerPhone.ifBlank { spocPhone },
                                email = customerEmail.ifBlank { SpocDetails.EMAIL },
                                company = customerCompany,
                                licenseNo = activeLicenceNo.ifBlank { null }
                            )
                        )
                    } catch (_: Exception) {}

                    isTyping = true
                    delay(800)
                    isTyping = false

                    messages.add(
                        ChatMessage(
                            id = (System.currentTimeMillis() + 1).toString(),
                            text = "Thank you $customerName! I have successfully created a support request for you. Our assigned engineer $spocName (+${spocPhone}) has been notified and will contact you shortly.",
                            timestamp = timeFormatter.format(Date()),
                            isFromMe = false,
                            senderName = spocName,
                            messageStatus = MessageStatus.DELIVERED,
                            showQuickReplies = true,
                            quickReplies = listOf("📞 Call $spocName Directly", "📋 Account Overview")
                        )
                    )
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            reply.contains("No, Just Browsing") || reply.contains("No, Thanks") -> {
                val now = timeFormatter.format(Date())
                messages.add(
                    ChatMessage(
                        id = System.currentTimeMillis().toString(),
                        text = "❌ No, thank you",
                        timestamp = now,
                        isFromMe = true,
                        senderName = customerName
                    )
                )

                scope.launch {
                    isTyping = true
                    delay(500)
                    isTyping = false

                    messages.add(
                        ChatMessage(
                            id = (System.currentTimeMillis() + 1).toString(),
                            text = "No problem $customerName! Feel free to ask if you have any other questions.",
                            timestamp = timeFormatter.format(Date()),
                            isFromMe = false,
                            senderName = spocName,
                            showQuickReplies = true,
                            quickReplies = listOf("📋 Account Overview", "❓ General Info (Tally, GST, Cloud)")
                        )
                    )
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            reply.contains("Existing Licence") -> {
                val now = timeFormatter.format(Date())
                messages.add(ChatMessage(id = System.currentTimeMillis().toString(), text = reply, timestamp = now, isFromMe = true, senderName = customerName))
                messages.add(
                    ChatMessage(
                        id = (System.currentTimeMillis() + 1).toString(),
                        text = "Here are your registered active licences. Please tap your Licence Number:",
                        timestamp = now,
                        isFromMe = false,
                        senderName = spocName,
                        showLicenceList = true
                    )
                )
                scope.launch { listState.animateScrollToItem(messages.size - 1) }
            }

            reply.contains("Inquire / Buy") -> {
                activeLicenceNo = ""
                val now = timeFormatter.format(Date())
                messages.add(ChatMessage(id = System.currentTimeMillis().toString(), text = reply, timestamp = now, isFromMe = true, senderName = customerName))
                messages.add(
                    ChatMessage(
                        id = (System.currentTimeMillis() + 1).toString(),
                        text = "Great! Please type what software, Tally module, or service you are looking for.",
                        timestamp = now,
                        isFromMe = false,
                        senderName = spocName
                    )
                )
                scope.launch { listState.animateScrollToItem(messages.size - 1) }
            }

            reply.contains("Call") -> {
                SpocDetails.callSpoc(context, spocPhone, activeLicenceNo)
            }

            else -> sendMessage(reply)
        }
    }

    fun handleLicenceSelect(lic: ProductService) {
        val key = lic.displayKey()
        activeLicenceNo = key
        val now = timeFormatter.format(Date())
        messages.add(ChatMessage(id = System.currentTimeMillis().toString(), text = "Selected Licence: ${lic.displayName()} (#$key)", timestamp = now, isFromMe = true, senderName = customerName))

        scope.launch {
            isTyping = true
            delay(600)
            isTyping = false
            messages.add(
                ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    text = "Got it! What assistance or issue do you need for ${lic.displayName()} (#$key)?\n\nWould you like me to connect you with $spocName for this licence?",
                    timestamp = timeFormatter.format(Date()),
                    isFromMe = false,
                    senderName = spocName,
                    messageStatus = MessageStatus.DELIVERED,
                    showQuickReplies = true,
                    quickReplies = listOf(
                        "✅ Yes, Connect Me / Create Support Request",
                        "❌ No, Just Browsing / Thanks"
                    )
                )
            )
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FssBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = FssDarkBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                                    .align(Alignment.BottomEnd)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = spocName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FssDarkBlue
                            )
                            Text(
                                text = "Assigned FSS Engineer • Online",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FssDarkBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { SpocDetails.callSpoc(context, spocPhone, activeLicenceNo) }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call SPOC", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = { SpocDetails.openWhatsAppSpoc(context, "Hello $spocName, I need support for my FSS Tally Licence.", spocPhone) }) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "WhatsApp SPOC", tint = Color(0xFF25D366))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Chat Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type your query for $spocName...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFF),
                            unfocusedContainerColor = Color(0xFFF8FAFF)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = { sendMessage(messageText) },
                        containerColor = FssDarkBlue,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF4F7FC))
        ) {
            // Typing Indicator
            if (isTyping) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$spocName is typing...", fontSize = 11.sp, color = FssBlue, fontWeight = FontWeight.Bold)
                }
            }

            // Chat Messages List with In-Chat VERTICAL Stacked Option Buttons & Licence Cards
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        msg = msg,
                        customerLicences = customerLicences,
                        onQuickReply = { handleQuickReply(it) },
                        onLicenceSelect = { handleLicenceSelect(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    msg: ChatMessage,
    customerLicences: List<ProductService>,
    onQuickReply: (String) -> Unit,
    onLicenceSelect: (ProductService) -> Unit
) {
    val alignment = if (msg.isFromMe) Alignment.End else Alignment.Start
    val bgColor = if (msg.isFromMe) FssDarkBlue else Color.White
    val textColor = if (msg.isFromMe) Color.White else Color.Black
    val shape = if (msg.isFromMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!msg.isFromMe && msg.senderName.isNotEmpty()) {
            Text(
                text = msg.senderName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = FssBlue,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Surface(
            shape = shape,
            color = bgColor,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 18.sp
                )

                // Render Vertical Stacked Interactive Options Buttons inside Message Bubble
                if (msg.showQuickReplies && msg.quickReplies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        msg.quickReplies.forEach { reply ->
                            OutlinedButton(
                                onClick = { onQuickReply(reply) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFFF0F5FF),
                                    contentColor = FssDarkBlue
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = reply,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Render In-Chat Interactive Licence Cards (Item Name + Licence Number)
                if (msg.showLicenceList && customerLicences.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        customerLicences.forEach { lic ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLicenceSelect(lic) },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0F5FF),
                                border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = FssBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lic.displayName(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FssDarkBlue
                                        )
                                        Text(
                                            text = "Licence #${lic.displayKey()} • Expiry: ${lic.expiryDate ?: "Active"}",
                                            fontSize = 10.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = FssBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.timestamp,
                    fontSize = 9.sp,
                    color = if (msg.isFromMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
