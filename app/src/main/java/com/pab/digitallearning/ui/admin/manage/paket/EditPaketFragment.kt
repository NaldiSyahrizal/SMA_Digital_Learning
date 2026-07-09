package com.pab.digitallearning.ui.admin.manage.paket

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.databinding.FragmentEditPaketBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPaketFragment : Fragment() {

    private var _binding: FragmentEditPaketBinding? = null
    private val binding get() = _binding!!
    private var packageId: Long = 0
    private var isEditMode = false
    private var selectedTingkatanId: Long? = null
    private var tingkatans: List<com.pab.digitallearning.data.model.Tingkatan> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPaketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialData()
        setupUI()
    }

    private fun setupInitialData() {
        packageId = arguments?.getLong("id") ?: 0
        binding.etNamaPaket.setText(arguments?.getString("nama"))
        binding.actvJurusan.setText(arguments?.getString("jurusan"))
        
        val initialTingkatanId = arguments?.getLong("tingkatan_id")
        selectedTingkatanId = if (initialTingkatanId != 0L) initialTingkatanId else null
        binding.actvTingkatan.setText(arguments?.getString("tingkatan_name"))
        
        binding.etDeskripsi.setText(arguments?.getString("deskripsi"))
        
        setupJurusanSpinner()
        fetchTingkatans()
    }

    private fun setupUI() {
        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        
        binding.btnActionToggle.setGemoyClick {
            if (!isEditMode) {
                enableEditMode(true)
            } else {
                updatePackage()
            }
        }
    }

    private fun enableEditMode(enable: Boolean) {
        isEditMode = enable
        
        // Update Button UI
        if (enable) {
            binding.btnActionToggle.text = "Simpan Perubahan"
            binding.btnActionToggle.setIconResource(android.R.drawable.ic_menu_save)
            binding.tvEditInstruction.text = "*Silakan ubah data pada kolom di bawah ini"
        } else {
            binding.btnActionToggle.text = "Edit Data"
            binding.btnActionToggle.setIconResource(android.R.drawable.ic_menu_edit)
            binding.tvEditInstruction.text = "*Klik tombol Edit di atas untuk mengubah data"
        }

        // Update Text Colors (Pudar vs Pekat)
        val textColor = if (enable) Color.parseColor("#102B5E") else Color.parseColor("#9E9E9E")
        binding.etNamaPaket.setTextColor(textColor)
        binding.actvTingkatan.setTextColor(textColor)
        binding.actvJurusan.setTextColor(textColor)
        binding.etDeskripsi.setTextColor(textColor)

        // Enable/Disable Fields
        binding.etNamaPaket.isEnabled = enable
        binding.actvTingkatan.isEnabled = enable
        binding.actvJurusan.isEnabled = enable
        binding.etDeskripsi.isEnabled = enable

        if (enable) {
            binding.etNamaPaket.requestFocus()
        }
    }

    private fun setupJurusanSpinner() {
        val jurusans = arrayOf("IPA", "IPS", "Bahasa", "TKJ", "RPL", "Multimedia")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jurusans)
        binding.actvJurusan.setAdapter(adapter)
    }

    private fun fetchTingkatans() {
        ApiClient.apiService.getTingkatans().enqueue(object : Callback<List<com.pab.digitallearning.data.model.Tingkatan>> {
            override fun onResponse(
                call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>,
                response: Response<List<com.pab.digitallearning.data.model.Tingkatan>>
            ) {
                if (_binding != null) {
                    if (response.isSuccessful && response.body() != null) {
                        tingkatans = response.body()!!
                        val names = tingkatans.map { it.namaTingkatan ?: "" }.toTypedArray()
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                        binding.actvTingkatan.setAdapter(adapter)
                        
                        binding.actvTingkatan.setOnItemClickListener { _, _, position, _ ->
                            selectedTingkatanId = tingkatans[position].id
                            
                            // Auto-prefix Nama Paket based on selected Tingkatan
                            val selectedName = tingkatans[position].namaTingkatan ?: ""
                            val newPrefix = "$selectedName - "
                            var currentName = binding.etNamaPaket.text.toString()
                            
                            // Remove any old prefix if user changes selection
                            tingkatans.forEach {
                                val oldPrefix = "${it.namaTingkatan} - "
                                if (currentName.startsWith(oldPrefix)) {
                                    currentName = currentName.removePrefix(oldPrefix)
                                }
                            }
                            
                            binding.etNamaPaket.setText("$newPrefix$currentName")
                            binding.etNamaPaket.setSelection(binding.etNamaPaket.text?.length ?: 0)
                            binding.etNamaPaket.requestFocus()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal load data tingkatan", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>, t: Throwable) {
                // Ignore on error silently for edit pre-load, could show toast
            }
        })
    }

    private fun updatePackage() {
        val nama = binding.etNamaPaket.text.toString().trim()
        val jurusan = binding.actvJurusan.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()

        if (nama.isEmpty() || jurusan.isEmpty() || selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap isi semua bidang dan pilih tingkatan")
            return
        }

        showLoading(true)
        ApiClient.apiService.updatePackage(packageId, nama, jurusan, selectedTingkatanId!!, deskripsi).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Paket berhasil diupdate") {
                            findNavController().popBackStack()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal update paket")
                    }
                }
            }

            override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                if (_binding != null) {
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
            val avd = binding.ivLoadingSpinner.drawable as? android.graphics.drawable.AnimatedVectorDrawable
            avd?.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
