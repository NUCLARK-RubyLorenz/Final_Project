package com.example.userlogin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth.currentUser?.uid?.let { checkRoleAndRedirect(it) }

        binding.BTNLoginMainActivity.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.BTNSignupMainActivity.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun checkRoleAndRedirect(uid: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(uid).get()
            .addOnSuccessListener { snap ->
                val role = snap.child("role").getValue(String::class.java)
                val target = if (role == "Professor")
                    ProfessorActivity::class.java
                else
                    StudentActivity::class.java

                startActivity(Intent(this, target))
                finish()
            }
            .addOnFailureListener {
                auth.signOut()
            }
    }
}
