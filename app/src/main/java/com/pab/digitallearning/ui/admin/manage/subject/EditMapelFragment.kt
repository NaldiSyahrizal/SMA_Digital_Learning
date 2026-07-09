package com.pab.digitallearning.ui.admin.manage.subject

import android.graphics.Color
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
import com.pab.digitallearning.data.model.Package
import com.pab.digitallearning.databinding.FragmentEditMapelBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditMapelFragment : Fragment() {

    private var _binding: FragmentEditMapelBinding? = null
    private val binding get() = _binding!!
    private var subjectId: Long = 0
    private var isEditMode = false
    
    private var allPackages = listOf<Package>()
    private var filteredPackages = listOf<Package>()
    private var selectedPackageIds = mutableListOf<Long>()
    
    private var tingkatans: List<com.pab.digitallearning.data.model.Tingkatan> = emptyList()
    private var selectedTingkatanId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMapelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialData()
        setupUI()
        fetchPackages()
    }

    private fun setupInitialData() {
        subjectId = arguments?.getLong("id") ?: 0
        binding.etNamaMapel.setText(arguments?.getString("nama"))
        binding.etKodeMapel.setText(arguments?.getString("kode"))
        
        val initialTingkatanId = arguments?.getLong("tingkatan_id")
        selectedTingkatanId = if (initialTingkatanId != 0L) initialTingkatanId else null
        binding.actvTingkatan.setText(arguments?.getString("tingkatan_name"))
        
        binding.actvKategori.setText(arguments?.getString("kategori")?.replaceFirstChar { it.uppercase() })
        
        val initialPackages = arguments?.getString("packages") ?: ""
        binding.actvPaketJurusan.setText(initialPackages)
        
        val initialIds = arguments?.getLongArray("package_ids")
        if (initialIds != null) {
            selectedPackageIds = initialIds.toMutableList()
        }
        
        val initialJamPelajaran = arguments?.getInt("jam_pelajaran") ?: 3
        binding.etJamPelajaran.setText(initialJamPelajaran.toString())
        
        val kategoris = arrayOf("Umum", "Pilihan")
        val adapterKategori = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kategoris)
        binding.actvKategori.setAdapter(adapterKategori)
        
        fetchTingkatans()
    }

    private fun setupUI() {
        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        
        binding.actvPaketJurusan.setOnClickListener {
            if (isEditMode) showPackageBottomSheet()
        }
        binding.actvPaketJurusan.keyListener = null 

        binding.btnActionToggle.setGemoyClick {
            if (!isEditMode) {
                enableEditMode(true)
            } else {
                updateSubject()
            }
        }
        
        enableEditMode(false)
    }

    private fun fetchPackages() {
        ApiClient.apiService.getPackages().enqueue(object : Callback<List<Package>> {
            override fun onResponse(call: Call<List<Package>>, response: Response<List<Package>>) {
                if (_binding != null && response.isSuccessful) {
                    allPackages = response.body() ?: emptyList()
                    if (selectedTingkatanId != null) {
                        filteredPackages = allPackages.filter { it.tingkatanId == selectedTingkatanId }
                    }
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
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Silakan pilih Tingkatan terlebih dahulu")
            return
        }
        
        if (allPackages.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Memuat data paket...")
            fetchPackages()
            return
        }
        
        if (filteredPackages.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Belum ada paket untuk tingkatan ini")
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
            val selectedNames = allPackages.filter { it.id in selectedPackageIds }.map { it.namaPaket ?: "" }
            binding.actvPaketJurusan.setText(selectedNames.joinToString(", "))
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun enableEditMode(enable: Boolean) {
        isEditMode = enable
        
        if (enable) {
            binding.btnActionToggle.text = "Simpan Perubahan"
            binding.btnActionToggle.setIconResource(android.R.drawable.ic_menu_save)
            binding.tvEditInstruction.text = "*Silakan ubah data pada kolom di bawah ini"
        } else {
            binding.btnActionToggle.text = "Edit Data"
            binding.btnActionToggle.setIconResource(android.R.drawable.ic_menu_edit)
            binding.tvEditInstruction.text = "*Klik tombol Edit di atas untuk mengubah data"
        }

        val textColor = if (enable) Color.parseColor("#102B5E") else Color.parseColor("#9E9E9E")
        binding.etNamaMapel.setTextColor(textColor)
        binding.etKodeMapel.setTextColor(textColor)
        binding.actvTingkatan.setTextColor(textColor)
        binding.actvKategori.setTextColor(textColor)
        binding.actvPaketJurusan.setTextColor(textColor)
        binding.etJamPelajaran.setTextColor(textColor)

        binding.etNamaMapel.isEnabled = enable
        binding.etKodeMapel.isEnabled = enable
        binding.actvTingkatan.isEnabled = enable
        binding.actvKategori.isEnabled = enable
        binding.actvPaketJurusan.isEnabled = enable
        binding.etJamPelajaran.isEnabled = enable
        
        if (enable) {
            binding.etNamaMapel.requestFocus()
        }
    }

    private fun updateSubject() {
        val nama = binding.etNamaMapel.text.toString().trim()
        val kode = binding.etKodeMapel.text.toString().trim()
        val kategoriRaw = binding.actvKategori.text.toString().trim()
        val packages = binding.actvPaketJurusan.text.toString().trim()

        if (nama.isEmpty() || kode.isEmpty() || kategoriRaw.isEmpty() || selectedPackageIds.isEmpty() || selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap lengkapi semua data dan pilih tingkatan")
            return
        }

        val kategori = kategoriRaw.lowercase()

        val jamPelajaranRaw = binding.etJamPelajaran.text.toString().trim()
        val jamPelajaran = jamPelajaranRaw.toIntOrNull() ?: 3

        showLoading(true)
        Log.d("DEBUG_MAPEL", "Update -> ID: $subjectId, Nama: $nama, Kode: $kode, Kategori: $kategori, Paket IDs: $selectedPackageIds, JP: $jamPelajaran")

        ApiClient.apiService.updateSubject(subjectId, nama, kode, kategori, selectedTingkatanId!!, selectedPackageIds, jamPelajaran).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (_binding != null) {
                    showLoading(false)
                    val result = response.body()?.string() ?: response.errorBody()?.string() ?: ""
                    Log.d("DEBUG_MAPEL", "Respon Server: $result")

                    if (response.isSuccessful && (result.contains("success", true) || result.contains("berhasil", true))) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Mapel berhasil diperbarui") {
                            findNavController().popBackStack()
                        }
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = result, fallbackMessage = "Gagal memperbarui mapel")
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
