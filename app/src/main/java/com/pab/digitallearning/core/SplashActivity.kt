package com.pab.digitallearning.core

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pab.digitallearning.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sceneSchool = findViewById<android.view.View>(R.id.sceneSchool)
        val sceneApp = findViewById<android.view.View>(R.id.sceneApp)

        // Wait 1.5 seconds on Scene 1
        Handler(Looper.getMainLooper()).postDelayed({
            // Fade out Scene 1 (App), Fade in Scene 2 (School)
            sceneApp.animate().alpha(0f).setDuration(500).start()
            sceneSchool.animate().alpha(1f).setDuration(500).start()

            // Wait another 1.5 seconds, then go to MainActivity
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 1500)
            
        }, 1500)
    }
}
