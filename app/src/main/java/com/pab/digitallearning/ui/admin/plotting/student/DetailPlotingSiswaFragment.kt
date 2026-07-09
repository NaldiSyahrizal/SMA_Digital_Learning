package com.pab.digitallearning.ui.admin.plotting.student

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.data.model.StudentClassroom
import com.pab.digitallearning.databinding.FragmentDetailPlotingSiswaBinding
import com.pab.digitallearning.util.DialogUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailPlotingSiswaFragment : Fragment() {

    private var _binding: FragmentDetailPlotingSiswaBinding? = null
    private val binding get() = _binding!!

    private var classId: Long = 0
    private lateinit var adapter: PlotingSiswaAdapter
    private var allClassrooms = listOf<Classroom>()
    private var allStudents = listOf<StudentClassroom>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPlotingSiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        classId = arguments?.getLong("classId") ?: 0L
        binding.tvClassName.text = arguments?.getString("className") ?: "Detail Kelas"

        adapter = PlotingSiswaAdapter { action, item ->
            when (action) {
                PlotingSiswaAdapter.Action.EDIT -> showEditDialog(item)
                PlotingSiswaAdapter.Action.DELETE -> confirmDelete(item)
            }
        }

        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { fetchStudents() }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnRefresh.setOnClickListener { fetchStudents() }

        binding.fabAddStudent.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("classId", classId)
                putString("className", arguments?.getString("className"))
            }
            findNavController().navigate(
                R.id.action_detailPlotingSiswaFragment_to_addPlotingSiswaFragment, bundle
            )
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStudents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchClassrooms()
        fetchStudents()
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            (binding.ivLoadingSpinner.drawable as? Animatable)?.start()
        } else {
            binding.loadingOverlay.visibility = View.GONE
            (binding.ivLoadingSpinner.drawable as? Animatable)?.stop()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun filterStudents(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(allStudents)
            return
        }
        val lowerCaseQuery = query.lowercase()
        val filtered = allStudents.filter {
            it.student?.namaLengkap?.lowercase()?.contains(lowerCaseQuery) == true ||
            it.student?.nis?.lowercase()?.contains(lowerCaseQuery) == true
        }
        adapter.submitList(filtered)
    }

    private fun fetchClassrooms() {
        ApiClient.apiService.getClassrooms().enqueue(object : Callback<List<Classroom>> {
            override fun onResponse(call: Call<List<Classroom>>, response: Response<List<Classroom>>) {
                if (response.isSuccessful) allClassrooms = response.body() ?: emptyList()
            }
            override fun onFailure(call: Call<List<Classroom>>, t: Throwable) {}
        })
    }

    private fun fetchStudents() {
        showLoading(true)
        ApiClient.apiService.getStudentClassrooms().enqueue(object : Callback<List<StudentClassroom>> {
            override fun onResponse(call: Call<List<StudentClassroom>>, response: Response<List<StudentClassroom>>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    allStudents = all.filter { it.class_id == classId }
                    filterStudents(binding.etSearch.text.toString())
                    binding.tvTotalSiswa.text = "${allStudents.size} Siswa Terdaftar"
                } else {
                    DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal memuat data siswa")
                }
            }
            override fun onFailure(call: Call<List<StudentClassroom>>, t: Throwable) {
                showLoading(false)
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
            }
        })
    }

    private fun showEditDialog(item: StudentClassroom) {
        if (allClassrooms.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Data kelas belum siap, coba refresh")
            return
        }

        val classNames = allClassrooms.map { it.namaKelas ?: "-" }.toTypedArray()
        val currentIndex = allClassrooms.indexOfFirst { it.id == item.class_id }.coerceAtLeast(0)

        var selectedIndex = currentIndex

        AlertDialog.Builder(requireContext())
            .setTitle("Pindah Kelas: ${item.student?.namaLengkap}")
            .setSingleChoiceItems(classNames, currentIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Simpan") { _, _ ->
                val newClassId = allClassrooms[selectedIndex].id ?: return@setPositiveButton
                updatePloting(item.id ?: 0L, newClassId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updatePloting(plotingId: Long, newClassId: Long) {
        showLoading(true)
        ApiClient.apiService.updateStudentClassroom(plotingId, newClassId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Siswa berhasil dipindah!") {
                            fetchStudents()
                        }
                    } else {
                        showLoading(false)
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal memindahkan siswa")
                    }
                }
                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    showLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
                }
            })
    }

    private fun confirmDelete(item: StudentClassroom) {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluarkan Siswa")
            .setMessage("Keluarkan ${item.student?.namaLengkap} dari kelas?")
            .setPositiveButton("Keluarkan") { _, _ ->
                showLoading(true)
                ApiClient.apiService.deleteStudentClassroom(item.id ?: 0L)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) fetchStudents() else showLoading(false)
                        }
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            showLoading(false)
                        }
                    })
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
