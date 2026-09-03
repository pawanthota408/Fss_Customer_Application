package com.fsscustomerapplication.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fsscustomerapplication.data.remote.RetrofitClient
import com.fsscustomerapplication.data.remote.model.ProductService
import kotlinx.coroutines.launch

sealed class LicensesUiState {
    object Loading : LicensesUiState()
    data class Success(
        val mainProducts: List<ProductService>,
        val catalogServices: List<ProductService>,
        val customerOwnedServices: List<ProductService>
    ) : LicensesUiState()
    data class Error(val message: String) : LicensesUiState()
}

class LicensesViewModel : ViewModel() {

    private val _uiState = mutableStateOf<LicensesUiState>(LicensesUiState.Loading)
    val uiState: State<LicensesUiState> = _uiState

    private val _spocState = mutableStateOf<SpocState>(SpocState.Idle)
    val spocState: State<SpocState> = _spocState

    fun fetchLicensesServices(userId: Int) {
        viewModelScope.launch {
            _uiState.value = LicensesUiState.Loading
            try {
                val response = RetrofitClient.apiService.getCustomerLicensesServices(userId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // Main Tally licences only
                    val products = body.mainLicenses
                        ?: body.tallyProducts
                        ?: emptyList()

                    // ★ FIXED: use the full catalog from API (has isOwned flag)
                    // NOT body.services / body.addonLicenses (those are only owned items)
                    val catalog = body.catalogServices
                        ?: emptyList()

                    // Owned / attached services (flat list) – optional
                    val owned = body.ownedServices
                        ?: body.services
                        ?: body.addonLicenses
                        ?: emptyList()

                    _uiState.value = LicensesUiState.Success(
                        mainProducts = products,
                        catalogServices = catalog,
                        customerOwnedServices = owned
                    )
                } else {
                    val code = response.code()
                    val msg = when (code) {
                        521, 502, 503, 504 -> "Server is under brief maintenance (Error $code). Please tap Retry."
                        else -> "Server connection issue (Error $code). Please tap Retry."
                    }
                    _uiState.value = LicensesUiState.Error(msg)
                }
            } catch (e: Exception) {
                _uiState.value = LicensesUiState.Error(
                    "Network error: ${e.localizedMessage ?: "Unable to connect"}"
                )
            }
        }
    }

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
                    _spocState.value = SpocState.Error("Failed: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                _spocState.value = SpocState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}