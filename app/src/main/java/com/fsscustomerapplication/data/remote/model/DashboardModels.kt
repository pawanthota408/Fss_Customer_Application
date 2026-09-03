package com.fsscustomerapplication.data.remote.model

import com.google.gson.annotations.SerializedName

data class CustomerDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("customer_id") val customerId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("company") val company: String?,
    @SerializedName("address") val address: String? = null,
    @SerializedName("active_services") val activeServices: Int? = 0,
    @SerializedName("pending_requests") val pendingRequests: Int? = 0,
    @SerializedName("invoices_count") val invoicesCount: Int? = 0
)

data class ProductService(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String? = null,
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("product_name") val productNameAlt: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("iconLink") val iconLink: String? = null,
    @SerializedName("icon_link") val iconLinkAlt: String? = null,
    @SerializedName("purchaseDate") val purchaseDate: String? = null,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("validTill") val validTill: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("license_key") val licenseKey: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("serial_number") val serialNumber: String? = null,
    @SerializedName("is_main") val isMain: Int? = 0,
    @SerializedName("type") val type: String? = null,
    @SerializedName("is_owned") val isOwned: Boolean? = false,

    // SPOC fields
    @SerializedName("spoc_name") val spocName: String? = null,
    @SerializedName("spoc_phone") val spocPhone: String? = null,
    @SerializedName("spocName") val spocNameCamel: String? = null,
    @SerializedName("spocPhone") val spocPhoneCamel: String? = null,
    @SerializedName("emp_name") val empName: String? = null,
    @SerializedName("emp_phone") val empPhone: String? = null,
    @SerializedName("engineer_name") val engineerName: String? = null,
    @SerializedName("engineer_phone") val engineerPhone: String? = null,
    @SerializedName("assigned_to_name") val assignedToName: String? = null,
    @SerializedName("assigned_to_phone") val assignedToPhone: String? = null,

    @SerializedName("services") val services: List<ProductService>? = emptyList(),
    @SerializedName("addons") val addons: List<ProductService>? = emptyList()
) {
    fun displayName(): String = name ?: productName ?: productNameAlt ?: "Unknown Product"
    fun displayIcon(): String? = iconLink ?: iconLinkAlt
    fun displayKey(): String = licenseKey ?: number ?: serialNumber ?: "N/A"
    fun nestedServices(): List<ProductService> = services?.ifEmpty { addons } ?: emptyList()

    fun getEffectiveSpocName(): String {
        val name = listOfNotNull(
            spocName, spocNameCamel,
            empName, engineerName, assignedToName
        ).firstOrNull {
            it.isNotBlank() &&
                    !it.equals("Not Assigned", ignoreCase = true)
        }
        return name ?: "Friends Software Solutions Support Team"
    }

    fun getEffectiveSpocPhone(): String {
        val phone = listOfNotNull(
            spocPhone, spocPhoneCamel,
            empPhone, engineerPhone, assignedToPhone
        ).firstOrNull { it.isNotBlank() }
        return phone ?: "+919848012345"
    }
}

data class CustomerInfo(
    val id: Int,
    @SerializedName("customer_id") val customerId: Int,
    val name: String?,
    val email: String?,
    val phone: String?,
    val company: String?
)

data class CustomerLicensesServicesResponse(
    val success: Boolean,
    val error: String?,
    val customer: CustomerInfo?,
    @SerializedName("main_licenses") val mainLicenses: List<ProductService>?,
    @SerializedName("tally_products") val tallyProducts: List<ProductService>?,
    val services: List<ProductService>?,
    @SerializedName("addon_licenses") val addonLicenses: List<ProductService>?,
    val licences: List<ProductService>?,
    @SerializedName("licence_list") val licenceList: List<ProductService>?,
    @SerializedName("owned_services") val ownedServices: List<ProductService>?,
    @SerializedName("catalog_services") val catalogServices: List<ProductService>?,
    val total: Int?,
    @SerializedName("main_count") val mainCount: Int?,
    @SerializedName("addon_count") val addonCount: Int?
)

