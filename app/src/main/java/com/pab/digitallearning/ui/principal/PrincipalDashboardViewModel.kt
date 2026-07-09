package com.pab.digitallearning.ui.principal

import androidx.lifecycle.ViewModel
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.PrincipalDashboardData
import com.pab.digitallearning.data.model.PrincipalDashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class PrincipalDashboardUiState {
    object Loading : PrincipalDashboardUiState()
    data class Success(val data: PrincipalDashboardData) : PrincipalDashboardUiState()
    data class Error(val message: String) : PrincipalDashboardUiState()
}

class PrincipalDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PrincipalDashboardUiState>(PrincipalDashboardUiState.Loading)
    val uiState: StateFlow<PrincipalDashboardUiState> = _uiState.asStateFlow()

    fun fetchDashboard(token: String?) {
        if (token.isNullOrEmpty()) {
            _uiState.value = PrincipalDashboardUiState.Error("Token tidak valid.")
            return
        }

        _uiState.value = PrincipalDashboardUiState.Loading

        ApiClient.apiService.getPrincipalDashboard("Bearer $token")
            .enqueue(object : Callback<PrincipalDashboardResponse> {
                override fun onResponse(
                    call: Call<PrincipalDashboardResponse>,
                    response: Response<PrincipalDashboardResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = PrincipalDashboardUiState.Success(response.body()!!.data)
                    } else {
                        _uiState.value = PrincipalDashboardUiState.Error("Gagal mengambil data dashboard.")
                    }
                }

                override fun onFailure(call: Call<PrincipalDashboardResponse>, t: Throwable) {
                    _uiState.value = PrincipalDashboardUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }
}
