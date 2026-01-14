package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.userlogin.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth = FirebaseAuth.getInstance()



        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val btnSignUpSignupActivity = findViewById<Button>(R.id.BTN_SignupSignUpActivity)
        val btnCancelSignupActivity = findViewById<Button>(R.id.BTN_CancelSignupActivity)

        val etEmail = findViewById<EditText>(R.id.ET_UsernameSignupActivity)
        val etPassword = findViewById<EditText>(R.id.ET_PasswordSignupActivity)

        btnSignUpSignupActivity.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val isStudent = email.contains("@students.nu-clark.edu.ph")
            val isProfessor = email.contains("@nu-clark.edu.ph")

            if (!isStudent && !isProfessor) {
                Toast.makeText(this, "Enter an eligible NU email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    task ->
                    if(task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnCancelSignupActivity.setOnClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }


    }
}