data class License(
    @SerializedName("number") val number: String,
    @SerializedName("status") val status: String,
    @SerializedName("validTill") val validTill: String,
    @SerializedName("expiry_date") val expiryDate: String? = null,
    @SerializedName("days_left") val daysLeft: Int? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("icon_link") val iconLink: String? = null,
    @SerializedName("is_main") val isMain: Int? = 0
)

// ===================== IMPROVED RenewalDetails =====================
data class RenewalDetails(
    @SerializedName("license_number") val licenseNumber: String? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("service_name") val serviceName: String? = null,
    @SerializedName("current_expiry") val currentExpiry: String? = null,
    @SerializedName("currentExpiry") val currentExpiryCamel: String? = null,
    @SerializedName("next_expiry") val nextExpiry: String? = null,
    @SerializedName("plan_type") val planType: String? = null,
    @SerializedName("planType") val planTypeCamel: String? = null,
    @SerializedName("subtotal") val subtotal: Double? = 0.0,
    @SerializedName("gst") val gst: Double? = 0.0,
    @SerializedName("total") val total: Double? = 0.0,
    @SerializedName("icon_link") val iconLink: String? = null,
    @SerializedName("iconLink") val iconLinkCamel: String? = null,
    @SerializedName("days_left") val daysLeft: Int? = 0,
    @SerializedName("renewable") val renewable: Boolean? = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("is_expired") val isExpired: Boolean? = false,
    @SerializedName("category") val category: String? = null,
    @SerializedName("gst_percent") val gstPercent: Int? = 18,
    @SerializedName("discount") val discount: Double? = 0.0,
    @SerializedName("proforma_no") val proformaNo: String? = null,
    @SerializedName("price_source") val priceSource: String? = null,

    // ===== SPOC fields =====
    @SerializedName("spoc_id") val spocId: Int? = null,
    @SerializedName("spocId") val spocIdCamel: Int? = null,
    @SerializedName("spoc_name") val spocName: String? = null,
    @SerializedName("spocName") val spocNameCamel: String? = null,
    @SerializedName("spoc_phone") val spocPhone: String? = null,
    @SerializedName("spocPhone") val spocPhoneCamel: String? = null,
    @SerializedName("spoc_email") val spocEmail: String? = null,
    @SerializedName("spocEmail") val spocEmailCamel: String? = null,
    @SerializedName("emp_name") val empName: String? = null,
    @SerializedName("emp_phone") val empPhone: String? = null,
    @SerializedName("engineer_name") val engineerName: String? = null,
    @SerializedName("engineer_phone") val engineerPhone: String? = null,
    @SerializedName("assigned_to_name") val assignedToName: String? = null,
    @SerializedName("assigned_to_phone") val assignedToPhone: String? = null,
    @SerializedName("spoc_source") val spocSource: String? = null
) {
    fun getEffectiveSpocName(): String {
        val name = listOfNotNull(
            spocName, spocNameCamel,
            empName, engineerName, assignedToName
        ).firstOrNull {
            it.isNotBlank() && !it.equals("Not Assigned", ignoreCase = true)
        }
        return name ?: "Friends Software Solutions Support Team"
    }

    fun getEffectiveSpocPhone(): String {
        val phone = listOfNotNull(
            spocPhone, spocPhoneCamel,
            empPhone, engineerPhone, assignedToPhone
        ).firstOrNull { it.isNotBlank() }
        return phone ?: "+919848012345"
    }

    fun getEffectiveSpocEmail(): String {
        return listOfNotNull(spocEmail, spocEmailCamel)
            .firstOrNull { it.isNotBlank() } ?: ""
    }

    fun hasValidSpoc(): Boolean = getEffectiveSpocPhone().isNotBlank()

    fun displayProductName(): String = productName ?: serviceName ?: "Service"
    fun displayPlanType(): String = planType ?: planTypeCamel ?: "Service | 1 Year"
    fun displayIcon(): String? = iconLink ?: iconLinkCamel
    fun displayCurrentExpiry(): String = currentExpiry ?: currentExpiryCamel ?: "N/A"
    fun displayLicenseNumber(): String = licenseNumber ?: "—"
    fun displayNextExpiry(): String = nextExpiry ?: "—"
    fun displayDaysLeft(): Int = daysLeft ?: 0
}

