package com.fsscustomerapplication.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsscustomerapplication.data.remote.RetrofitClient
import com.fsscustomerapplication.data.remote.model.AssistanceHistoryItem
import com.fsscustomerapplication.data.remote.model.DashboardResponse
import com.fsscustomerapplication.data.remote.model.ProfileUpdateRequest
import com.fsscustomerapplication.data.remote.model.RenewalResponse
import com.fsscustomerapplication.data.remote.model.SpocData
import com.fsscustomerapplication.data.remote.model.Ticket
import com.fsscustomerapplication.data.remote.model.TicketActivity
import com.fsscustomerapplication.data.remote.model.TicketRequest
import com.fsscustomerapplication.data.remote.model.Tdl
import com.fsscustomerapplication.data.remote.model.TdlListResponse
import com.fsscustomerapplication.data.remote.model.TdlDetailResponse
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardResponse) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

sealed class RenewalState {
    object Idle : RenewalState()
    object Loading : RenewalState()
    data class Success(val data: RenewalResponse) : RenewalState()
    data class Error(val message: String) : RenewalState()
}

sealed class SpocState {
    object Idle : SpocState()
    object Loading : SpocState()
    data class Success(val data: SpocData) : SpocState()
    data class Error(val message: String) : SpocState()
}

sealed class AssistanceHistoryState {
    object Idle : AssistanceHistoryState()
    object Loading : AssistanceHistoryState()
    data class Success(val data: List<AssistanceHistoryItem>) : AssistanceHistoryState()
    data class Error(val message: String) : AssistanceHistoryState()
}

sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    data class Success(val message: String) : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

sealed class TicketSubmitState {
    object Idle : TicketSubmitState()
    object Loading : TicketSubmitState()
    data class Success(val message: String) : TicketSubmitState()
    data class Error(val message: String) : TicketSubmitState()
}

sealed class TicketDetailState {
    object Idle : TicketDetailState()
    object Loading : TicketDetailState()
    data class Success(
        val ticket: com.fsscustomerapplication.data.remote.model.Ticket,
        val activities: List<com.fsscustomerapplication.data.remote.model.TicketActivity>,
        val quotes: List<com.fsscustomerapplication.data.remote.model.TicketQuote> = emptyList(),
        val invoices: List<com.fsscustomerapplication.data.remote.model.TicketInvoice> = emptyList(),
        val followups: List<com.fsscustomerapplication.data.remote.model.TicketFollowUp> = emptyList()
    ) : TicketDetailState()
    data class Error(val message: String) : TicketDetailState()
}

sealed class TdlListState {
    object Idle : TdlListState()
    object Loading : TdlListState()
    data class Success(val data: List<Tdl>) : TdlListState()
    data class Error(val message: String) : TdlListState()
}

sealed class TdlDetailState {
    object Idle : TdlDetailState()
    object Loading : TdlDetailState()
    data class Success(val data: Tdl) : TdlDetailState()
    data class Error(val message: String) : TdlDetailState()
}

class DashboardViewModel : ViewModel() {

    private val _uiState = mutableStateOf<DashboardState>(DashboardState.Loading)
    val uiState: State<DashboardState> = _uiState

    private val _renewalState = mutableStateOf<RenewalState>(RenewalState.Idle)
    val renewalState: State<RenewalState> = _renewalState

    private val _spocState = mutableStateOf<SpocState>(SpocState.Idle)
    val spocState: State<SpocState> = _spocState

    private val _assistanceHistoryState = mutableStateOf<AssistanceHistoryState>(AssistanceHistoryState.Idle)
    val assistanceHistoryState: State<AssistanceHistoryState> = _assistanceHistoryState

    private val _profileUpdateState = mutableStateOf<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: State<ProfileUpdateState> = _profileUpdateState

    private val _ticketSubmitState = mutableStateOf<TicketSubmitState>(TicketSubmitState.Idle)
    val ticketSubmitState: State<TicketSubmitState> = _ticketSubmitState

    private val _ticketDetailState = mutableStateOf<TicketDetailState>(TicketDetailState.Idle)
    val ticketDetailState: State<TicketDetailState> = _ticketDetailState

