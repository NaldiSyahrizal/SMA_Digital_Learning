package com.pab.digitallearning.ui.admin.manage.classroom

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.pab.digitallearning.databinding.FragmentEditKelasBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditKelasFragment : Fragment() {

    private var _binding: FragmentEditKelasBinding? = null
    private val binding get() = _binding!!

    private var classId: Long = 0
    private var isEditMode = false

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
        _binding = FragmentEditKelasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialData()
        setupUI()
    }

    private fun setupInitialData() {
        arguments?.let {
            classId = it.getLong("id", 0)
            binding.etNamaKelas.setText(it.getString("nama_kelas", ""))

            val tingkatanId = it.getLong("tingkatan_id")
            selectedTingkatanId = if (tingkatanId != 0L) tingkatanId else null
            binding.actvTingkat.setText(it.getString("tingkatan_name", ""), false)

            selectedWaliKelasId = it.getLong("wali_kelas_id", 0L)
            binding.actvWaliKelas.setText(it.getString("wali_kelas_name", ""), false)

            val packageId = it.getLong("package_id")
            selectedPackageId = if (packageId != 0L) packageId else null
            binding.actvPaket.setText(it.getString("package_name", ""), false)
        }

        fetchTeachers()
        fetchTingkatans()
        fetchPackages()
    }

    private fun setupUI() {
        binding.btnBack.setGemoyClick { findNavController().popBackStack() }

        binding.btnActionToggle.setGemoyClick {
            if (!isEditMode) {
                enableEditMode(true)
            } else {
                saveClassroom()
            }
        }

        binding.actvTingkat.setOnClickListener { if (isEditMode) showTingkatBottomSheet() }
        binding.actvWaliKelas.setOnClickListener { if (isEditMode) showTeacherBottomSheet() }
        binding.actvPaket.setOnClickListener { if (isEditMode) showPackageBottomSheet() }

        // Prevent keyboard on dropdowns
        binding.actvTingkat.keyListener = null
        binding.actvWaliKelas.keyListener = null
        binding.actvPaket.keyListener = null

        // Start in read-only mode
        enableEditMode(false)
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

        // Update text colors
        val textColor = if (enable) Color.parseColor("#102B5E") else Color.parseColor("#9E9E9E")
        binding.etNamaKelas.setTextColor(textColor)
        binding.actvTingkat.setTextColor(textColor)
        binding.actvWaliKelas.setTextColor(textColor)
        binding.actvPaket.setTextColor(textColor)

        // Enable/disable fields
        binding.etNamaKelas.isEnabled = enable
        binding.actvTingkat.isEnabled = enable
        binding.actvWaliKelas.isEnabled = enable
        binding.actvPaket.isEnabled = enable

        if (enable) binding.etNamaKelas.requestFocus()
    }

    private fun fetchTeachers() {
        ApiClient.apiService.getTeachersManage().enqueue(object : Callback<List<TeacherProfile>> {
            override fun onResponse(call: Call<List<TeacherProfile>>, response: Response<List<TeacherProfile>>) {
                if (_binding != null && response.isSuccessful) {
                    allTeachers = response.body() ?: emptyList()

                    // Set current wali kelas name from loaded teachers
                    if (selectedWaliKelasId > 0) {
                        val teacher = allTeachers.find { it.userId == selectedWaliKelasId }
                        if (teacher != null) {
                            binding.actvWaliKelas.setText(teacher.namaLengkap ?: teacher.username ?: "-", false)
                        }
                    }
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
                if (_binding != null && response.isSuccessful) {
                    tingkatans = response.body() ?: emptyList()
                }
            }
            override fun onFailure(call: Call<List<com.pab.digitallearning.data.model.Tingkatan>>, t: Throwable) {}
        })
    }

    private fun fetchPackages() {
        ApiClient.apiService.getPackages().enqueue(object : Callback<List<com.pab.digitallearning.data.model.Package>> {
            override fun onResponse(call: Call<List<com.pab.digitallearning.data.model.Package>>, response: Response<List<com.pab.digitallearning.data.model.Package>>) {
                if (_binding != null && response.isSuccessful) {
                    packages = response.body() ?: emptyList()
                    if (selectedPackageId != null && selectedPackageId!! > 0) {
                        val pkg = packages.find { it.id == selectedPackageId }
                        if (pkg != null) {
                            binding.actvPaket.setText(pkg.namaPaket ?: "-", false)
                        }
                    }
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
            binding.actvPaket.setText(name, false)
            binding.actvPaket.setTextColor(Color.parseColor("#102B5E"))
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
            fetchTingkatans()
            return
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_select_single_bottom_sheet, null)

        view.findViewById<TextView>(R.id.tvBottomSheetTitle).text = "Pilih Tingkatan"
        view.findViewById<View>(R.id.searchLayout).visibility = View.GONE

        val rv = view.findViewById<RecyclerView>(R.id.rvSingleSelect)
        val list = tingkatans.map { Pair(it.namaTingkatan ?: "-", it.id ?: 0L) }

        val adapter = SingleSelectAdapter(list) { name, id ->
            if (selectedTingkatanId != id) {
                selectedTingkatanId = id
                selectedPackageId = null
                binding.actvPaket.setText("", false)
            }
            binding.actvTingkat.setText(name, false)
            binding.actvTingkat.setTextColor(Color.parseColor("#102B5E"))
            dialog.dismiss()
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showTeacherBottomSheet() {
        if (allTeachers.isEmpty()) {
            fetchTeachers()
            return
        }

        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_select_single_bottom_sheet, null)

        view.findViewById<TextView>(R.id.tvBottomSheetTitle).text = "Pilih Wali Kelas"

        val rv = view.findViewById<RecyclerView>(R.id.rvSingleSelect)
        val search = view.findViewById<EditText>(R.id.etSearch)

        val teacherList = allTeachers.map { Pair(it.namaLengkap ?: it.username ?: "-", it.userId ?: 0L) }

        val adapter = SingleSelectAdapter(teacherList) { name, id ->
            binding.actvWaliKelas.setText(name, false)
            binding.actvWaliKelas.setTextColor(Color.parseColor("#102B5E"))
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

        if (nama.isEmpty() || selectedTingkatanId == null) {
            DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Harap isi nama kelas dan pilih tingkatan")
            return
        }

        showLoading(true)
        ApiClient.apiService.updateClassroom(
            id = classId,
            nama = nama,
            tingkatanId = selectedTingkatanId!!,
            waliId = selectedWaliKelasId,
            packageId = selectedPackageId
        ).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Data kelas berhasil diperbarui") {
                            findNavController().popBackStack()
                        }
                    } else {
                        val err = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = err, fallbackMessage = "Gagal memperbarui kelas")
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