data class RenewalResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: RenewalDetails? = null
)

// ===================== SPOC API Models =====================
data class SpocResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SpocData? = null
)

data class SpocData(
    @SerializedName("spoc_id") val spocId: Int? = null,
    @SerializedName("spoc_name") val spocName: String? = null,
    @SerializedName("spoc_phone") val spocPhone: String? = null,
    @SerializedName("spoc_email") val spocEmail: String? = null,
    @SerializedName("spocId") val spocIdCamel: Int? = null,
    @SerializedName("spocName") val spocNameCamel: String? = null,
    @SerializedName("spocPhone") val spocPhoneCamel: String? = null,
    @SerializedName("spocEmail") val spocEmailCamel: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("customer_id") val customerId: Int? = null,
    @SerializedName("lead_id") val leadId: Int? = null
) {
    fun getEffectiveName(): String {
        val name = listOfNotNull(spocName, spocNameCamel)
            .firstOrNull { it.isNotBlank() && !it.equals("Not Assigned", ignoreCase = true) }
        return name ?: "Friends Software Solutions Support Team"
    }

    fun getEffectivePhone(): String {
        val phone = listOfNotNull(spocPhone, spocPhoneCamel)
            .firstOrNull { it.isNotBlank() }
        return phone ?: "+919848012345"
    }

    fun getEffectiveEmail(): String {
        return listOfNotNull(spocEmail, spocEmailCamel)
            .firstOrNull { it.isNotBlank() } ?: ""
    }

    fun hasValidSpoc(): Boolean = getEffectivePhone().isNotBlank()
}

// ===================== NEW: Assistance History Models =====================
data class AssistanceHistoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String? = null,           // "call" or "ticket"
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("completed_at") val completedAt: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("mobile") val mobile: String? = null
) {
    fun displayTitle(): String = title ?: if (type == "call") "Call Support" else "Support Ticket"
    fun displayDescription(): String = description?.takeIf { it.isNotBlank() } ?: "Service assistance"
    fun displayDateTime(): String {
        val d = date ?: ""
        val t = time ?: ""
        return listOf(d, t).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { createdAt ?: "N/A" }
    }
    fun displayStatus(): String = status ?: "Completed"
    fun isCompleted(): Boolean = listOf("completed", "resolved", "closed", "done").contains(status?.lowercase())
}

data class AssistanceHistoryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("count") val count: Int? = 0,
    @SerializedName("data") val data: List<AssistanceHistoryItem>? = null,
    @SerializedName("message") val message: String? = null
)

// ===================== Other Models =====================
data class DashboardResponse(
    @SerializedName("status") val status: String,
    @SerializedName("customer") val customer: CustomerDetails? = null,
    @SerializedName("products") val products: List<ProductService>? = emptyList(),
    @SerializedName("services") val services: List<ProductService>? = emptyList(),
    @SerializedName("licence_list") val licences: List<License>? = emptyList(),
    @SerializedName("licences") val allLicences: List<ProductService>? = emptyList(),
    @SerializedName("tickets") val tickets: List<Ticket>? = emptyList(),
    @SerializedName("main_licenses") val mainLicenses: List<ProductService>? = emptyList(),
    @SerializedName("tally_products") val tallyProducts: List<ProductService>? = emptyList(),
    @SerializedName("owned_services") val ownedServices: List<ProductService>? = emptyList(),
    @SerializedName("expiring_services") val expiringServices: List<ProductService>? = emptyList()
)

