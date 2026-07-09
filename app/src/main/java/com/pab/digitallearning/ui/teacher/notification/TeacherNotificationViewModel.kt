package com.pab.digitallearning.ui.teacher.notification

import androidx.lifecycle.ViewModel
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeacherNotificationViewModel : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val notificationsState: StateFlow<NotificationUiState> = _notificationsState.asStateFlow()

    // 1. Fetch all notifications
    fun fetchNotifications(token: String?) {
        if (token.isNullOrEmpty()) {
            _notificationsState.value = NotificationUiState.Error("Token tidak valid.")
            return
        }
        _notificationsState.value = NotificationUiState.Loading

        ApiClient.apiService.getNotifications("Bearer $token")
            .enqueue(object : Callback<NotificationListResponse> {
                override fun onResponse(
                    call: Call<NotificationListResponse>,
                    response: Response<NotificationListResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _notificationsState.value = NotificationUiState.Success(response.body()!!.data)
                    } else {
                        _notificationsState.value = NotificationUiState.Error("Gagal memuat notifikasi: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<NotificationListResponse>, t: Throwable) {
                    _notificationsState.value = NotificationUiState.Error("Koneksi gagal: ${t.message}")
                }
            })
    }

    // 2. Mark specific notification as read
    fun markAsRead(token: String?, id: Long) {
        if (token.isNullOrEmpty()) return

        ApiClient.apiService.markNotificationRead("Bearer $token", id)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        // Silent update state
                        val currentState = _notificationsState.value
                        if (currentState is NotificationUiState.Success) {
                            val updatedList = currentState.notifications.map {
                                if (it.id == id) it.copy(isRead = true) else it
                            }
                            _notificationsState.value = NotificationUiState.Success(updatedList)
                        }
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    // Ignore
                }
            })
    }

    // 3. Mark all notifications as read
    fun markAllAsRead(token: String?) {
        if (token.isNullOrEmpty()) return

        ApiClient.apiService.markAllNotificationsRead("Bearer $token")
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        val currentState = _notificationsState.value
                        if (currentState is NotificationUiState.Success) {
                            val updatedList = currentState.notifications.map {
                                it.copy(isRead = true)
                            }
                            _notificationsState.value = NotificationUiState.Success(updatedList)
                        }
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    // Ignore
                }
            })
    }

    // 4. Simulate a student submission (Very useful for demos and testing!)
    fun simulateSubmission(token: String?, contentId: Long, studentId: Long, onResult: (Boolean, String) -> Unit) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        ApiClient.apiService.simulateStudentSubmission("Bearer $token", contentId, studentId, null)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        onResult(body.status == "success", body.message)
                        if (body.status == "success") {
                            // Fetch fresh notifications
                            fetchNotifications(token)
                        }
                    } else {
                        onResult(false, "Gagal memicu simulasi: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    // 5. Delete a specific notification
    fun deleteNotification(token: String?, id: Long, onResult: (Boolean, String) -> Unit) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }

        ApiClient.apiService.deleteNotification("Bearer $token", id)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        // Silent update state
                        val currentState = _notificationsState.value
                        if (currentState is NotificationUiState.Success) {
                            val updatedList = currentState.notifications.filter { it.id != id }
                            _notificationsState.value = NotificationUiState.Success(updatedList)
                        }
                        onResult(true, "Notifikasi berhasil dihapus.")
                    } else {
                        onResult(false, "Gagal menghapus notifikasi: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }

    // 6. Delete multiple selected notifications
    fun deleteMultipleNotifications(token: String?, ids: List<Long>, onResult: (Boolean, String) -> Unit) {
        if (token.isNullOrEmpty()) {
            onResult(false, "Token tidak valid.")
            return
        }
        if (ids.isEmpty()) {
            onResult(false, "Tidak ada notifikasi terpilih.")
            return
        }

        ApiClient.apiService.deleteMultipleNotifications("Bearer $token", ids)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        // Silent update state
                        val currentState = _notificationsState.value
                        if (currentState is NotificationUiState.Success) {
                            val updatedList = currentState.notifications.filter { !ids.contains(it.id) }
                            _notificationsState.value = NotificationUiState.Success(updatedList)
                        }
                        onResult(true, "Notifikasi terpilih berhasil dihapus.")
                    } else {
                        onResult(false, "Gagal menghapus notifikasi terpilih: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    onResult(false, "Koneksi gagal: ${t.message}")
                }
            })
    }
}

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val notifications: List<Notification>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}
