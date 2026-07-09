package com.pab.digitallearning.ui.admin.manage.student

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.databinding.FragmentTambahSiswaBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddSiswaFragment : Fragment() {

    private var _binding: FragmentTambahSiswaBinding? = null
    private val binding get() = _binding!!
    
    private val genderOptions = listOf("Laki-laki", "Perempuan")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahSiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()
        setupListeners()
    }
    
    private fun setupDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.actvJenisKelamin.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.btnSimpan.setGemoyClick { saveStudent() }
    }

    private fun saveStudent() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val nama = binding.etNama.text.toString().trim()
        val nis = binding.etNis.text.toString().trim()
        val jkText = binding.actvJenisKelamin.text.toString()
        val telp = binding.etNoTelp.text.toString().trim()
        val password = binding.etPassword.text.toString()

        val jk = if (jkText == "Laki-laki") "L" else if (jkText == "Perempuan") "P" else ""

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || nis.isEmpty() || nama.isEmpty() || jk.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Mohon lengkapi semua kolom")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Format email tidak valid!")
            return
        }

        binding.btnSimpan.isEnabled = false
        showLoading(true)
        ApiClient.apiService.addStudent(username, email, nama, nis, jk, telp, password).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding == null) return
                binding.btnSimpan.isEnabled = true
                showLoading(false)
                if (response.isSuccessful) {
                    com.pab.digitallearning.util.AdminActivityTracker.logActivity(requireContext(), "Menambahkan siswa baru: $nama", "add")
                    DialogUtils.showSuccessDialog(requireContext(), message = "Siswa berhasil ditambahkan") {
                        findNavController().popBackStack()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menambah siswa")
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                if (_binding != null) {
                    binding.btnSimpan.isEnabled = true
                    showLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            }
        })
    }

    private fun showLoading(show: Boolean) {
        if (_binding == null) return
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            val spinner = binding.ivLoadingSpinner
            val avd = spinner.drawable as? android.graphics.drawable.AnimatedVectorDrawable
            avd?.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