data class Ticket(
    @SerializedName("id") val id: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("license_no") val licenseNo: String? = null,
    @SerializedName("priority") val priority: String? = "Normal",
    @SerializedName("customer_name") val customerName: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("assigned_name") val assignedName: String? = null,

    // Quotation Details
    @SerializedName("quotation_no") val quotationNo: String? = null,
    @SerializedName("quotation_date") val quotationDate: String? = null,
    @SerializedName("quotation_amount") val quotationAmount: Double? = null,
    @SerializedName("quotation_status") val quotationStatus: String? = null,

    // Invoice Details
    @SerializedName("invoice_no") val invoiceNo: String? = null,
    @SerializedName("invoice_date") val invoiceDate: String? = null,
    @SerializedName("invoice_amount") val invoiceAmount: Double? = null,
    @SerializedName("invoice_status") val invoiceStatus: String? = null,

    // Payment Details
    @SerializedName("paid_amount") val paidAmount: Double? = null,
    @SerializedName("balance_amount") val balanceAmount: Double? = null,
    @SerializedName("payment_status") val paymentStatus: String? = null
) {
    fun isPendingPayment(): Boolean {
        if (paymentStatus != null) {
            val ps = paymentStatus.lowercase()
            if (ps.contains("unpaid") || ps.contains("partially") || ps.contains("pending")) return true
            if (ps.contains("paid") || ps.contains("completed")) return false
        }
        val s = status.lowercase()
        return s.contains("unpaid") || s.contains("partially") || s.contains("pending")
    }
    fun displayQuotationNo(): String = quotationNo ?: "QUOT-FSS-${id}"
    fun displayInvoiceNo(): String = invoiceNo ?: "INV-FSS-${id}"
    fun displayInvoiceAmount(): Double = invoiceAmount ?: quotationAmount ?: 0.0
    fun displayPaidAmount(): Double = paidAmount ?: displayInvoiceAmount()
    fun displayBalanceAmount(): Double = balanceAmount ?: (displayInvoiceAmount() - displayPaidAmount()).coerceAtLeast(0.0)
    fun displayPaymentStatus(): String = paymentStatus ?: if (displayBalanceAmount() <= 0.0 && displayInvoiceAmount() > 0) "Paid" else "Pending"
}

data class TicketActivity(
    @SerializedName("id") val id: Int,
    @SerializedName("ticket_id") val ticketId: Int,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_by") val updatedBy: String? = "System"
)

data class TicketQuote(
    @SerializedName("id") val id: Int,
    @SerializedName("quote_no") val quoteNo: String? = null,
    @SerializedName("customer_name") val customerName: String? = null,
    @SerializedName("amount") val amount: Double? = 0.0,
    @SerializedName("tax") val tax: Double? = 0.0,
    @SerializedName("total") val total: Double? = 0.0,
    @SerializedName("status") val status: String? = "Draft",
    @SerializedName("quote_date") val quoteDate: String? = null,
    @SerializedName("valid_until") val validUntil: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("admin_notes") val adminNotes: String? = null
)

data class TicketInvoice(
    @SerializedName("id") val id: Int,
    @SerializedName("invoice_no") val invoiceNo: String? = null,
    @SerializedName("customer_name") val customerName: String? = null,
    @SerializedName("amount") val amount: Double? = 0.0,
    @SerializedName("tax") val tax: Double? = 0.0,
    @SerializedName("total") val total: Double? = 0.0,
    @SerializedName("paid_amount") val paidAmount: Double? = null,
    @SerializedName("amount_paid") val amountPaid: Double? = null,
    @SerializedName("status") val status: String? = "Unpaid",
    @SerializedName("invoice_date") val invoiceDate: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    fun displayTotalAmount(): Double = total ?: ((amount ?: 0.0) + (tax ?: 0.0))
    fun displayPaidAmount(): Double {
        if (paidAmount != null) return paidAmount
        if (amountPaid != null) return amountPaid
        return if (status.equals("Paid", ignoreCase = true)) displayTotalAmount() else 0.0
    }
    fun displayBalanceAmount(): Double = (displayTotalAmount() - displayPaidAmount()).coerceAtLeast(0.0)
    fun isUnpaidOrPartial(): Boolean = displayBalanceAmount() > 0.0 || !status.equals("Paid", ignoreCase = true)
}

