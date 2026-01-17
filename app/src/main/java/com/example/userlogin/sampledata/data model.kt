package com.example.userlogin.model

import com.google.firebase.database.Exclude

data class Professor(
    val uid: String = "",
    val username: String = "",
    val status: Boolean = false,
    @get:Exclude var isFavorite: Boolean = false
)
