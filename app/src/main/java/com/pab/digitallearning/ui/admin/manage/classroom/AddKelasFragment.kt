package com.pab.digitallearning.ui.admin.manage.classroom

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.TeacherProfile
import com.pab.digitallearning.databinding.FragmentTambahKelasBinding
import com.pab.digitallearning.util.DialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddKelasFragment : Fragment() {

    private var _binding: FragmentTambahKelasBinding? = null
    private val binding get() = _binding!!
    
    private var allTeachers = listOf<TeacherProfile>()
    private var selectedWaliKelasId: Long = 0
    private var tingkatans: List<com.pab.digitallearning.data.model.Tingkatan> = emptyList()
    private var selectedTingkatanId: Long? = null
    private var packages: List<com.pab.digitallearning.data.model.Package> = emptyList()
    private var selectedPackageId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahKelasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        binding.actvTingkat.setOnClickListener { showTingkatBottomSheet() }
        binding.actvWaliKelas.setOnClickListener { showTeacherBottomSheet() }
        binding.actvPaket.setOnClickListener { showPackageBottomSheet() }
        
        // Prevent typing in AutoCompleteTextViews
        binding.actvTingkat.keyListener = null
        binding.actvWaliKelas.keyListener = null
        binding.actvPaket.keyListener = null
        
        binding.btnSimpan.setOnClickListener { saveClassroom() }
        
        fetchTeachers()
        fetchTingkatans()
        fetchPackages()
    }
    
    private fun fetchTeachers() {
        ApiClient.apiService.getTeachersManage().enqueue(object : Callback<List<TeacherProfile>> {
            override fun onResponse(call: Call<List<TeacherProfile>>, response: Response<List<TeacherProfile>>) {
                if (response.isSuccessful) {
                    allTeachers = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<TeacherProfile>>, t: Throwable) {}
        })
    }
    
    private fun fetchTingkatans() {
        ApiClient.apiService.getTingkatans().enqueue(object : Callback<List<com.pab.digitallearning.data.model.Tingkatan>> {
            override fun onResponse(
                call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>,
                response: Response<List<com.pab.digitallearning.data.model.Tingkatan>>
            ) {
                if (response.isSuccessful) {
                    tingkatans = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>, t: Throwable) {}
        })
    }

    private fun fetchPackages() {
        ApiClient.apiService.getPackages().enqueue(object : Callback<List<com.pab.digitallearning.data.model.Package>> {
            override fun onResponse(call: Call<List<com.pab.digitallearning.data.model.Package>>, response: Response<List<com.pab.digitallearning.data.model.Package>>) {
                if (response.isSuccessful) {
                    packages = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.Package>>, t: Throwable) {}
        })
    }

    private fun showPackageBottomSheet() {
        val tingkatanId = selectedTingkatanId
        if (tingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Pilih tingkatan terlebih dahulu!")
            return
        }

        if (packages.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Memuat data paket, coba lagi...")
            fetchPackages()
            return
        }

        val filteredPackages = packages.filter { it.tingkatanId == tingkatanId }
        if (filteredPackages.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Tidak ada paket untuk tingkatan kelas ini!")
            return
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_select_single_bottom_sheet, null)
        
        view.findViewById<TextView>(R.id.tvBottomSheetTitle).text = "Pilih Paket"
        view.findViewById<View>(R.id.searchLayout).visibility = View.GONE
        
        val rv = view.findViewById<RecyclerView>(R.id.rvSingleSelect)
        val packageList = filteredPackages.map { Pair(it.namaPaket ?: "-", it.id ?: 0L) }
        
        val adapter = SingleSelectAdapter(packageList) { name, id ->
            binding.actvPaket.setText(name)
            selectedPackageId = id
            dialog.dismiss()
        }
        
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showTingkatBottomSheet() {
        if (tingkatans.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Memuat data tingkat, coba lagi...")
            fetchTingkatans()
            return
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_select_single_bottom_sheet, null)
        
        view.findViewById<TextView>(R.id.tvBottomSheetTitle).text = "Pilih Tingkat"
        view.findViewById<View>(R.id.searchLayout).visibility = View.GONE
        
        val rv = view.findViewById<RecyclerView>(R.id.rvSingleSelect)
        val tingkatList = tingkatans.map { Pair(it.namaTingkatan ?: "-", it.id ?: 0L) }
        
        val adapter = SingleSelectAdapter(tingkatList) { name, id ->
            if (selectedTingkatanId != id) {
                selectedTingkatanId = id
                selectedPackageId = null
                binding.actvPaket.setText("")
            }
            binding.actvTingkat.setText(name)
            dialog.dismiss()
        }
        
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        
        dialog.setContentView(view)
        dialog.show()
    }
    
    private fun showTeacherBottomSheet() {
        if (allTeachers.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Memuat data guru, coba lagi...")
            fetchTeachers()
            return
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_select_single_bottom_sheet, null)
        
        view.findViewById<TextView>(R.id.tvBottomSheetTitle).text = "Pilih Wali Kelas"
        
        val rv = view.findViewById<RecyclerView>(R.id.rvSingleSelect)
        val search = view.findViewById<EditText>(R.id.etSearch)
        
        val teacherList = allTeachers.map { Pair(it.namaLengkap ?: "-", it.userId ?: 0L) }
        
        val adapter = SingleSelectAdapter(teacherList) { name, id ->
            binding.actvWaliKelas.setText(name)
            selectedWaliKelasId = id
            dialog.dismiss()
        }
        
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun saveClassroom() {
        val nama = binding.etNamaKelas.text.toString().trim()
        val tingkat = binding.actvTingkat.text.toString().trim()
        
        if (nama.isEmpty() || selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap isi nama kelas dan tingkat")
            return
        }
        
        showLoading(true)
        ApiClient.apiService.addClassroom(nama, selectedTingkatanId!!, selectedWaliKelasId, selectedPackageId).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Kelas berhasil ditambahkan") {
                            findNavController().popBackStack()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menambahkan kelas")
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
