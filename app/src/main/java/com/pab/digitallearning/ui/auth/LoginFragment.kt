package com.pab.digitallearning.ui.auth

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.pab.digitallearning.util.DialogUtils
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.LoginResponse
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.ResetTokenResponse
import com.pab.digitallearning.databinding.FragmentLoginBinding
import com.pab.digitallearning.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load and start entrance animations
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        val fadeInLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_left)
        binding.loginCard.startAnimation(slideUp)
        binding.headerTextContainer.startAnimation(fadeInLeft)

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                showLoading(true)
                login(email, password)
            } else {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Lengkapi email dan password!")
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
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

        var currentEmail = ""
        var resetToken = ""

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
            val email = etForgotEmail.text.toString().trim()

            if (email.isEmpty()) {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Mohon masukkan alamat email!")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Format email tidak valid!")
                return@setOnClickListener
            }

            showDialogLoading(true)
            ApiClient.apiService.forgotPasswordRequest(email).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    showDialogLoading(false)
                    if (response.isSuccessful) {
                        currentEmail = email
                        layoutStepRequest.visibility = View.GONE
                        layoutStepVerify.visibility = View.VISIBLE
                        tvForgotSubtitle.text = "Masukkan 6-digit kode OTP yang telah dikirim ke email $currentEmail."
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
            ApiClient.apiService.forgotPasswordVerify(currentEmail, otp).enqueue(object : Callback<ResetTokenResponse> {
                override fun onResponse(call: Call<ResetTokenResponse>, response: Response<ResetTokenResponse>) {
                    showDialogLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        resetToken = response.body()!!.resetToken ?: ""
                        layoutStepVerify.visibility = View.GONE
                        layoutStepReset.visibility = View.VISIBLE
                        tvForgotSubtitle.text = "Masukkan kata sandi baru Anda untuk akun $currentEmail."
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
            ApiClient.apiService.forgotPasswordReset(currentEmail, resetToken, newPassword).enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    showDialogLoading(false)
                    if (response.isSuccessful) {
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

    private fun login(email: String, password: String) {
        ApiClient.apiService.login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val token = response.body()?.token ?: ""
                    var role = response.body()?.role ?: ""

                    // TODO: The actual teacher verification logic will be done by the backend (ambil dari tabel guru).
                    // For now, if the user typed 'guru' in email, we force the role to 'teacher' to simulate successful login.
                    if (email.contains("guru") && role.isEmpty()) {
                        role = "teacher"
                    }

                    if (role == "admin" || role == "teacher" || role == "guru" || role == "pengajar" || role == "student" || role == "siswa" || role == "murid" || role == "kepala_sekolah") {
                        // Save session
                        val sessionManager = com.pab.digitallearning.core.SessionManager(requireContext())
                        sessionManager.saveSession(token, role)

                        val intent = if (role == "admin") {
                            com.pab.digitallearning.util.AdminActivityTracker.logActivity(requireContext(), "Login ke Dashboard Admin", "security")
                            android.content.Intent(requireContext(), com.pab.digitallearning.AdminDashboardActivity::class.java)
                        } else if (role == "kepala_sekolah") {
                            android.content.Intent(requireContext(), com.pab.digitallearning.ui.principal.PrincipalDashboardActivity::class.java)
                        } else if (role == "teacher" || role == "guru" || role == "pengajar") {
                            android.content.Intent(requireContext(), com.pab.digitallearning.ui.teacher.TeacherDashboardActivity::class.java)
                        } else {
                            android.content.Intent(requireContext(), com.pab.digitallearning.ui.student.StudentDashboardActivity::class.java)
                        }
                        
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Role '$role' belum didukung.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("LoginError", "Code: ${response.code()}, Body: $errorBody")
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Login gagal: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                android.util.Log.e("LoginError", "Network error: ${t.message}")
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Gagal terhubung ke server")
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            // Jalankan animasi spinner
            val drawable = binding.ivLoadingSpinner.drawable
            if (drawable is AnimatedVectorDrawable) {
                drawable.start()
            }
        } else {
            binding.loadingOverlay.visibility = View.GONE
            binding.btnLogin.isEnabled = true
            // Hentikan animasi spinner
            val drawable = binding.ivLoadingSpinner.drawable
            if (drawable is AnimatedVectorDrawable) {
                drawable.stop()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