    private val _tdlListState = mutableStateOf<TdlListState>(TdlListState.Idle)
    val tdlListState: State<TdlListState> = _tdlListState

    private val _tdlDetailState = mutableStateOf<TdlDetailState>(TdlDetailState.Idle)
    val tdlDetailState: State<TdlDetailState> = _tdlDetailState

    private fun formatHttpError(code: Int): String {
        return when (code) {
            521, 502, 503, 504 -> "Server is under brief maintenance (Error $code). Please tap Retry."
            500 -> "Server is busy. Please tap Retry to reload."
            404 -> "Requested details not found."
            else -> "Server connection issue (Error $code). Please tap Retry."
        }
    }

    fun fetchDashboardData(userId: Int) {
        viewModelScope.launch {
            _uiState.value = DashboardState.Loading
            try {
                val response = RetrofitClient.apiService.getDashboardData(userId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = DashboardState.Success(response.body()!!)
                } else {
                    _uiState.value = DashboardState.Error(formatHttpError(response.code()))
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error("Network error: ${e.localizedMessage ?: "Unable to connect"}")
            }
        }
    }

    fun fetchRenewalDetails(
        licenseNumber: String,
        id: Int? = null,
        productId: Int? = null,
    ) {
        viewModelScope.launch {
            _renewalState.value = RenewalState.Loading
            try {
                val response = RetrofitClient.apiService.getRenewalDetails(
                    licenseNumber = licenseNumber.ifBlank { null },
                    id = id,
                    customerId = id,
                    productId = productId
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    when {
                        body == null ->
                            _renewalState.value = RenewalState.Error("Empty response from server")
                        body.status != "success" ->
                            _renewalState.value = RenewalState.Error(
                                body.message ?: "Renewal not available"
                            )
                        body.data == null ->
                            _renewalState.value = RenewalState.Error(
                                body.message ?: "No renewal data returned"
                            )
                        else ->
                            _renewalState.value = RenewalState.Success(body)
                    }
                } else {
                    _renewalState.value = RenewalState.Error(formatHttpError(response.code()))
                }
            } catch (e: Exception) {
                _renewalState.value = RenewalState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    // ===================== Fetch SPOC =====================
    fun fetchSpoc(
        customerId: Int? = null,
        leadId: Int? = null,
        licenseNumber: String? = null,
        id: Int? = null
    ) {
        viewModelScope.launch {
            _spocState.value = SpocState.Loading
            try {
                val response = RetrofitClient.apiService.getSpoc(
                    customerId = customerId,
                    leadId = leadId,
                    licenseNumber = licenseNumber,
                    id = id
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true) && body.data != null) {
                        _spocState.value = SpocState.Success(body.data)
                    } else {
                        _spocState.value = SpocState.Error(body.message ?: "SPOC not found")
                    }
                } else {
                    _spocState.value = SpocState.Error(formatHttpError(response.code()))
                }
            } catch (e: Exception) {
                _spocState.value = SpocState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    // ===================== NEW: Fetch Assistance History =====================
    fun fetchAssistanceHistory(
        customerId: Int? = null,
        userId: Int? = null
    ) {
        viewModelScope.launch {
            _assistanceHistoryState.value = AssistanceHistoryState.Loading
            try {
                val response = RetrofitClient.apiService.getAssistanceHistory(
                    customerId = customerId,
                    userId = userId
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true)) {
                        _assistanceHistoryState.value = AssistanceHistoryState.Success(
                            body.data ?: emptyList()
                        )
                    } else {
                        _assistanceHistoryState.value = AssistanceHistoryState.Error(
                            body.message ?: "Failed to load history"
                        )
                    }
                } else {
                    _assistanceHistoryState.value = AssistanceHistoryState.Error(
                        formatHttpError(response.code())
                    )
                }
            } catch (e: Exception) {
                _assistanceHistoryState.value = AssistanceHistoryState.Error(
                    e.localizedMessage ?: "Network error"
                )
            }
        }
    }

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            try {
                val response = RetrofitClient.apiService.updateProfile(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _profileUpdateState.value =
                        ProfileUpdateState.Success(response.body()?.message ?: "Updated")
                    fetchDashboardData(request.userId)
                } else {
                    _profileUpdateState.value =
                        ProfileUpdateState.Error(response.body()?.message ?: "Failed")
                }
            } catch (e: Exception) {
                _profileUpdateState.value =
                    ProfileUpdateState.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun submitTicket(request: TicketRequest) {
        viewModelScope.launch {
            _ticketSubmitState.value = TicketSubmitState.Loading
            try {
                val response = RetrofitClient.apiService.submitTicket(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _ticketSubmitState.value =
                        TicketSubmitState.Success(response.body()?.message ?: "Submitted")
                    fetchDashboardData(request.userId)
                } else {
                    _ticketSubmitState.value =
                        TicketSubmitState.Error(response.body()?.message ?: "Failed")
                }
            } catch (e: Exception) {
                _ticketSubmitState.value =
                    TicketSubmitState.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun clearTicketDetail() {
        _ticketDetailState.value = TicketDetailState.Idle
    }

    fun fetchTicketDetails(userId: Int, ticketId: Int) {
        if (userId <= 0 || ticketId <= 0) {
            _ticketDetailState.value = TicketDetailState.Error(
                "Invalid ids: userId=$userId ticketId=$ticketId"
            )
            return
        }

        viewModelScope.launch {
            _ticketDetailState.value = TicketDetailState.Loading
            try {
                val response = RetrofitClient.apiService.getTicketDetails(
                    userId = userId,
                    ticketId = ticketId
                )
                val body = response.body()

                val ok = response.isSuccessful && body != null &&
                        (body.success == true || "success".equals(body.status, ignoreCase = true))

                val ticket = body?.getEffectiveTicket()
                if (ok && ticket != null) {
                    _ticketDetailState.value = TicketDetailState.Success(
                        ticket = ticket,
                        activities = body.activities ?: emptyList(),
                        quotes = body.quotes ?: emptyList(),
                        invoices = body.invoices ?: emptyList(),
                        followups = body.followups ?: emptyList()
                    )
                } else {
                    _ticketDetailState.value = TicketDetailState.Error(
                        body?.message ?: "Failed to load details (HTTP ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _ticketDetailState.value =
                    TicketDetailState.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun resetProfileUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Idle
    }

    fun resetTicketSubmitState() {
        _ticketSubmitState.value = TicketSubmitState.Idle
    }

    fun resetSpocState() {
        _spocState.value = SpocState.Idle
    }

    fun resetAssistanceHistoryState() {
        _assistanceHistoryState.value = AssistanceHistoryState.Idle
    }

    // ===================== TDL Functions =====================
    fun fetchTdls() {
        viewModelScope.launch {
            _tdlListState.value = TdlListState.Loading
            try {
                val response = RetrofitClient.apiService.getTdls()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true)) {
                        _tdlListState.value = TdlListState.Success(body.data ?: emptyList())
                    } else {
                        _tdlListState.value = TdlListState.Error(body.message ?: "Failed to load TDLs")
                    }
                } else {
                    _tdlListState.value = TdlListState.Error(formatHttpError(response.code()))
                }
            } catch (e: Exception) {
                _tdlListState.value = TdlListState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun fetchTdlDetail(id: Int) {
        viewModelScope.launch {
            _tdlDetailState.value = TdlDetailState.Loading
            try {
                val response = RetrofitClient.apiService.getTdlDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status.equals("success", ignoreCase = true) && body.data != null) {
                        _tdlDetailState.value = TdlDetailState.Success(body.data)
                    } else {
                        _tdlDetailState.value = TdlDetailState.Error(body.message ?: "TDL not found")
                    }
                } else {
                    _tdlDetailState.value = TdlDetailState.Error(formatHttpError(response.code()))
                }
            } catch (e: Exception) {
                _tdlDetailState.value = TdlDetailState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun resetTdlListState() {
        _tdlListState.value = TdlListState.Idle
    }

    fun resetTdlDetailState() {
        _tdlDetailState.value = TdlDetailState.Idle
    }
}