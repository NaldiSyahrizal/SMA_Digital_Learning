package com.pab.digitallearning.ui.admin.plotting.teacher

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.pab.digitallearning.data.model.TeachingAssignment
import com.pab.digitallearning.databinding.FragmentPlotingGuruBinding
import com.pab.digitallearning.util.DialogUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlotingGuruFragment : Fragment() {

    private var _binding: FragmentPlotingGuruBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlotingGuruAdapter
    private var allAssignments = listOf<TeachingAssignment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlotingGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlotingGuruAdapter { action, assignment ->
            when (action) {
                PlotingGuruAdapter.Action.EDIT -> editAssignment(assignment)
                PlotingGuruAdapter.Action.DELETE -> confirmDelete(assignment)
            }
        }

        binding.rvAssignments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssignments.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnRefresh.setOnClickListener { fetchAssignments() }
        binding.swipeRefresh.setOnRefreshListener { fetchAssignments() }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_plotingGuruFragment_to_addPlotingGuruFragment)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAssignments(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchAssignments()
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

    private fun filterAssignments(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(allAssignments)
            return
        }
        val lowerCaseQuery = query.lowercase()
        val filtered = allAssignments.filter {
            it.teacherName?.lowercase()?.contains(lowerCaseQuery) == true ||
            it.subjectName?.lowercase()?.contains(lowerCaseQuery) == true ||
            it.className?.lowercase()?.contains(lowerCaseQuery) == true
        }
        adapter.submitList(filtered)
    }

    private fun fetchAssignments() {
        showLoading(true)
        ApiClient.apiService.getTeachingAssignments().enqueue(object : Callback<List<TeachingAssignment>> {
            override fun onResponse(call: Call<List<TeachingAssignment>>, response: Response<List<TeachingAssignment>>) {
                showLoading(false)
                if (response.isSuccessful) {
                    allAssignments = response.body() ?: emptyList()
                    filterAssignments(binding.etSearch.text.toString())
                    binding.tvTotalAssignments.text = "${allAssignments.size} Ploting"
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error Unknown"
                    Log.e("FETCH_ERROR", "Code: ${response.code()}, Body: $errorMsg")
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = errorMsg, fallbackMessage = "Gagal memuat data")
                }
            }

            override fun onFailure(call: Call<List<TeachingAssignment>>, t: Throwable) {
                showLoading(false)
                Log.e("FETCH_ERROR", "Failure: ${t.message}")
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
            }
        })
    }

    private fun editAssignment(assignment: TeachingAssignment) {
        val bundle = Bundle().apply {
            putLong("assignmentId", assignment.id ?: 0L)
            putLong("teacherId", assignment.teacherId)
            putLong("subjectId", assignment.subjectId)
            putLong("classId", assignment.classId)
            putString("teacherName", assignment.teacherName)
            putString("subjectName", assignment.subjectName)
            putString("className", assignment.className)
        }
        findNavController().navigate(R.id.action_plotingGuruFragment_to_addPlotingGuruFragment, bundle)
    }

    private fun confirmDelete(assignment: TeachingAssignment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Ploting")
            .setMessage("Hapus ploting ${assignment.teacherName} di kelas ${assignment.className}?")
            .setPositiveButton("Hapus") { _, _ ->
                showLoading(true)
                ApiClient.apiService.deleteTeachingAssignment(assignment.id ?: 0L).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) fetchAssignments() else showLoading(false)
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
