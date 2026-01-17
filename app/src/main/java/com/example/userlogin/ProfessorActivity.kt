package com.example.userlogin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.userlogin.databinding.ActivityProfessorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfessorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfessorBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = auth.currentUser!!.uid
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)

        // Auto offline on disconnect
        userRef.child("status").onDisconnect().setValue(false)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("username").getValue(String::class.java)
                val status = snapshot.child("status").getValue(Boolean::class.java) ?: false

                binding.TVCurrentUser.text = "Welcome, Prof. $name"
                binding.SWSwitchProfessorActivity.isChecked = status
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.SWSwitchProfessorActivity.setOnCheckedChangeListener { _, checked ->
            userRef.child("status").setValue(checked)
        }

        binding.BTNSignoutProfessorActivity.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
