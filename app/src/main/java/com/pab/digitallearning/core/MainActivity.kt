package com.pab.digitallearning.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pab.digitallearning.AdminDashboardActivity
import com.pab.digitallearning.databinding.ActivityMainBinding
import com.pab.digitallearning.ui.teacher.TeacherDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Auto Login Routing
        if (sessionManager.isLoggedIn()) {
            val role = sessionManager.getRole()
            if (role == "admin") {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return
            } else if (role == "kepala_sekolah") {
                startActivity(Intent(this, com.pab.digitallearning.ui.principal.PrincipalDashboardActivity::class.java))
                finish()
                return
            } else if (role == "teacher" || role == "guru" || role == "pengajar") {
                startActivity(Intent(this, TeacherDashboardActivity::class.java))
                finish()
                return
            } else if (role == "student" || role == "siswa" || role == "murid") {
                startActivity(Intent(this, com.pab.digitallearning.ui.student.StudentDashboardActivity::class.java))
                finish()
                return
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // MainActivity sekarang cuma jadi host buat NavHostFragment (Login) jika belum login
    }
}
