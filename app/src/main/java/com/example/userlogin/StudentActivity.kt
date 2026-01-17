package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var statusCircle: View
    private lateinit var tvProfessorStatus: TextView
    private var statusListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser

        if(currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid

        val signOut = findViewById<Button>(R.id.BTN_SignoutStudentActivity)
        statusCircle = findViewById(R.id.V_StatusCircleStudentActivity)
        val tvCurrentUser = findViewById<TextView>(R.id.TV_CurrentUserStudent)
        tvProfessorStatus = findViewById(R.id.TV_ProfessorStatus)

        // Fetch username from Firebase Database
        database.getReference("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    if (username != null) {
                        tvCurrentUser.text = "Welcome $username"
                    } else {
                        tvCurrentUser.text = "Welcome User"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvCurrentUser.text = "Welcome User"
                }
            })

        // Load and monitor professor status from Firebase in real-time
        monitorProfessorStatus()

        signOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun monitorProfessorStatus() {
        // Monitor all users in real-time for professor status changes
        statusListener = database.getReference("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var anyProfessorOnline = false
                    var professorName = "Professor"

                    // Loop through all users to find available professors
                    for (userSnapshot in snapshot.children) {
                        val role = userSnapshot.child("role").getValue(String::class.java)
                        val status = userSnapshot.child("status").getValue(Boolean::class.java) ?: false

                        // Check if this user is a professor with status = true
                        if (role == "Professor" && status) {
                            anyProfessorOnline = true
                            professorName = userSnapshot.child("username").getValue(String::class.java) ?: "Professor"
                            break // Found an available professor
                        }
                    }

                    // Update UI based on professor availability
                    updateStatusUI(anyProfessorOnline, professorName)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error - show offline status
                    statusCircle.setBackgroundResource(R.drawable.status_off)
                    tvProfessorStatus.text = "Unable to check status"
                }
            })
    }

    private fun updateStatusUI(isAvailable: Boolean, professorName: String) {
        if (isAvailable) {
            statusCircle.setBackgroundResource(R.drawable.status_on)
            tvProfessorStatus.text = "$professorName is available"
        } else {
            statusCircle.setBackgroundResource(R.drawable.status_off)
            tvProfessorStatus.text = "No professors available"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener to prevent memory leaks
        statusListener?.let {
            database.getReference("Users").removeEventListener(it)
        }
    }
}