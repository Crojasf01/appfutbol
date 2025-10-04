package com.example.appfutbol.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize // Import for @Parcelize

@Parcelize
data class Partido(
    val titulo: String? = "",
    val fechaPartido: Timestamp? = null,
    val cupos: Int? = 0,
    val estado: String? = null,
    val jugadores: List<Jugador>? = null,

    val diaNumero: String = "",
    //valores de fragment partido
    val dia: String ="",
    val hora: String = "",
    val lugar: String? = "",
    val creador: String? ="",

    val mes: String = "",

    val diaTexto: String= ""
) : Parcelable

@Parcelize
data class Jugador(
    val nombre: String? = null,
    val hora: String? = null,
    val categoriaEdad: String? = null
) : Parcelable
