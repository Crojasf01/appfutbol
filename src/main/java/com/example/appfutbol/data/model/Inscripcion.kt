package com.example.appfutbol.data.model

import com.google.firebase.Timestamp

data class Inscripcion(
    val categoriaEdad: String = "",
    val dia: String = "",
    var fecha: Timestamp? = null, // <-- usar Timestamp
    val hora: String = "",
    val usuarioId: String = ""
)
