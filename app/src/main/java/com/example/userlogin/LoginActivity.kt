package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.BTNLoginLoginActivity.setOnClickListener {
            performLogin()
        }

        binding.BTNCAncelLoginActivity.setOnClickListener {
            finish() 
        }
    }

    private fun performLogin() {
        val email = binding.ETEmailLoginActivity.text.toString().trim()
        val password = binding.ETPasswordLoginActivity.text.toString().trim()

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        binding.BTNLoginLoginActivity.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    fetchUserRoleAndRedirect(uid)
                } else {
                    // binding.PBLoading.visibility = View.GONE
                    binding.BTNLoginLoginActivity.isEnabled = true
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchUserRoleAndRedirect(uid: String) {
        val database = FirebaseDatabase.getInstance().getReference("Users").child(uid)

        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val role = snapshot.child("role").getValue(String::class.java)

                val intent = when (role) {
                    "Professor" -> Intent(this, ProfessorActivity::class.java)
                    "Student" -> Intent(this, StudentActivity::class.java)
                    else -> {
                        Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "User data record not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
            binding.BTNLoginLoginActivity.isEnabled = true
        }
    }
}