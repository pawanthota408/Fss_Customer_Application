package com.fsscustomerapplication.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fsscustomerapplication.data.remote.model.Ticket
import com.fsscustomerapplication.data.remote.model.TicketActivity
import com.fsscustomerapplication.ui.theme.FssBlue
import com.fsscustomerapplication.ui.theme.FssDarkBlue
import com.fsscustomerapplication.ui.viewmodels.DashboardState
import com.fsscustomerapplication.ui.viewmodels.DashboardViewModel
import com.fsscustomerapplication.ui.viewmodels.TicketDetailState
import com.fsscustomerapplication.utils.tr
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    userId: Int,
    onBack: () -> Unit,
    onPayWithRazorpay: ((com.fsscustomerapplication.data.remote.model.TicketInvoice) -> Unit)? = null
) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState
    val detailState by viewModel.ticketDetailState

    var selectedTicketId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(userId) {
        if (userId > 0) {
            viewModel.fetchDashboardData(userId)
        }
    }

    fun closeDetail() {
        selectedTicketId = null
        viewModel.clearTicketDetail()
    }

    BackHandler(enabled = selectedTicketId != null) {
        closeDetail()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTicketId == null) tr("My Tickets") else tr("Ticket Details"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTicketId == null) tr("Track your requests") else tr("View lead activity"),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedTicketId == null) onBack() else closeDetail()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTicketId == null) {
                        IconButton(onClick = { viewModel.fetchDashboardData(userId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFF))
        ) {
            // -------- LIST --------
            AnimatedVisibility(
                visible = selectedTicketId == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (val state = uiState) {
                    is DashboardState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = FssDarkBlue
                        )
                    }
                    is DashboardState.Success -> {
                        val tickets = state.data.tickets ?: emptyList()
                        if (tickets.isEmpty()) {
                            EmptyTicketsView()
                        } else {
                            TicketList(tickets) { ticket ->
                                Log.d("TicketAPI", "click userId=$userId ticketId=${ticket.id}")
                                if (userId <= 0 || (ticket.id <= 0)) return@TicketList
                                selectedTicketId = ticket.id
                                viewModel.fetchTicketDetails(userId, ticket.id)
                            }
                        }
                    }
                    is DashboardState.Error -> {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }
            }

            // -------- DETAIL --------
            AnimatedVisibility(
                visible = selectedTicketId != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(Modifier.fillMaxSize()) {
                    when (val state = detailState) {
                        is TicketDetailState.Loading,
                        is TicketDetailState.Idle -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = FssDarkBlue
                            )
                        }
                        is TicketDetailState.Success -> {
                            TicketDetailView(
                                ticket = state.ticket,
                                activities = state.activities,
                                quotes = state.quotes,
                                invoices = state.invoices,
                                followups = state.followups,
                                onPayWithRazorpay = onPayWithRazorpay
                            )
                        }
                        is TicketDetailState.Error -> {
                            Text(
                                text = state.message,
                                color = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketList(tickets: List<Ticket>, onTicketClick: (Ticket) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        items(tickets, key = { it.id }) { ticket ->
            TicketCard(ticket) { onTicketClick(ticket) }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = if (ticket.category == "Product") Color(0xFFE3F2FD) else Color(0xFFF3E5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = ticket.category?.uppercase() ?: "REQUEST",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ticket.category == "Product") Color(0xFF1976D2) else Color(0xFF7B1FA2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ticket.subject,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FssDarkBlue
                    )
                }
                StatusBadge(status = ticket.status, isUnpaid = ticket.isPendingPayment())
            }

            if (!ticket.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ticket.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFF1F1F1)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = ticket.createdAt, fontSize = 11.sp, color = Color.Gray)
                }
                Text(
                    text = "ID: #${ticket.id}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun TicketDetailView(
    ticket: Ticket?,
    activities: List<TicketActivity>,
    quotes: List<com.fsscustomerapplication.data.remote.model.TicketQuote> = emptyList(),
    invoices: List<com.fsscustomerapplication.data.remote.model.TicketInvoice> = emptyList(),
    followups: List<com.fsscustomerapplication.data.remote.model.TicketFollowUp> = emptyList(),
    onPayWithRazorpay: ((com.fsscustomerapplication.data.remote.model.TicketInvoice) -> Unit)? = null
) {
    if (ticket == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ticket Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(ticket.status, isUnpaid = ticket.isPendingPayment())
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Raised on ${ticket.createdAt}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = ticket.subject,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ticket.description ?: "No description provided",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color(0xFFF1F1F1)
                )

                InfoRow("Category", ticket.category ?: "N/A")
                InfoRow("License No", ticket.licenseNo ?: "N/A")
                InfoRow("Priority", ticket.priority ?: "Normal")
                if (!ticket.assignedName.isNullOrBlank()) {
                    InfoRow("Assigned SPOC", ticket.assignedName)
                }
                if (!ticket.updatedAt.isNullOrBlank()) {
                    InfoRow("Last update", ticket.updatedAt)
                }
            }
        }

        // 2. Real Quotations Section (ONLY shown if generated on server)
        if (quotes.isNotEmpty()) {
            quotes.forEach { quote ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE3F2FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = FssBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Quotation Details",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FssDarkBlue
                                    )
                                    Text(
                                        text = "Quotation prepared by employee",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = quote.status ?: "Prepared",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FssBlue
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))

                        InfoRow("Quotation No", quote.quoteNo ?: "N/A")
                        InfoRow("Quote Date", quote.quoteDate ?: quote.createdAt ?: "N/A")
                        if (!quote.validUntil.isNullOrBlank()) {
                            InfoRow("Valid Until", quote.validUntil)
                        }
                        InfoRow("Subtotal Amount", "₹ ${String.format(Locale.US, "%.2f", quote.amount ?: 0.0)}")
                        InfoRow("Tax (GST)", "₹ ${String.format(Locale.US, "%.2f", quote.tax ?: 0.0)}")
                        InfoRow("Total Quotation Amount", "₹ ${String.format(Locale.US, "%.2f", quote.total ?: 0.0)}")
                    }
                }
            }
        }

        // 3. Real Invoices & Payment Summary Section (ONLY shown if generated on server)
        if (invoices.isNotEmpty()) {
            invoices.forEach { inv ->
                val totAmt = inv.displayTotalAmount()
                val paidAmt = inv.displayPaidAmount()
                val balAmt = inv.displayBalanceAmount()
                val isPendingPay = inv.isUnpaidOrPartial()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5FF)),
                    border = BorderStroke(1.dp, FssBlue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3E5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color(0xFF7B1FA2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Tax Invoice Details",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FssDarkBlue
                                    )
                                    Text(
                                        text = "Invoice issued by employee",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            val invStatus = if (isPendingPay) (inv.status ?: "Unpaid") else "Paid"
                            Surface(
                                color = if (isPendingPay) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = invStatus,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPendingPay) Color(0xFFE65100) else Color(0xFF2E7D32)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE0E8F8))

                        InfoRow("Invoice No", inv.invoiceNo ?: "N/A")
                        InfoRow("Invoice Date", inv.invoiceDate ?: inv.createdAt ?: "N/A")
                        if (!inv.dueDate.isNullOrBlank()) {
                            InfoRow("Due Date", inv.dueDate)
                        }
                        InfoRow("Total Invoice Amount", "₹ ${String.format(Locale.US, "%.2f", totAmt)}")
                        InfoRow("Amount Paid", "₹ ${String.format(Locale.US, "%.2f", paidAmt)}")
                        InfoRow("Balance Due", "₹ ${String.format(Locale.US, "%.2f", balAmt)}")

                        // Payment Button (if balance due > 0)
                        if (isPendingPay) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onPayWithRazorpay?.invoke(inv) },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF528FF0))
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pay ₹ ${String.format(Locale.US, "%.2f", balAmt)} Now with Razorpay",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Real Follow-ups Section (ONLY shown if present in API)
        if (followups.isNotEmpty()) {
            Text(
                text = tr("Follow-up History"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = FssDarkBlue,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    followups.forEachIndexed { index, fu ->
                        FollowUpItem(fu)
                        if (index < followups.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        }
                    }
                }
            }
        }

        Text(
            text = tr("Activity History"),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = FssDarkBlue,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (activities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
            ) {
                Text(
                    text = tr("No activity recorded yet."),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            activities.forEachIndexed { index, activity ->
                ActivityItem(
                    activity = activity,
                    isLast = index == activities.size - 1
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ActivityItem(activity: TicketActivity, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(FssBlue)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(FssBlue.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activity.status,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activity.createdAt,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.message,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Updated by ${activity.updatedBy}",
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = FssDarkBlue
        )
    }
}

@Composable
fun StatusBadge(status: String, isUnpaid: Boolean = false) {
    val (bgColor, textColor) = when {
        isUnpaid -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        status.lowercase().contains("pending") || status.lowercase().contains("new") || status.lowercase().contains("unpaid") -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        status.lowercase().contains("progress") || status.lowercase().contains("assigned") -> Color(0xFFE1F5FE) to Color(0xFF01579B)
        status.lowercase().contains("completed") || status.lowercase().contains("resolved") || status.lowercase().contains("closed") || status.lowercase().contains("paid") -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        else -> Color(0xFFEEEEEE) to Color(0xFF616161)
    }

    val displayStatus = if (isUnpaid) "Unpaid Invoice" else status

    Surface(color = bgColor, shape = CircleShape) {
        Text(
            text = displayStatus,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun FollowUpItem(fu: com.fsscustomerapplication.data.remote.model.TicketFollowUp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (fu.isCompleted()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                tint = if (fu.isCompleted()) Color(0xFF2E7D32) else Color(0xFFE65100),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fu.displayEmployee(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FssDarkBlue
                )

                Surface(
                    color = if (fu.isCompleted()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = fu.status ?: "Pending",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (fu.isCompleted()) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = fu.displayDateTime(),
                fontSize = 10.sp,
                color = FssBlue,
                fontWeight = FontWeight.Medium
            )

            if (!fu.remarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = fu.remarks,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }

            if (!fu.completedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Completed on ${fu.completedAt}",
                    fontSize = 10.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyTicketsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Tickets Found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = "Your raised requests will appear here once you submit a product or service inquiry.",
            fontSize = 14.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}