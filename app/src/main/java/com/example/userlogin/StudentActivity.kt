package com.example.userlogin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.userlogin.data.UserRepository
import com.example.userlogin.databinding.ActivityStudentBinding
import com.example.userlogin.model.Professor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private val auth = FirebaseAuth.getInstance()
    private val repo = UserRepository()

    private val fullList = mutableListOf<Professor>()
    private val adapter = ProfessorAdapter { prof ->
        repo.toggleFavorite(auth.currentUser!!.uid, prof.uid, prof.isFavorite)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.RVProfessorList.layoutManager = LinearLayoutManager(this)
        binding.RVProfessorList.adapter = adapter

        observeData()
    }

    private fun observeData() {
        val uid = auth.currentUser!!.uid
        val favIds = mutableSetOf<String>()

        repo.observeFavorites(uid, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favIds.clear()
                snapshot.children.forEach { favIds.add(it.key!!) }
                adapter.submitList(
                    fullList.map {
                        it.copy(isFavorite = favIds.contains(it.uid))
                    }
                )
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        repo.observeProfessors(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullList.clear()
                snapshot.children.forEach {
                    fullList.add(
                        Professor(
                            it.key!!,
                            it.child("username").value.toString(),
                            it.child("status").getValue(Boolean::class.java) ?: false
                        )
                    )
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
