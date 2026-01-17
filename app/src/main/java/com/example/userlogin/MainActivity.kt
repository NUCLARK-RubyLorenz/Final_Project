package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 1. Auto-Login Logic
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.uid)
        }

        binding.BTNLoginMainActivity.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.BTNSignupMainActivity.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(String::class.java)
                val intent = if (role == "Professor") {
                    Intent(this, ProfessorActivity::class.java)
                } else {
                    Intent(this, StudentActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
    }
}