package com.pab.digitallearning.ui.student.notification

import androidx.lifecycle.ViewModel
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.StudentNotification
import com.pab.digitallearning.data.model.StudentNotificationListResponse
import com.pab.digitallearning.util.StudentNotificationPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed interface StudentNotificationUiState {
    object Loading : StudentNotificationUiState
    data class Success(val data: List<StudentNotification>) : StudentNotificationUiState
    data class Error(val message: String) : StudentNotificationUiState
}

class StudentNotificationViewModel : ViewModel() {

    private val _notificationsState = MutableStateFlow<StudentNotificationUiState>(StudentNotificationUiState.Loading)
    val notificationsState: StateFlow<StudentNotificationUiState> = _notificationsState.asStateFlow()

    fun fetchNotifications(token: String?, prefs: StudentNotificationPrefs? = null) {
        if (token.isNullOrEmpty()) {
            _notificationsState.value = StudentNotificationUiState.Error("Token tidak ditemukan.")
            return
        }
        if (_notificationsState.value !is StudentNotificationUiState.Success) {
            _notificationsState.value = StudentNotificationUiState.Loading
        }

        ApiClient.apiService.getStudentNotifications("Bearer $token")
            .enqueue(object : Callback<StudentNotificationListResponse> {
                override fun onResponse(
                    call: Call<StudentNotificationListResponse>,
                    response: Response<StudentNotificationListResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val originalList = response.body()!!.data
                        val processedList = if (prefs != null) {
                            val readIds = prefs.getReadIds()
                            val deletedIds = prefs.getDeletedIds()
                            originalList
                                .filter { notif -> !deletedIds.contains(notif.id.toString()) }
                                .map { notif ->
                                    if (readIds.contains(notif.id.toString())) {
                                        notif.copy(isRead = true)
                                    } else {
                                        notif
                                    }
                                }
                        } else {
                            originalList
                        }

                        _notificationsState.value = StudentNotificationUiState.Success(processedList)
                    } else {
                        _notificationsState.value = StudentNotificationUiState.Error("Gagal memuat notifikasi: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<StudentNotificationListResponse>, t: Throwable) {
                    _notificationsState.value = StudentNotificationUiState.Error("Koneksi gagal: ${t.localizedMessage}")
                }
            })
    }

    fun markAsRead(id: Long, prefs: StudentNotificationPrefs) {
        prefs.markAsRead(id.toString())
        val currentState = _notificationsState.value
        if (currentState is StudentNotificationUiState.Success) {
            val updated = currentState.data.map {
                if (it.id == id) it.copy(isRead = true) else it
            }
            _notificationsState.value = StudentNotificationUiState.Success(updated)
        }
    }

    fun markAllAsRead(prefs: StudentNotificationPrefs) {
        val currentState = _notificationsState.value
        if (currentState is StudentNotificationUiState.Success) {
            val unreadIds = currentState.data.filter { !it.isRead }.map { it.id.toString() }
            if (unreadIds.isNotEmpty()) {
                prefs.markMultipleAsRead(unreadIds)
            }
            val updated = currentState.data.map { it.copy(isRead = true) }
            _notificationsState.value = StudentNotificationUiState.Success(updated)
        }
    }

    fun deleteNotification(id: Long, prefs: StudentNotificationPrefs, onResult: (Boolean, String) -> Unit) {
        prefs.deleteNotification(id.toString())
        val currentState = _notificationsState.value
        if (currentState is StudentNotificationUiState.Success) {
            val updated = currentState.data.filter { it.id != id }
            _notificationsState.value = StudentNotificationUiState.Success(updated)
        }
        onResult(true, "Notifikasi berhasil dihapus.")
    }

    fun deleteMultipleNotifications(ids: List<Long>, prefs: StudentNotificationPrefs, onResult: (Boolean, String) -> Unit) {
        if (ids.isEmpty()) {
            onResult(false, "Tidak ada notifikasi terpilih.")
            return
        }
        prefs.deleteMultipleNotifications(ids.map { it.toString() })
        val currentState = _notificationsState.value
        if (currentState is StudentNotificationUiState.Success) {
            val updated = currentState.data.filter { !ids.contains(it.id) }
            _notificationsState.value = StudentNotificationUiState.Success(updated)
        }
        onResult(true, "Notifikasi terpilih berhasil dihapus.")
    }
}
