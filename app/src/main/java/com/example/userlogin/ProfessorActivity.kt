package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfessorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var statusSwitch: SwitchCompat
    private lateinit var statusCircle: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_professor)
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

        val btnSignOut = findViewById<Button>(R.id.BTN_SignoutProfessorActivity)
        statusSwitch = findViewById(R.id.SW_SwitchProfessorActivity)
        statusCircle = findViewById(R.id.V_StatusCircleProfessorActivity)
        val tvCurrentUser = findViewById<TextView>(R.id.TV_CurrentUser)

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

        // Load saved status from Firebase
        loadStatusFromDatabase(userId)

        // Handle switch changes
        statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateStatus(userId, isChecked)
            if (isChecked) {
                statusCircle.setBackgroundResource(R.drawable.status_on)
            } else {
                statusCircle.setBackgroundResource(R.drawable.status_off)
            }
        }

        btnSignOut.setOnClickListener {
            // Don't change status when signing out - preserve user's choice
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loadStatusFromDatabase(userId: String) {
        database.getReference("Users").child(userId).child("status")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isAvailable = snapshot.getValue(Boolean::class.java) ?: false
                    // Only update UI if the switch state doesn't match
                    if (statusSwitch.isChecked != isAvailable) {
                        statusSwitch.isChecked = isAvailable
                    }
                    if (isAvailable) {
                        statusCircle.setBackgroundResource(R.drawable.status_on)
                    } else {
                        statusCircle.setBackgroundResource(R.drawable.status_off)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Default to off if error
                    statusSwitch.isChecked = false
                    statusCircle.setBackgroundResource(R.drawable.status_off)
                }
            })
    }

    private fun updateStatus(userId: String, isAvailable: Boolean) {
        // Update the status in Firebase with proper error handling
        database.getReference("Users").child(userId).child("status")
            .setValue(isAvailable)
            .addOnSuccessListener {
                // Status updated successfully - students will see this change in real-time
            }
            .addOnFailureListener { e ->
                // Revert switch if update failed
                statusSwitch.isChecked = !isAvailable
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't change status on destroy - keep the user's last choice
    }
}