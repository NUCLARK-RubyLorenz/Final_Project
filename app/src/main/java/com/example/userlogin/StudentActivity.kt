package com.example.userlogin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.userlogin.databinding.ActivityStudentBinding
import com.example.userlogin.databinding.ItemProfessorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding
    private lateinit var auth: FirebaseAuth

    // Master list holds all data from Firebase
    private val fullProfessorList = mutableListOf<Professor>()
    // Display list holds what is currently shown (filtered)
    private val displayList = mutableListOf<Professor>()
    private val favoriteIds = mutableSetOf<String>()

    private lateinit var adapter: ProfessorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Initialize Adapter with displayList
        adapter = ProfessorAdapter(displayList) { professor ->
            toggleFavorite(professor)
        }

        binding.RVProfessorList.layoutManager = LinearLayoutManager(this)
        binding.RVProfessorList.adapter = adapter

        // Listen for search input changes
        binding.ETSearchProfessor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Sign out logic
        binding.BTNSignoutStudentActivity.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadFavoritesAndProfessors()
    }

    private fun loadFavoritesAndProfessors() {
        val myUid = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("Users")

        // 1. Listen for Favorites
        userRef.child(myUid).child("favorites").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(favSnapshot: DataSnapshot) {
                favoriteIds.clear()
                favSnapshot.children.forEach { it.key?.let { id -> favoriteIds.add(id) } }

                // Fetch professors only once or update current list
                fetchProfessorsFromServer()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchProfessorsFromServer() {
        FirebaseDatabase.getInstance().getReference("Users")
            .orderByChild("role").equalTo("Professor")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullProfessorList.clear()
                    for (postSnapshot in snapshot.children) {
                        val profUid = postSnapshot.key ?: ""
                        val name = postSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
                        val status = postSnapshot.child("status").getValue(Boolean::class.java) ?: false

                        val prof = Professor(profUid, name, status, favoriteIds.contains(profUid))
                        fullProfessorList.add(prof)
                    }
                    // Apply current search filter to the new data
                    filterList(binding.ETSearchProfessor.text.toString())
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun filterList(query: String) {
        displayList.clear()
        if (query.isEmpty()) {
            displayList.addAll(fullProfessorList)
        } else {
            val filtered = fullProfessorList.filter {
                it.username.contains(query, ignoreCase = true)
            }
            displayList.addAll(filtered)
        }

        // Sort: Favorites at the top, then by name
        displayList.sortWith(compareByDescending<Professor> { it.isFavorite }.thenBy { it.username })
        adapter.notifyDataSetChanged()
    }

    private fun toggleFavorite(professor: Professor) {
        val myUid = auth.currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(myUid).child("favorites").child(professor.uid)

        if (professor.isFavorite) {
            favRef.removeValue()
        } else {
            favRef.setValue(true)
        }
    }

    // --- ADAPTER ---
    class ProfessorAdapter(
        private val list: List<Professor>,
        private val onFavClick: (Professor) -> Unit
    ) : RecyclerView.Adapter<ProfessorAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemProfessorBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemProfessorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val prof = list[position]
            holder.binding.apply {
                TVItemProfessorName.text = prof.username
                TVItemStatusText.text = if (prof.status) "Available" else "Busy"

                IVFavorite.setImageResource(
                    if (prof.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_border
                )

                VStatusIndicator.setBackgroundResource(
                    if (prof.status) R.drawable.status_on else R.drawable.status_off
                )

                IVFavorite.setOnClickListener { onFavClick(prof) }
            }
        }

        override fun getItemCount() = list.size
    }
}