data class TicketDetailResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("ticket") val ticket: Ticket? = null,
    @SerializedName("lead") val lead: Ticket? = null,
    @SerializedName("activities") val activities: List<TicketActivity>? = emptyList(),
    @SerializedName("quotes") val quotes: List<TicketQuote>? = emptyList(),
    @SerializedName("invoices") val invoices: List<TicketInvoice>? = emptyList(),
    @SerializedName("followups") val followups: List<TicketFollowUp>? = emptyList()
) {
    fun getEffectiveTicket(): Ticket? = ticket ?: lead
}

data class TicketFollowUp(
    @SerializedName("id") val id: Int,
    @SerializedName("lead_id") val leadId: Int? = null,
    @SerializedName("employee_id") val employeeId: Int? = null,
    @SerializedName("employee_name") val employeeName: String? = null,
    @SerializedName("follow_up_date") val followUpDate: String? = null,
    @SerializedName("follow_up_time") val followUpTime: String? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("status") val status: String? = "Pending",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("completed_at") val completedAt: String? = null
) {
    fun displayEmployee(): String = employeeName?.takeIf { it.isNotBlank() } ?: "Service Engineer"
    fun displayDateTime(): String {
        val d = followUpDate ?: ""
        val t = followUpTime ?: ""
        return listOf(d, t).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { createdAt ?: "N/A" }
    }
    fun isCompleted(): Boolean = status.equals("Completed", ignoreCase = true) || !completedAt.isNullOrBlank()
}

data class TdlModule(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val videoUrl: String? = null
)

// ===================== TDL API Models =====================
data class TdlImage(
    @SerializedName("id") val id: Int,
    @SerializedName("tdl_id") val tdlId: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("alt_text") val altText: String? = null,
    @SerializedName("sort_order") val sortOrder: Int? = 0,
    @SerializedName("is_primary") val isPrimary: Int? = 0,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Tdl(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("short_desc") val shortDesc: String? = null,
    @SerializedName("full_desc") val fullDesc: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("price") val price: Double? = 0.0,
    @SerializedName("is_featured") val isFeatured: Int? = 0,
    @SerializedName("is_active") val isActive: Int? = 1,
    @SerializedName("download_link") val downloadLink: String? = null,
    @SerializedName("demo_video") val demoVideo: String? = null,
    @SerializedName("compatibility") val compatibility: String? = null,
    @SerializedName("sort_order") val sortOrder: Int? = 0,
    @SerializedName("meta_title") val metaTitle: String? = null,
    @SerializedName("meta_description") val metaDescription: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("images") val images: List<TdlImage>? = emptyList(),
    @SerializedName("primary_image") val primaryImage: String? = null
) {
    fun displayName(): String = name
    fun displayShortDesc(): String = shortDesc ?: ""
    fun displayFullDesc(): String = fullDesc ?: shortDesc ?: ""
    fun displayPrice(): String = if (price != null && price > 0) "₹${price}" else "Contact for Price"
    fun displayImage(): String? = primaryImage ?: images?.firstOrNull()?.imageUrl
    fun displayVideo(): String? = demoVideo
    fun displayCompatibility(): String? = compatibility
    fun isFeatured(): Boolean = isFeatured == 1
    fun toTdlModule(): TdlModule = TdlModule(
        id = id,
        name = displayName(),
        description = if (!shortDesc.isNullOrBlank()) shortDesc else (fullDesc ?: "Premium TDL addon for Tally"),
        imageUrl = primaryImage ?: images?.firstOrNull()?.imageUrl ?: "https://crm.friendssoftwaresolutions.in/assets/images/logo.png",
        videoUrl = demoVideo
    )
}

data class TdlListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("count") val count: Int? = 0,
    @SerializedName("data") val data: List<Tdl>? = emptyList()
)

data class TdlDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Tdl? = null
)

data class ProfileUpdateRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("update_type") val updateType: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("company_name") val companyName: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("new_password") val newPassword: String? = null
)

data class ProfileUpdateResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class TicketRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("mobile") val mobile: String,
    @SerializedName("email") val email: String,
    @SerializedName("company") val company: String,
    @SerializedName("license_no") val licenseNo: String? = null
)