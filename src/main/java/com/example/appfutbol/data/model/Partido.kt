package com.example.appfutbol.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // Import for @Parcelize

@Parcelize // Add this annotation
data class Partido(
    val id: String, // Assuming id is part of your model, good for unique identification
    val titulo: String,
    val lugar: String,
    val fecha: String,
    val hora: String,
    val cupos: Int,
    val inscritos: Int
) : Parcelable // Implement Parcelable