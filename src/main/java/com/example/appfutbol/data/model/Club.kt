package com.example.appfutbol.data.model

import com.google.firebase.Timestamp

data class Club(
    val nombre: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val admin: Admin = Admin()
)
