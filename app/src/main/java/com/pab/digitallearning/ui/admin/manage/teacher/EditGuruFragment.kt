package com.pab.digitallearning.ui.admin.manage.teacher

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.databinding.FragmentEditGuruBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditGuruFragment : Fragment() {

    private var _binding: FragmentEditGuruBinding? = null
    private val binding get() = _binding!!
    
    private var teacherId: Long = 0
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialData()
        setupUI()
    }

    private fun setupInitialData() {
        teacherId = arguments?.getLong("id") ?: 0
        binding.etNama.setText(arguments?.getString("nama"))
        binding.etNip.setText(arguments?.getString("nip"))
        binding.etNoTelp.setText(arguments?.getString("telp"))
        binding.etUsername.setText(arguments?.getString("username"))
        binding.etEmail.setText(arguments?.getString("email"))
        
        val gender = arguments?.getString("jk")
        val fullGender = if (gender == "L") "Laki-Laki" else if (gender == "P") "Perempuan" else gender
        binding.actvJenisKelamin.setText(fullGender, false)
        
        setupGenderDropdown()
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Laki-Laki", "Perempuan")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.actvJenisKelamin.setAdapter(adapter)
    }

    private fun setupUI() {
        binding.btnBack.setGemoyClick { findNavController().popBackStack() }

        // Setup UI

        binding.btnActionToggle.setGemoyClick {
            if (!isEditMode) {
                enableEditMode(true)
            } else {
                updateTeacher()
            }
        }
        
        // Ensure default state is read-only
        enableEditMode(false)
    }

    private fun enableEditMode(enable: Boolean) {
        isEditMode = enable
        
        // Update Button UI
        if (enable) {
            binding.btnActionToggle.text = "Simpan Perubahan"
            binding.tvEditInstruction.text = "*Silakan ubah data pada kolom di atas"
        } else {
            binding.btnActionToggle.text = "Edit Data"
            binding.tvEditInstruction.text = "*Klik tombol Edit di atas untuk mengubah data"
        }

        // Update Text Colors
        val textColor = if (enable) Color.parseColor("#102B5E") else Color.parseColor("#9E9E9E")
        binding.etNama.setTextColor(textColor)
        binding.etNip.setTextColor(textColor)
        binding.actvJenisKelamin.setTextColor(textColor)
        binding.etNoTelp.setTextColor(textColor)
        binding.etUsername.setTextColor(textColor)
        binding.etEmail.setTextColor(textColor)
        binding.etPassword.setTextColor(textColor)

        // Enable/Disable Fields
        binding.etNama.isEnabled = enable
        binding.etNip.isEnabled = enable
        binding.actvJenisKelamin.isEnabled = enable
        binding.etNoTelp.isEnabled = enable
        binding.etUsername.isEnabled = enable
        binding.etEmail.isEnabled = enable
        binding.etPassword.isEnabled = enable

        if (enable) {
            binding.etNama.requestFocus()
        }
    }

    private fun updateTeacher() {
        val nama = binding.etNama.text.toString().trim()
        val nip = binding.etNip.text.toString().trim()
        val fullGender = binding.actvJenisKelamin.text.toString().trim()
        val telp = binding.etNoTelp.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (nama.isEmpty() || nip.isEmpty() || fullGender.isEmpty() || username.isEmpty() || email.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap isi bidang wajib")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Format email tidak valid!")
            return
        }

        val genderCode = if (fullGender == "Laki-Laki") "L" else "P"
        val passwordParam = if (password.isNotEmpty()) password else null

        binding.btnActionToggle.isEnabled = false
        ApiClient.apiService.updateTeacher(teacherId, username, email, nama, nip, genderCode, telp, passwordParam).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding == null) return
                binding.btnActionToggle.isEnabled = true
                if (response.isSuccessful) {
                    com.pab.digitallearning.util.AdminActivityTracker.logActivity(requireContext(), "Mengubah data guru: $nama", "edit")
                    DialogUtils.showSuccessDialog(requireContext(), message = "Guru berhasil diupdate") {
                        findNavController().popBackStack()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal update guru")
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                if (_binding != null) {
                    binding.btnActionToggle.isEnabled = true
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
