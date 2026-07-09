package com.pab.digitallearning.ui.teacher.dashboard

import androidx.lifecycle.ViewModel
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.TeacherDashboardData
import com.pab.digitallearning.data.model.TeacherDashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeacherDashboardViewModel : ViewModel() {

    private val _dashboardState = MutableStateFlow<TeacherDashboardUiState>(TeacherDashboardUiState.Loading)
    val dashboardState: StateFlow<TeacherDashboardUiState> = _dashboardState.asStateFlow()

    fun fetchDashboardStats(token: String?) {
        if (token.isNullOrEmpty()) {
            _dashboardState.value = TeacherDashboardUiState.Error("Token tidak valid.")
            return
        }
        _dashboardState.value = TeacherDashboardUiState.Loading

        ApiClient.apiService.getTeacherDashboardStats("Bearer $token")
            .enqueue(object : Callback<TeacherDashboardResponse> {
                override fun onResponse(
                    call: Call<TeacherDashboardResponse>,
                    response: Response<TeacherDashboardResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _dashboardState.value = TeacherDashboardUiState.Success(response.body()!!.data)
                    } else {
                        _dashboardState.value = TeacherDashboardUiState.Error("Gagal memuat statistik dashboard: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<TeacherDashboardResponse>, t: Throwable) {
                    _dashboardState.value = TeacherDashboardUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }
}

sealed class TeacherDashboardUiState {
    object Loading : TeacherDashboardUiState()
    data class Success(val data: TeacherDashboardData) : TeacherDashboardUiState()
    data class Error(val message: String) : TeacherDashboardUiState()
}
