package com.example.userlogin.data

import com.google.firebase.database.*

class UserRepository {

    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    fun getUser(uid: String, onResult: (DataSnapshot?) -> Unit) {
        usersRef.child(uid).get()
            .addOnSuccessListener { onResult(it) }
            .addOnFailureListener { onResult(null) }
    }

    fun setUserStatus(uid: String, status: Boolean) {
        usersRef.child(uid).child("status").setValue(status)
    }

    fun observeProfessors(listener: ValueEventListener) {
        usersRef.orderByChild("role")
            .equalTo("Professor")
            .addValueEventListener(listener)
    }

    fun observeFavorites(uid: String, listener: ValueEventListener) {
        usersRef.child(uid).child("favorites")
            .addValueEventListener(listener)
    }

    fun toggleFavorite(myUid: String, profUid: String, isFavorite: Boolean) {
        val favRef = usersRef.child(myUid).child("favorites").child(profUid)
        if (isFavorite) favRef.removeValue() else favRef.setValue(true)
    }
}
