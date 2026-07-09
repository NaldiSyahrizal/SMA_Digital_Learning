package com.pab.digitallearning.ui.admin.manage.classroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.databinding.FragmentKelolaKelasBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KelolaKelasFragment : Fragment() {

    private var _binding: FragmentKelolaKelasBinding? = null
    private val binding get() = _binding!!
    
    private var classroomList = mutableListOf<Classroom>()
    private lateinit var adapter: ClassroomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaKelasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        fetchClassrooms()
    }

    private fun setupUI() {
        adapter = ClassroomAdapter(classroomList) { classroom ->
            showActionBottomSheet(classroom)
        }

        binding.rvClassrooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClassrooms.adapter = adapter

        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        
        binding.btnTambahKelas.setGemoyClick { 
            findNavController().navigate(R.id.action_kelolaKelasFragment_to_addKelasFragment) 
        }

        binding.swipeRefresh.setOnRefreshListener { fetchClassrooms() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = if (query.isEmpty()) {
                    classroomList
                } else {
                    classroomList.filter { it.namaKelas?.lowercase()?.contains(query) == true }
                }
                adapter.updateList(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun fetchClassrooms() {
        if (_binding == null) return
        showLoading(true)
        ApiClient.apiService.getClassrooms().enqueue(object : Callback<List<Classroom>> {
            override fun onResponse(call: Call<List<Classroom>>, response: Response<List<Classroom>>) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val classes = response.body() ?: emptyList()
                        classroomList.clear()
                        classroomList.addAll(classes)
                        adapter.notifyDataSetChanged()
                        updateStats(classes.size)
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal mengambil data kelas")
                    }
                }
            }

            override fun onFailure(call: Call<List<Classroom>>, t: Throwable) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan jaringan: ${t.message}")
                }
            }
        })
    }

    private fun updateStats(total: Int) {
        binding.tvTotalKelas.text = total.toString()
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        binding.tvLastUpdate.text = sdf.format(Date())
    }

    private fun navigateToEdit(classroom: Classroom) {
        val bundle = Bundle().apply {
            putLong("id", classroom.id ?: 0L)
            putString("nama_kelas", classroom.namaKelas)
            putLong("tingkatan_id", classroom.tingkatanId ?: 0L)
            putString("tingkatan_name", classroom.tingkatanName)
            putLong("wali_kelas_id", classroom.waliKelasId ?: 0L)
            putLong("package_id", classroom.packageId ?: 0L)
            putString("package_name", classroom.packageName)
        }
        findNavController().navigate(R.id.action_kelolaKelasFragment_to_editKelasFragment, bundle)
    }

    private fun showActionBottomSheet(classroom: Classroom) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_subject_actions, null)
        
        view.findViewById<TextView>(R.id.tvSubjectName).text = "Kelas ${classroom.namaKelas}"

        view.findViewById<View>(R.id.btnEdit).setGemoyClick {
            dialog.dismiss()
            navigateToEdit(classroom)
        }

        view.findViewById<View>(R.id.btnDelete).setGemoyClick {
            dialog.dismiss()
            confirmDelete(classroom)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun confirmDelete(classroom: Classroom) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kelas")
            .setMessage("Yakin ingin menghapus kelas ${classroom.namaKelas}?")
            .setPositiveButton("Hapus") { _, _ -> deleteClassroom(classroom.id ?: 0L) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteClassroom(id: Long) {
        showLoading(true)
        ApiClient.apiService.deleteClassroom(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Kelas berhasil dihapus") {
                            fetchClassrooms()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menghapus kelas")
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
