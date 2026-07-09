package com.pab.digitallearning.ui.admin.manage.subject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.Package
import com.pab.digitallearning.databinding.FragmentTambahMapelBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMapelFragment : Fragment() {

    private var _binding: FragmentTambahMapelBinding? = null
    private val binding get() = _binding!!
    
    private var allPackages = listOf<Package>()
    private var filteredPackages = listOf<Package>()
    private var selectedPackageIds = mutableListOf<Long>()
    
    private var tingkatans: List<com.pab.digitallearning.data.model.Tingkatan> = emptyList()
    private var selectedTingkatanId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahMapelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        fetchTingkatans()
        fetchPackages()
    }

    private fun setupUI() {
        val kategoris = arrayOf("Umum", "Pilihan")
        val adapterKategori = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kategoris)
        binding.actvKategori.setAdapter(adapterKategori)

        binding.actvPaketJurusan.setOnClickListener { showPackageBottomSheet() }
        binding.actvPaketJurusan.keyListener = null 

        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.btnSimpan.setGemoyClick { saveSubject() }
    }

    private fun fetchPackages() {
        ApiClient.apiService.getPackages().enqueue(object : Callback<List<Package>> {
            override fun onResponse(call: Call<List<Package>>, response: Response<List<Package>>) {
                if (_binding != null && response.isSuccessful) {
                    allPackages = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<Package>>, t: Throwable) {}
        })
    }
    
    private fun fetchTingkatans() {
        ApiClient.apiService.getTingkatans().enqueue(object : Callback<List<com.pab.digitallearning.data.model.Tingkatan>> {
            override fun onResponse(
                call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>,
                response: Response<List<com.pab.digitallearning.data.model.Tingkatan>>
            ) {
                if (_binding != null && response.isSuccessful && response.body() != null) {
                    tingkatans = response.body()!!
                    val names = tingkatans.map { it.namaTingkatan ?: "" }.toTypedArray()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
                    binding.actvTingkatan.setAdapter(adapter)
                    
                    binding.actvTingkatan.setOnItemClickListener { _, _, position, _ ->
                        val newTingkatanId = tingkatans[position].id
                        if (selectedTingkatanId != newTingkatanId) {
                            selectedTingkatanId = newTingkatanId
                            // Filter packages and reset selection
                            filteredPackages = allPackages.filter { it.tingkatanId == selectedTingkatanId }
                            selectedPackageIds.clear()
                            binding.actvPaketJurusan.setText("")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>, t: Throwable) {}
        })
    }

    private fun showPackageBottomSheet() {
        if (selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Silakan pilih Tingkatan terlebih dahulu")
            return
        }

        if (allPackages.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Memuat data paket...")
            fetchPackages()
            return
        }
        
        if (filteredPackages.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Belum ada paket untuk tingkatan ini")
            return
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_select_package_bottom_sheet, null)
        
        val rv = view.findViewById<RecyclerView>(R.id.rvSelectPackages)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmSelection)
        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearchPackage)
        
        val adapter = PackageSelectAdapter(filteredPackages, selectedPackageIds)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        btnConfirm.setGemoyClick {
            selectedPackageIds = adapter.getSelectedIds().toMutableList()
            // Map IDs back to names for display in the UI
            val selectedNames = allPackages.filter { it.id in selectedPackageIds }.map { it.namaPaket ?: "" }
            binding.actvPaketJurusan.setText(selectedNames.joinToString(", "))
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun saveSubject() {
        val nama = binding.etNamaMapel.text.toString().trim()
        val kode = binding.etKodeMapel.text.toString().trim()
        val kategoriRaw = binding.actvKategori.text.toString().trim()
        val packages = binding.actvPaketJurusan.text.toString().trim()

        val jamPelajaranRaw = binding.etJamPelajaran.text.toString().trim()
        val jamPelajaran = jamPelajaranRaw.toIntOrNull() ?: 3

        if (nama.isEmpty() || kode.isEmpty() || kategoriRaw.isEmpty() || selectedPackageIds.isEmpty() || selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap isi nama, kode, tingkatan, kategori, dan paket")
            return
        }

        // PAKSA JADI HURUF KECIL biar lolos validasi Laravel
        val kategori = kategoriRaw.lowercase()

        showLoading(true)
        Log.d("DEBUG_MAPEL", "Kirim -> Nama: $nama, Kode: $kode, Kategori: $kategori, Paket IDs: $selectedPackageIds, JP: $jamPelajaran")

        ApiClient.apiService.addSubject(nama, kode, kategori, selectedTingkatanId!!, selectedPackageIds, jamPelajaran).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Mapel berhasil ditambahkan") {
                            findNavController().popBackStack()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menambahkan mapel")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
