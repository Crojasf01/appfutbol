package com.example.appfutbol.data.model

sealed class InscripcionItem {
    data class Header(val dia: String) : InscripcionItem()
    data class Item(val inscripcion: Inscripcion) : InscripcionItem()
}
