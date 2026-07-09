package com.pab.digitallearning.ui.admin.manage.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pab.digitallearning.R
import com.pab.digitallearning.databinding.FragmentMasterDataBinding

class MasterDataFragment : Fragment() {

    private var _binding: FragmentMasterDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMasterDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
    }

    private fun setupNavigation() {
        binding.cardDataGuru.setGemoyClick {
            findNavController().navigate(R.id.action_masterDataFragment_to_kelolaGuruFragment)
        }

        binding.cardDataMurid.setGemoyClick {
            findNavController().navigate(R.id.action_masterDataFragment_to_kelolaSiswaFragment)
        }

        binding.cardDataMapel.setGemoyClick {
            findNavController().navigate(R.id.action_masterDataFragment_to_kelolaMapelFragment)
        }

        binding.cardDataKelas.setGemoyClick {
            findNavController().navigate(R.id.action_masterDataFragment_to_kelolaKelasFragment)
        }

        binding.cardDataPaket.setGemoyClick {
            findNavController().navigate(R.id.action_masterDataFragment_to_kelolaPaketFragment)
        }
    }

    /**
     * Efek Gemoy (Bounce/Scale Up) Extension
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun View.setGemoyClick(action: () -> Unit) {
        this.setOnTouchListener { v, event ->
            if (_binding == null) return@setOnTouchListener false
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true
                    val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1.05f)
                    val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1.05f)
                    scaleX.duration = 100
                    scaleY.duration = 100
                    AnimatorSet().apply {
                        play(scaleX).with(scaleY)
                        start()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f)
                    val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f)
                    scaleX.duration = 400
                    scaleY.duration = 400
                    scaleX.interpolator = OvershootInterpolator(3f)
                    scaleY.interpolator = OvershootInterpolator(3f)
                    AnimatorSet().apply {
                        play(scaleX).with(scaleY)
                        start()
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                        v.postDelayed({
                            if (_binding != null) action()
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
