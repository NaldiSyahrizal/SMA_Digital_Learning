package com.pab.digitallearning.ui.admin.manage.subject

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
import com.pab.digitallearning.data.model.Subject
import com.pab.digitallearning.databinding.FragmentKelolaMapelBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.util.setGemoyClick
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KelolaMapelFragment : Fragment() {

    private var _binding: FragmentKelolaMapelBinding? = null
    private val binding get() = _binding!!
    private var subjectList = listOf<Subject>()
    private lateinit var adapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaMapelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        fetchSubjects()
    }

    private fun setupUI() {
        adapter = SubjectAdapter { subject ->
            showActionBottomSheet(subject)
        }

        binding.rvSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubjects.adapter = adapter

        binding.btnBack.setGemoyClick { findNavController().popBackStack() }
        binding.btnTambahMapel.setGemoyClick { 
            findNavController().navigate(R.id.action_kelolaMapelFragment_to_addMapelFragment) 
        }

        binding.swipeRefresh.setOnRefreshListener { fetchSubjects() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = if (query.isEmpty()) {
                    subjectList
                } else {
                    subjectList.filter { it.nama?.lowercase()?.contains(query) == true }
                }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun fetchSubjects() {
        if (_binding == null) return
        showLoading(true)
        ApiClient.apiService.getSubjects().enqueue(object : Callback<List<Subject>> {
            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful) {
                        val subjects = response.body() ?: emptyList()
                        subjectList = subjects
                        adapter.submitList(subjects)
                        updateStats(subjects.size)
                    } else {
                        DialogUtils.showErrorDialog(requireContext(), fallbackMessage = "Gagal mengambil data mapel")
                    }
                }
            }

            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                if (_binding != null) {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan jaringan: ${t.message}")
                }
            }
        })
    }

    private fun updateStats(total: Int) {
        binding.tvTotalMapel.text = total.toString()
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        binding.tvLastUpdate.text = sdf.format(Date())
    }

    private fun showActionBottomSheet(subject: Subject) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_subject_actions, null)
        
        view.findViewById<TextView>(R.id.tvSubjectName).text = subject.nama

        view.findViewById<View>(R.id.btnEdit).setGemoyClick {
            dialog.dismiss()
            navigateToEdit(subject)
        }

        view.findViewById<View>(R.id.btnDelete).setGemoyClick {
            dialog.dismiss()
            confirmDelete(subject)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun navigateToEdit(subject: Subject) {
        val bundle = Bundle().apply {
            putLong("id", subject.id ?: 0L)
            putString("nama", subject.nama)
            putString("kode", subject.kodeMapel)
            putLong("tingkatan_id", subject.tingkatanId ?: 0L)
            putString("tingkatan_name", subject.tingkatanName)
            putString("kategori", subject.kategori)
            putLongArray("package_ids", subject.packageIds?.toLongArray() ?: longArrayOf())
            putString("packages", subject.packages)
            putInt("jam_pelajaran", subject.jamPelajaran ?: 3)
        }
        findNavController().navigate(R.id.action_kelolaMapelFragment_to_editMapelFragment, bundle)
    }

    private fun confirmDelete(subject: Subject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Mapel")
            .setMessage("Yakin ingin menghapus mata pelajaran ${subject.nama}?")
            .setPositiveButton("Hapus") { _, _ -> deleteSubject(subject.id ?: 0L) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteSubject(id: Long) {
        showLoading(true)
        ApiClient.apiService.deleteSubject(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (_binding != null) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        DialogUtils.showSuccessDialog(requireContext(), message = "Mapel berhasil dihapus") {
                            fetchSubjects()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorBody, fallbackMessage = "Gagal menghapus mapel")
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
