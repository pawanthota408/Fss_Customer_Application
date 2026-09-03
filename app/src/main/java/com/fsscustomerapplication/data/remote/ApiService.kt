package com.fsscustomerapplication.data.remote

import com.fsscustomerapplication.data.remote.model.DashboardResponse
import com.fsscustomerapplication.data.remote.model.LoginRequest
import com.fsscustomerapplication.data.remote.model.LoginResponse
import com.fsscustomerapplication.data.remote.model.LoginSliderResponse
import com.fsscustomerapplication.data.remote.model.RenewalResponse
import com.fsscustomerapplication.data.remote.model.ProfileUpdateRequest
import com.fsscustomerapplication.data.remote.model.ProfileUpdateResponse
import com.fsscustomerapplication.data.remote.model.TicketRequest
import com.fsscustomerapplication.data.remote.model.CustomerLicensesServicesResponse
import com.fsscustomerapplication.data.remote.model.SpocResponse
import com.fsscustomerapplication.data.remote.model.TdlListResponse
import com.fsscustomerapplication.data.remote.model.TdlDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("dashboard.php")
    suspend fun getDashboardData(@Query("user_id") userId: Int): Response<DashboardResponse>

    @GET("login_sliders.php")
    suspend fun getLoginSliders(): Response<LoginSliderResponse>

    @GET("renewal_details.php")
    suspend fun getRenewalDetails(
        @Query("license_number") licenseNumber: String? = null,
        @Query("id") id: Int? = null,
        @Query("customer_id") customerId: Int? = null,
        @Query("product_id") productId: Int? = null,
    ): Response<RenewalResponse>

    @POST("update_profile.php")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<ProfileUpdateResponse>

    @POST("submit_ticket.php")
    suspend fun submitTicket(
        @Body request: TicketRequest,
    ): Response<ProfileUpdateResponse>

    @GET("ticket_details.php")
    suspend fun getTicketDetails(
        @Query("user_id") userId: Int,
        @Query("ticket_id") ticketId: Int
    ): Response<com.fsscustomerapplication.data.remote.model.TicketDetailResponse>

    /**
     * Customer Licenses + Services / Addons
     * Returns only the logged-in customer's licenses and related services/addons.
     */
    @GET("licenses_services.php")
    suspend fun getCustomerLicensesServices(
        @Query("user_id") userId: Int,
        @Query("search") search: String? = null,
    ): Response<CustomerLicensesServicesResponse>

    // =====================================================
    // NEW: Separate SPOC API
    // =====================================================
    /**
     * Get Assigned Service Engineer (SPOC)
     * You can pass any one of these parameters:
     * - customer_id
     * - lead_id
     * - license_number
     * - id (customer_licenses.id)
     */
    @GET("get_spoc.php")
    suspend fun getSpoc(
        @Query("customer_id") customerId: Int? = null,
        @Query("lead_id") leadId: Int? = null,
        @Query("license_number") licenseNumber: String? = null,
        @Query("id") id: Int? = null
    ): Response<SpocResponse>

    @GET("get_assistance_history.php")
    suspend fun getAssistanceHistory(
        @Query("customer_id") customerId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("license_number") licenseNumber: String? = null
    ): Response<com.fsscustomerapplication.data.remote.model.AssistanceHistoryResponse>

    @GET("log_call.php")
    suspend fun logCall(
        @Query("customer_id") customerId: Int? = null,
        @Query("user_id") userId: Int? = null,
        @Query("name") name: String? = null,
        @Query("mobile") mobile: String? = null,
        @Query("email") email: String? = null,
        @Query("license_number") licenseNumber: String? = null,
        @Query("call_date") callDate: String? = null,
        @Query("call_time") callTime: String? = null
    ): Response<ProfileUpdateResponse>

    // =====================================================
    // TDL APIs
    // =====================================================
    @GET("get_tdls.php")
    suspend fun getTdls(): Response<TdlListResponse>

    @GET("get_tdl_detail.php")
    suspend fun getTdlDetail(
        @Query("id") id: Int
    ): Response<TdlDetailResponse>
}