package com.pab.digitallearning.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Efek Gemoy (Bouncy Scale) untuk semua View
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setGemoyClick(action: () -> Unit) {
    this.setOnTouchListener { v, event ->
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
                    v.postDelayed({ action() }, 180)
                }
            }
        }
        true
    }
}
