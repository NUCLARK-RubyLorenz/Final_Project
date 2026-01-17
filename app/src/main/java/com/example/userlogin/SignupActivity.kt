package com.example.userlogin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BTNSignupSignUpActivity.setOnClickListener { signUp() }
        binding.BTNCancelSignupActivity.setOnClickListener { finish() }
    }

    private fun signUp() {
        val username = binding.ETUsernameSignupActivity.text.toString().trim()
        val email = binding.ETEmailSignupActivity.text.toString().trim()
        val password = binding.ETPasswordSignupActivity.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Fill all fields")
            return
        }

        val role = when {
            email.endsWith("@nu-clark.edu.ph") -> "Professor"
            email.endsWith("@students.nu-clark.edu.ph") -> "Student"
            else -> {
                toast("Use NU email")
                return
            }
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid
                usersRef.child(uid).setValue(
                    mapOf(
                        "username" to username,
                        "email" to email,
                        "role" to role,
                        "status" to false
                    )
                )
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                toast(it.message ?: "Signup failed")
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
