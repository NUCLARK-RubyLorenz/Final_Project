package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.BTNSignupSignUpActivity.setOnClickListener {
            val username = binding.ETUsernameSignupActivity.text.toString().trim()
            val email = binding.ETEmailSignupActivity.text.toString().trim()
            val password = binding.ETPasswordSignupActivity.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isStudent = email.endsWith("@students.nu-clark.edu.ph")
            val isProfessor = email.endsWith("@nu-clark.edu.ph")

            if (!isStudent && !isProfessor) {
                Toast.makeText(this, "Use a valid NU-Clark email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val userMap = mapOf(
                        "username" to username,
                        "email" to email,
                        "role" to if (isProfessor) "Professor" else "Student",
                        "status" to false // Default offline
                    )
                    database.child("Users").child(userId).setValue(userMap).addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.BTNCancelSignupActivity.setOnClickListener { finish() }
    }
}