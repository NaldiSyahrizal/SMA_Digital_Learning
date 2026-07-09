package com.pab.digitallearning.ui.admin.plotting.student

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.Classroom
import com.pab.digitallearning.databinding.FragmentPlotingSiswaBinding
import com.pab.digitallearning.util.DialogUtils
import com.pab.digitallearning.ui.admin.plotting.student.PlotingClassAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlotingSiswaFragment : Fragment() {

    private var _binding: FragmentPlotingSiswaBinding? = null
    private val binding get() = _binding!!
    
    private var allClassrooms = listOf<Classroom>()
    private var adapter: PlotingClassAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlotingSiswaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPlotingSiswa.layoutManager = LinearLayoutManager(requireContext())
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        
        binding.swipeRefresh.setOnRefreshListener {
            fetchClassrooms()
        }
        
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterClassrooms(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchClassrooms()
    }
    
    private fun filterClassrooms(query: String) {
        if (adapter == null) return
        if (query.isEmpty()) {
            adapter?.updateData(allClassrooms)
            return
        }
        val lowerCaseQuery = query.lowercase()
        val filtered = allClassrooms.filter {
            it.namaKelas?.lowercase()?.contains(lowerCaseQuery) == true
        }
        adapter?.updateData(filtered)
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

    private fun fetchClassrooms() {
        showLoading(true)
        ApiClient.apiService.getClassrooms().enqueue(object : Callback<List<Classroom>> {
            override fun onResponse(call: Call<List<Classroom>>, response: Response<List<Classroom>>) {
                showLoading(false)
                if (response.isSuccessful) {
                    allClassrooms = response.body() ?: emptyList()
                    binding.tvTotalClasses.text = allClassrooms.size.toString()
                    
                    adapter = PlotingClassAdapter(allClassrooms) { classroom ->
                        val bundle = Bundle().apply {
                            putLong("classId", classroom.id ?: 0L)
                            putString("className", classroom.namaKelas)
                        }
                        findNavController().navigate(
                            R.id.action_plotingSiswaFragment_to_detailPlotingSiswaFragment,
                            bundle
                        )
                    }
                    binding.rvPlotingSiswa.adapter = adapter
                    filterClassrooms(binding.etSearch.text.toString())
                }
            }

            override fun onFailure(call: Call<List<Classroom>>, t: Throwable) {
                showLoading(false)
                DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan Jaringan: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
