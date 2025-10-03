package com.example.appfutbol.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize // Import for @Parcelize

@Parcelize
data class Partido(
    val titulo: String? = "",
    val lugar: String? = "",
    val fechaPartido: Timestamp? = null,
    val cupos: Int? = 0,
    val estado: String? = null,
    val jugadores: List<Jugador>? = null,
) : Parcelable

@Parcelize
data class Jugador(
    val nombre: String? = null,
    val hora: String? = null,
    val categoriaEdad: String? = null
) : Parcelable
