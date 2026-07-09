package com.pab.digitallearning.ui.admin.manage.student

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.BasicResponse
import com.pab.digitallearning.data.model.StudentProfile
import com.pab.digitallearning.databinding.FragmentKelolaSiswaBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KelolaSiswaFragment : Fragment() {

    private var _binding: FragmentKelolaSiswaBinding? = null
    private val binding get() = _binding!!
    private var studentList = listOf<StudentProfile>()
    private lateinit var adapter: StudentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaSiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchStudents()
    }

    private fun setupUI() {
        adapter = StudentAdapter { action, student ->
            when (action) {
                StudentAdapter.Action.EDIT -> showActionBottomSheet(student)
                StudentAdapter.Action.DELETE -> confirmDelete(student)
            }
        }

        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter

        val today = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        binding.tvLastUpdate.text = today

        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.fabAdd.setGemoyClick {
            findNavController().navigate(R.id.action_kelolaSiswaFragment_to_addSiswaFragment)
        }

        binding.swipeRefresh.setOnRefreshListener { fetchStudents() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = if (query.isEmpty()) {
                    studentList
                } else {
                    studentList.filter { 
                        it.namaLengkap?.lowercase()?.contains(query) == true || 
                        it.nis?.lowercase()?.contains(query) == true 
                    }
                }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun fetchStudents() {
        if (_binding == null) return
        showLoading(true)

        ApiClient.apiService.getStudentsManage().enqueue(object : Callback<List<StudentProfile>> {
            override fun onResponse(call: Call<List<StudentProfile>>, response: Response<List<StudentProfile>>) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val students = response.body() ?: emptyList()
                        studentList = students
                        adapter.submitList(students)
                        binding.tvTotalStudents.text = students.size.toString()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal mengambil data siswa")
                    }
                }
            }
            override fun onFailure(call: Call<List<StudentProfile>>, t: Throwable) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan jaringan: ${t.message}")
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

    fun showActionBottomSheet(student: StudentProfile) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_package_actions, null)

        view.findViewById<TextView>(R.id.tvPackageName).text = student.namaLengkap ?: "Siswa"

        val btnEdit = view.findViewById<LinearLayout>(R.id.btnEdit)
        val btnDelete = view.findViewById<LinearLayout>(R.id.btnDelete)
        val tvDeleteText = view.findViewById<TextView>(R.id.tvDeleteText)
        tvDeleteText?.text = "Nonaktifkan Siswa"

        btnEdit.setGemoyClick {
            dialog.dismiss()
            editStudent(student)
        }

        btnDelete.setGemoyClick {
            dialog.dismiss()
            confirmDelete(student)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun editStudent(student: StudentProfile) {
        val bundle = Bundle().apply {
            putLong("id", student.id ?: 0L)
            putString("nama", student.namaLengkap)
            putString("nis", student.nis)
            putString("username", student.username)
            putString("email", student.email)
            putString("jk", student.jenisKelamin)
            putString("telp", student.noTelp)
        }
        findNavController().navigate(R.id.action_kelolaSiswaFragment_to_editSiswaFragment, bundle)
    }

    private fun confirmDelete(student: StudentProfile) {
        val input = android.widget.EditText(requireContext())
        input.hint = "Alasan Nonaktif (wajib diisi)"
        
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 0)
        container.addView(input)

        AlertDialog.Builder(requireContext())
            .setTitle("Nonaktifkan Siswa")
            .setMessage("Yakin ingin menonaktifkan siswa ${student.namaLengkap}?")
            .setView(container)
            .setPositiveButton("Nonaktifkan") { _, _ -> 
                val reason = input.text.toString().trim()
                if (reason.isEmpty()) {
                    DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Alasan nonaktif harus diisi!")
                } else {
                    deleteStudent(student.id ?: 0L, student.namaLengkap ?: "Siswa", reason) 
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteStudent(id: Long, name: String, reason: String) {
        showLoading(true)
        ApiClient.apiService.deleteStudent(id, reason).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        com.pab.digitallearning.util.AdminActivityTracker.logActivity(requireContext(), "Menonaktifkan data siswa: $name", "delete")
                        DialogUtils.showSuccessDialog(requireContext(), message = "Siswa berhasil dinonaktifkan") {
                            fetchStudents()
                        }
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal menonaktifkan siswa")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

