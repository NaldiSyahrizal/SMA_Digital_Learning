package com.pab.digitallearning.ui.admin.manage.teacher

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
import com.pab.digitallearning.data.model.TeacherProfile
import com.pab.digitallearning.databinding.FragmentKelolaGuruBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KelolaGuruFragment : Fragment() {

    private var _binding: FragmentKelolaGuruBinding? = null
    private val binding get() = _binding!!
    private var teacherList = listOf<TeacherProfile>()
    private lateinit var adapter: TeacherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchTeachers()
    }

    private fun setupUI() {
        adapter = TeacherAdapter { action, teacher ->
            when (action) {
                TeacherAdapter.Action.EDIT -> showActionBottomSheet(teacher)
                TeacherAdapter.Action.DELETE -> confirmDelete(teacher)
            }
        }

        binding.rvTeachers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTeachers.adapter = adapter

        val today = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        binding.tvLastUpdate.text = today

        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.fabAdd.setGemoyClick {
            findNavController().navigate(R.id.action_kelolaGuruFragment_to_addGuruFragment)
        }

        binding.swipeRefresh.setOnRefreshListener { fetchTeachers() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = if (query.isEmpty()) {
                    teacherList
                } else {
                    teacherList.filter { 
                        it.namaLengkap?.lowercase()?.contains(query) == true || 
                        it.nip?.lowercase()?.contains(query) == true 
                    }
                }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun fetchTeachers() {
        if (_binding == null) return
        showLoading(true)

        ApiClient.apiService.getTeachersManage().enqueue(object : Callback<List<TeacherProfile>> {
            override fun onResponse(call: Call<List<TeacherProfile>>, response: Response<List<TeacherProfile>>) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val teachers = response.body() ?: emptyList()
                        teacherList = teachers
                        adapter.submitList(teachers)
                        binding.tvTotalGuru.text = teachers.size.toString()
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal mengambil data guru")
                    }
                }
            }
            override fun onFailure(call: Call<List<TeacherProfile>>, t: Throwable) {
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

    fun showActionBottomSheet(teacher: TeacherProfile) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_package_actions, null)

        view.findViewById<TextView>(R.id.tvPackageName).text = teacher.namaLengkap ?: "Guru"

        val btnEdit = view.findViewById<LinearLayout>(R.id.btnEdit)
        val btnDelete = view.findViewById<LinearLayout>(R.id.btnDelete)

        btnEdit.setGemoyClick {
            dialog.dismiss()
            editTeacher(teacher)
        }

        btnDelete.setGemoyClick {
            dialog.dismiss()
            confirmDelete(teacher)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun editTeacher(teacher: TeacherProfile) {
        val bundle = Bundle().apply {
            putLong("id", teacher.id ?: 0L)
            putString("nama", teacher.namaLengkap)
            putString("nip", teacher.nip)
            putString("username", teacher.username)
            putString("email", teacher.email)
            putString("jk", teacher.jenisKelamin)
            putString("telp", teacher.noTelp)
        }
        findNavController().navigate(R.id.action_kelolaGuruFragment_to_editGuruFragment, bundle)
    }

    private fun confirmDelete(teacher: TeacherProfile) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Guru")
            .setMessage("Yakin ingin menghapus guru ${teacher.namaLengkap}?")
            .setPositiveButton("Hapus") { _, _ -> deleteTeacher(teacher.id ?: 0L, teacher.namaLengkap ?: "Guru") }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTeacher(id: Long, name: String) {
        showLoading(true)
        ApiClient.apiService.deleteTeacher(id).enqueue(object : Callback<BasicResponse> {
            override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        com.pab.digitallearning.util.AdminActivityTracker.logActivity(requireContext(), "Menghapus data guru: $name", "delete")
                        DialogUtils.showSuccessDialog(requireContext(), message = "Guru berhasil dihapus") {
                            fetchTeachers()
                        }
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal menghapus guru")
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
