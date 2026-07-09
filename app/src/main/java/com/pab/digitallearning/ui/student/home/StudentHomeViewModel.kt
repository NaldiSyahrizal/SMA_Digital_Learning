package com.pab.digitallearning.ui.student.home

import androidx.lifecycle.ViewModel
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.StudentDashboardData
import com.pab.digitallearning.data.model.StudentDashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed interface StudentHomeUiState {
    object Loading : StudentHomeUiState
    data class Success(val data: StudentDashboardData) : StudentHomeUiState
    data class Error(val message: String) : StudentHomeUiState
}

class StudentHomeViewModel : ViewModel() {

    private val _homeState = MutableStateFlow<StudentHomeUiState>(StudentHomeUiState.Loading)
    val homeState: StateFlow<StudentHomeUiState> = _homeState.asStateFlow()

    fun fetchDashboard(token: String?) {
        if (token.isNullOrEmpty()) {
            _homeState.value = StudentHomeUiState.Error("Token tidak ditemukan. Silakan login kembali.")
            return
        }
        if (_homeState.value !is StudentHomeUiState.Success) {
            _homeState.value = StudentHomeUiState.Loading
        }

        ApiClient.apiService.getStudentDashboard("Bearer $token")
            .enqueue(object : Callback<StudentDashboardResponse> {
                override fun onResponse(
                    call: Call<StudentDashboardResponse>,
                    response: Response<StudentDashboardResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _homeState.value = StudentHomeUiState.Success(response.body()!!.data)
                    } else {
                        _homeState.value = StudentHomeUiState.Error("Gagal memuat dashboard: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StudentDashboardResponse>, t: Throwable) {
                    _homeState.value = StudentHomeUiState.Error("Koneksi internet bermasalah: ${t.localizedMessage}")
                }
            })
    }
}
