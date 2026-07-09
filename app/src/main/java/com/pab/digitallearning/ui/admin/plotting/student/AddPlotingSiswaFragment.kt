package com.pab.digitallearning.ui.admin.plotting.student

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.data.model.StudentProfile
import com.pab.digitallearning.databinding.FragmentTambahPlotingSiswaBinding
import com.pab.digitallearning.ui.admin.plotting.student.SelectStudentAdapter
import com.pab.digitallearning.util.DialogUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPlotingSiswaFragment : Fragment() {

    private val TAG = "AddPlotingSiswa"

    private var _binding: FragmentTambahPlotingSiswaBinding? = null
    private val binding get() = _binding!!

    private lateinit var studentAdapter: SelectStudentAdapter
    private var allUnassignedStudents = listOf<StudentProfile>()
    private var classrooms = listOf<Classroom>()
    private var selectedClassId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPlotingSiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argClassId = arguments?.getLong("classId") ?: 0L
        val argClassName = arguments?.getString("className")

        if (argClassId != 0L) {
            selectedClassId = argClassId
            binding.actvKelas.setText(argClassName, false)
            binding.actvKelas.isEnabled = false
        }

        studentAdapter = SelectStudentAdapter()
        binding.rvSelectStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSelectStudents.adapter = studentAdapter

        fetchUnassignedStudents()
        if (selectedClassId == null) fetchClassrooms()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.etSearchStudent.doOnTextChanged { text, _, _, _ ->
            filterStudents(text?.toString() ?: "")
        }

        binding.btnSave.setOnClickListener { savePloting() }
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return
        if (isLoading) {
            binding.loadingOverlay.visibility = View.VISIBLE
            (binding.ivLoadingSpinner.drawable as? Animatable)?.start()
        } else {
            binding.loadingOverlay.visibility = View.GONE
            (binding.ivLoadingSpinner.drawable as? Animatable)?.stop()
        }
    }

    private fun fetchClassrooms() {
        showLoading(true)
        ApiClient.apiService.getClassrooms().enqueue(object : Callback<List<Classroom>> {
            override fun onResponse(call: Call<List<Classroom>>, response: Response<List<Classroom>>) {
                showLoading(false)
                if (response.isSuccessful) {
                    classrooms = response.body() ?: emptyList()
                    val options = classrooms.map { it.namaKelas ?: "Unknown" }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
                    binding.actvKelas.setAdapter(adapter)
                    binding.actvKelas.setOnItemClickListener { _, _, position, _ ->
                        selectedClassId = classrooms[position].id
                    }
                }
            }
            override fun onFailure(call: Call<List<Classroom>>, t: Throwable) {
                showLoading(false)
            }
        })
    }

    private fun fetchUnassignedStudents() {
        binding.btnSave.isEnabled = false
        showLoading(true)
        ApiClient.apiService.getUnassignedStudents().enqueue(object : Callback<List<StudentProfile>> {
            override fun onResponse(call: Call<List<StudentProfile>>, response: Response<List<StudentProfile>>) {
                binding.btnSave.isEnabled = true
                showLoading(false)
                if (response.isSuccessful) {
                    allUnassignedStudents = response.body() ?: emptyList()
                    studentAdapter.submitList(allUnassignedStudents)
                }
            }
            override fun onFailure(call: Call<List<StudentProfile>>, t: Throwable) {
                binding.btnSave.isEnabled = true
                showLoading(false)
            }
        })
    }

    private fun filterStudents(query: String) {
        val filtered = if (query.isEmpty()) allUnassignedStudents else allUnassignedStudents.filter {
            (it.namaLengkap?.contains(query, true) ?: false) ||
            (it.nis?.contains(query, true) ?: false)
        }
        studentAdapter.submitList(filtered)
    }

    private fun savePloting() {
        val classId = selectedClassId
        if (classId == null) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Pilih kelas dulu bray!")
            return
        }

        val studentIds = studentAdapter.getSelectedIds()
        if (studentIds.isEmpty()) {
            DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Pilih minimal satu siswa bray!")
            return
        }

        binding.btnSave.isEnabled = false
        showLoading(true)
        ApiClient.apiService.addStudentClassroom(classId, studentIds)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    binding.btnSave.isEnabled = true
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Siswa berhasil diploting") {
                            findNavController().popBackStack()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal memploting siswa")
                    }
                }
                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    binding.btnSave.isEnabled = true
                    showLoading(false)
                    DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Terjadi kesalahan koneksi")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
