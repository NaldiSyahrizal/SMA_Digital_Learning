package com.pab.digitallearning.ui.student.subject

import androidx.lifecycle.ViewModel
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed interface StudentSubjectsUiState {
    object Loading : StudentSubjectsUiState
    data class Success(val data: List<StudentSubject>) : StudentSubjectsUiState
    data class Error(val message: String) : StudentSubjectsUiState
}

sealed interface StudentContentsUiState {
    object Loading : StudentContentsUiState
    data class Success(val data: List<StudentContentItem>) : StudentContentsUiState
    data class Error(val message: String) : StudentContentsUiState
}

class StudentSubjectViewModel : ViewModel() {

    private val _subjectsState = MutableStateFlow<StudentSubjectsUiState>(StudentSubjectsUiState.Loading)
    val subjectsState: StateFlow<StudentSubjectsUiState> = _subjectsState.asStateFlow()

    private val _contentsState = MutableStateFlow<StudentContentsUiState>(StudentContentsUiState.Loading)
    val contentsState: StateFlow<StudentContentsUiState> = _contentsState.asStateFlow()

    fun fetchSubjects(token: String?) {
        if (token.isNullOrEmpty()) {
            _subjectsState.value = StudentSubjectsUiState.Error("Token tidak valid.")
            return
        }
        if (_subjectsState.value !is StudentSubjectsUiState.Success) {
            _subjectsState.value = StudentSubjectsUiState.Loading
        }

        ApiClient.apiService.getStudentSubjects("Bearer $token")
            .enqueue(object : Callback<StudentSubjectResponse> {
                override fun onResponse(
                    call: Call<StudentSubjectResponse>,
                    response: Response<StudentSubjectResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _subjectsState.value = StudentSubjectsUiState.Success(response.body()!!.data)
                    } else {
                        _subjectsState.value = StudentSubjectsUiState.Error("Gagal memuat mata pelajaran: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StudentSubjectResponse>, t: Throwable) {
                    _subjectsState.value = StudentSubjectsUiState.Error("Koneksi bermasalah: ${t.localizedMessage}")
                }
            })
    }

    fun updateInterest(token: String?, subjectId: Long, interestScore: Int, onComplete: () -> Unit = {}) {
        if (token.isNullOrEmpty()) return

        ApiClient.apiService.saveStudentSubjectInterest("Bearer $token", subjectId, interestScore)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        fetchSubjects(token) // Refresh lists
                        onComplete()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    // Fail silently or handle
                }
            })
    }

    fun fetchContents(token: String?, subjectId: Long) {
        if (token.isNullOrEmpty()) {
            _contentsState.value = StudentContentsUiState.Error("Token tidak valid.")
            return
        }
        if (_contentsState.value !is StudentContentsUiState.Success) {
            _contentsState.value = StudentContentsUiState.Loading
        }

        ApiClient.apiService.getStudentContents("Bearer $token", subjectId)
            .enqueue(object : Callback<StudentContentsResponse> {
                override fun onResponse(
                    call: Call<StudentContentsResponse>,
                    response: Response<StudentContentsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _contentsState.value = StudentContentsUiState.Success(response.body()!!.data)
                    } else {
                        _contentsState.value = StudentContentsUiState.Error("Gagal memuat konten pelajaran: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StudentContentsResponse>, t: Throwable) {
                    _contentsState.value = StudentContentsUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }
}
