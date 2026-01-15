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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.ET_EmailLoginActivity)
        val etPassword = findViewById<EditText>(R.id.ET_PasswordLoginActivity)

        val btnLogin = findViewById<Button>(R.id.BTN_LoginLoginActivity)
        val btnCancel = findViewById<Button>(R.id.BTN_CAncelLoginActivity)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val isStudent = email.contains("@students.nu-clark.edu.ph")
            val isProfessor = email.contains("@nu-clark.edu.ph")

            if (!isStudent && !isProfessor) {
                Toast.makeText(this, "Enter an eligible NU email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,"Fill in all fields",Toast.LENGTH_SHORT).show()
            }else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (isStudent) {
                                startActivity(Intent(this, StudentActivity::class.java))
                            } else {
                                startActivity(Intent(this, ProfessorActivity::class.java))
                            }

                            finish()

                        } else {
                            Toast.makeText(
                                this,task.exception?.message, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

        }


        btnCancel.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}