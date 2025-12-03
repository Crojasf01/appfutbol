package com.example.appfutbol.data.model

data class Inscripcion(
    val categoriaEdad: String = "",
    val dia: String = "",
    var fecha: String = "", // <-- usar Timestamp
    val hora: String = "",
    val usuarioId: String? = "",
    val nombre: String = ""
)
