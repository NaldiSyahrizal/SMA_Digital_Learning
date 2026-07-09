package com.pab.digitallearning.ui.admin.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import com.pab.digitallearning.util.DialogUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.R
import com.pab.digitallearning.core.ApiClient
import com.pab.digitallearning.data.model.DashboardResponse
import com.pab.digitallearning.databinding.FragmentHomeBinding
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityAdapter: AdminActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupNavigation()
        fetchDashboardData()
    }

    private fun setupRecyclerView() {
        activityAdapter = AdminActivityAdapter()
        binding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivities.adapter = activityAdapter
    }

    private fun setupNavigation() {
        // Pasang efek Gemoy ke semua card
        binding.cardTotalUser.setGemoyClick {
            findNavController().navigate(R.id.action_nav_home_to_kelolaGuruFragment)
        }
        binding.cardTotalSiswa.setGemoyClick {
            findNavController().navigate(R.id.action_nav_home_to_kelolaSiswaFragment)
        }
        binding.cardTotalKelas.setGemoyClick {
            findNavController().navigate(R.id.action_nav_home_to_kelolaKelasFragment)
        }
        binding.cardTotalMapel.setGemoyClick {
            findNavController().navigate(R.id.action_nav_home_to_kelolaMapelFragment)
        }
    }

    private fun fetchDashboardData() {
        // 1. Fetch dashboard stats
        ApiClient.apiService.getDashboardStats().enqueue(object : Callback<DashboardResponse> {
            override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                // Cek apakah binding masih ada sebelum update UI
                if (_binding != null && response.isSuccessful) {
                    val stats = response.body()?.data
                    stats?.let {
                        animateCounter(binding.tvTotalGuruCount, it.totalGuru)
                        animateCounter(binding.tvTotalSiswaCount, it.totalSiswa)
                        animateCounter(binding.tvTotalKelasCount, it.totalKelas)
                        animateCounter(binding.tvTotalMapelCount, it.totalMapel)
                    }
                } else if (_binding != null) {
                    Log.e("HomeFragment", "Error stats: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                if (_binding != null) {
                    Log.e("HomeFragment", "Failure stats: ${t.message}")
                    DialogUtils.showErrorDialog(requireContext(), errorBodyString = "Kesalahan jaringan")
                }
            }
        })

        // 2. Load Local Activities
        val activities = com.pab.digitallearning.util.AdminActivityTracker.getActivities(requireContext())
        activityAdapter.submitList(activities)
        if (activities.isEmpty()) {
            binding.layoutEmptyActivities.visibility = View.VISIBLE
            binding.rvActivities.visibility = View.GONE
        } else {
            binding.layoutEmptyActivities.visibility = View.GONE
            binding.rvActivities.visibility = View.VISIBLE
        }
    }

    private fun animateCounter(textView: TextView, targetValue: Int) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1200
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    /**
     * Efek Gemoy (Bounce/Scale) Extension
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun View.setGemoyClick(action: () -> Unit) {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true // Nyalain warna biru selector
                    // Melembung (membesar) dikit pas diteken
                    val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.06f)
                    val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.06f)
                    scaleX.duration = 100
                    scaleY.duration = 100
                    AnimatorSet().apply {
                        play(scaleX).with(scaleY)
                        start()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false // Matiin warna biru selector
                    // Balik ke ukuran semula dengan efek mantul kenyal
                    val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f)
                    val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f)
                    scaleX.duration = 400
                    scaleY.duration = 400
                    scaleX.interpolator = OvershootInterpolator(3f) // Makin bouncy
                    scaleY.interpolator = OvershootInterpolator(3f)
                    AnimatorSet().apply {
                        play(scaleX).with(scaleY)
                        start()
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                        // Kasih jeda dikit biar animasinya keliatan mantul dulu baru pindah
                        v.postDelayed({
                            action()
                        }, 200)
                    }
                }
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
