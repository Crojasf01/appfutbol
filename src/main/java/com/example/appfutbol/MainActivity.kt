package com.example.appfutbol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appfutbol.data.model.OnPartidoClickListener
import com.example.appfutbol.data.model.Partido
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.appfutbol.ui.perfil.PerfilFragment
import com.google.firebase.FirebaseApp


class MainActivity : AppCompatActivity(), OnPartidoClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // tu layout principal con BottomNavigationView
        FirebaseApp.initializeApp(this) // Inicializa Firebase

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(PartidosFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_partidos -> {
                    loadFragment(PartidosFragment())
                    true
                }
                R.id.navigation_perfil -> {
                    loadFragment(PerfilFragment())
                    true
                }
                R.id.navigation_miclub -> {
                    loadFragment(MyClubFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onPartidoClick(partido: Partido) {
    }
}
