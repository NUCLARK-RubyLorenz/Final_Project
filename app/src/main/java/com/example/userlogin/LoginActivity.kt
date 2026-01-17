package com.example.userlogin.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BTNLoginLoginActivity.setOnClickListener { login() }
        binding.BTNCAncelLoginActivity.setOnClickListener { finish() }
    }

    private fun login() {
        val email = binding.ETEmailLoginActivity.text.toString().trim()
        val password = binding.ETPasswordLoginActivity.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            toast("Enter credentials")
            return
        }

        binding.BTNLoginLoginActivity.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                redirect(auth.currentUser!!.uid)
            }
            .addOnFailureListener {
                binding.BTNLoginLoginActivity.isEnabled = true
                toast(it.message ?: "Login failed")
            }
    }

    private fun redirect(uid: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(uid).get()
            .addOnSuccessListener {
                val role = it.child("role").getValue(String::class.java)
                val target =
                    if (role == "Professor") ProfessorActivity::class.java
                    else StudentActivity::class.java

                startActivity(Intent(this, target).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
