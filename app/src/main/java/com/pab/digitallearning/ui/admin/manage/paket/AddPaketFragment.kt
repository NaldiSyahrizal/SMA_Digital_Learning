package com.pab.digitallearning.ui.admin.manage.paket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.databinding.FragmentTambahPaketBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPaketFragment : Fragment() {

    private var _binding: FragmentTambahPaketBinding? = null
    private val binding get() = _binding!!
    
    private var tingkatans: List<com.pab.digitallearning.data.model.Tingkatan> = emptyList()
    private var selectedTingkatanId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPaketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        setupJurusanSpinner()
        fetchTingkatans()
        
        // Efek Gemoy
        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.btnSimpan.setGemoyClick { savePackage() }
    }

    private fun setupJurusanSpinner() {
        val jurusans = arrayOf("IPA", "IPS", "Bahasa", "TKJ", "RPL", "Multimedia")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jurusans)
        binding.actvJurusan.setAdapter(adapter)
    }

    private fun fetchTingkatans() {
        showLoading(true)
        ApiClient.apiService.getTingkatans().enqueue(object : Callback<List<com.pab.digitallearning.data.model.Tingkatan>> {
            override fun onResponse(
                call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>,
                response: Response<List<com.pab.digitallearning.data.model.Tingkatan>>
            ) {
                if (_binding != null) {
                    showLoading(false)
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
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Gagal mengambil data tingkatan")
                    }
                }
            }

            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>, t: Throwable) {
                if (_binding != null) {
                    showLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            }
        })
    }

    private fun savePackage() {
        val nama = binding.etNamaPaket.text.toString().trim()
        val jurusan = binding.actvJurusan.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()

        if (nama.isEmpty() || jurusan.isEmpty() || selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap isi semua bidang dan pilih tingkatan")
            return
        }

        showLoading(true)
        ApiClient.apiService.addPackage(nama, jurusan, selectedTingkatanId!!, deskripsi).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Paket berhasil ditambahkan") {
                            findNavController().popBackStack()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menambahkan paket")
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
