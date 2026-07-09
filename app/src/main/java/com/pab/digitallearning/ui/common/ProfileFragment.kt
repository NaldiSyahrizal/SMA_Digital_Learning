package com.pab.digitallearning.ui.common

import android.graphics.drawable.AnimatedVectorDrawable
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.core.MainActivity
import com.pab.digitallearning.core.SessionManager
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.ResetTokenResponse
import com.pab.digitallearning.data.model.UserResponse
import com.pab.digitallearning.databinding.FragmentProfileBinding
import com.pab.digitallearning.util.DialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var adminEmail: String = ""
    private var adminUsername: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getToken()

        // Fetch User profile info
        if (token != null) {
            fetchUserProfile(token)
        }

        binding.btnChangePassword.setOnClickListener {
            if (adminEmail.isNotEmpty()) {
                showChangePasswordDialog()
            } else {
                Toast.makeText(requireContext(), "Data profil belum dimuat. Mohon tunggu.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            // Hapus sesi login
            sessionManager.clearSession()

            // Navigate back to Login (MainActivity)
            val intent = Intent(requireContext(), MainActivity::class.java)
            // Clear the activity back stack so user cannot press back to return to the dashboard
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun fetchUserProfile(token: String) {
        ApiClient.apiService.getLoggedUser("Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (_binding != null && response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    adminEmail = user.email
                    adminUsername = user.username

                    binding.tvUsernameValue.text = adminUsername
                    binding.tvEmailValue.text = adminEmail
                    binding.tvName.text = adminUsername.replaceFirstChar { it.uppercase() }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                if (_binding != null) {
                    Log.e("ProfileFragment", "Gagal mengambil profil: ${t.message}")
                }
            }
        })
    }

    private fun showChangePasswordDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        dialog.setContentView(view)

        val tvForgotSubtitle = view.findViewById<TextView>(R.id.tvForgotSubtitle)

        val layoutStepRequest = view.findViewById<LinearLayout>(R.id.layoutStepRequest)
        val layoutStepVerify = view.findViewById<LinearLayout>(R.id.layoutStepVerify)
        val layoutStepReset = view.findViewById<LinearLayout>(R.id.layoutStepReset)

        val etForgotEmail = view.findViewById<EditText>(R.id.etForgotEmail)
        val btnRequestOtp = view.findViewById<Button>(R.id.btnRequestOtp)

        val etForgotOtp = view.findViewById<EditText>(R.id.etForgotOtp)
        val btnVerifyOtp = view.findViewById<Button>(R.id.btnVerifyOtp)

        val etForgotNewPassword = view.findViewById<EditText>(R.id.etForgotNewPassword)
        val etForgotConfirmPassword = view.findViewById<EditText>(R.id.etForgotConfirmPassword)
        val btnResetPassword = view.findViewById<Button>(R.id.btnResetPassword)

        val dialogLoadingOverlay = view.findViewById<FrameLayout>(R.id.dialogLoadingOverlay)
        val ivDialogLoadingSpinner = view.findViewById<ImageView>(R.id.ivDialogLoadingSpinner)

        var resetToken = ""

        // Prefill email and disable manual editing for security
        etForgotEmail.setText(adminEmail)
        etForgotEmail.isEnabled = false

        fun showDialogLoading(isLoading: Boolean) {
            if (isLoading) {
                dialogLoadingOverlay.visibility = View.VISIBLE
                btnRequestOtp.isEnabled = false
                btnVerifyOtp.isEnabled = false
                btnResetPassword.isEnabled = false
                val drawable = ivDialogLoadingSpinner.drawable
                if (drawable is AnimatedVectorDrawable) {
                    drawable.start()
                }
            } else {
                dialogLoadingOverlay.visibility = View.GONE
                btnRequestOtp.isEnabled = true
                btnVerifyOtp.isEnabled = true
                btnResetPassword.isEnabled = true
                val drawable = ivDialogLoadingSpinner.drawable
                if (drawable is AnimatedVectorDrawable) {
                    drawable.stop()
                }
            }
        }

        // STEP 1: REQUEST OTP
        btnRequestOtp.setOnClickListener {
            showDialogLoading(true)
            ApiClient.apiService.forgotPasswordRequest(adminEmail).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    showDialogLoading(false)
                    if (response.isSuccessful) {
                        layoutStepRequest.visibility = View.GONE
                        layoutStepVerify.visibility = View.VISIBLE
                        tvForgotSubtitle.text = "Masukkan 6-digit kode OTP yang telah dikirim ke email $adminEmail."
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal mengirim kode OTP")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    showDialogLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            })
        }

        // STEP 2: VERIFY OTP
        btnVerifyOtp.setOnClickListener {
            val otp = etForgotOtp.text.toString().trim()

            if (otp.isEmpty() || otp.length != 6) {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Masukkan 6 digit kode OTP!")
                return@setOnClickListener
            }

            showDialogLoading(true)
            ApiClient.apiService.forgotPasswordVerify(adminEmail, otp).enqueue(object : Callback<ResetTokenResponse> {
                override fun onResponse(call: Call<ResetTokenResponse>, response: Response<ResetTokenResponse>) {
                    showDialogLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        resetToken = response.body()!!.resetToken ?: ""
                        layoutStepVerify.visibility = View.GONE
                        layoutStepReset.visibility = View.VISIBLE
                        tvForgotSubtitle.text = "Masukkan kata sandi baru Anda untuk akun $adminEmail."
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Kode OTP tidak valid atau kadaluwarsa")
                    }
                }

                override fun onFailure(call: Call<ResetTokenResponse>, t: Throwable) {
                    showDialogLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            })
        }

        // STEP 3: RESET PASSWORD
        btnResetPassword.setOnClickListener {
            val newPassword = etForgotNewPassword.text.toString().trim()
            val confirmPassword = etForgotConfirmPassword.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Mohon lengkapi semua kolom!")
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Password minimal 6 karakter!")
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Konfirmasi password tidak cocok!")
                return@setOnClickListener
            }

            showDialogLoading(true)
            ApiClient.apiService.forgotPasswordReset(adminEmail, resetToken, newPassword).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    showDialogLoading(false)
                    if (response.isSuccessful) {
                        com.pab.digitallearning.util.AdminActivityTracker.logActivity(requireContext(), "Mengubah kata sandi akun admin", "security")
                        dialog.dismiss()
                        DialogUtils.showSuccessDialog(requireContext(), message = "Kata sandi berhasil diubah!")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal mereset kata sandi")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    showDialogLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            })
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
