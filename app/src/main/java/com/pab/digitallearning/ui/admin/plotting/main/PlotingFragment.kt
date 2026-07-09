package com.pab.digitallearning.ui.admin.plotting.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.R
import com.pab.digitallearning.databinding.FragmentPlotingBinding

class PlotingFragment : Fragment() {

    private var _binding: FragmentPlotingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlotingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardPlotingMurid.setOnClickListener {
            findNavController().navigate(R.id.action_plotingFragment_to_plotingSiswaFragment)
        }

        binding.cardPlotingGuru.setOnClickListener {
            findNavController().navigate(R.id.action_plotingFragment_to_plotingGuruFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
