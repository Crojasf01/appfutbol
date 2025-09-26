package com.example.appfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.hdodenhof.circleimageview.CircleImageView

class MyClubFragment : Fragment() {

    private lateinit var logoImage: CircleImageView
    private lateinit var clubTitle: TextView
    private lateinit var gridIcon: ImageView
    private lateinit var fieldIcon: ImageView
    private lateinit var iconGrouping: ImageView
    private lateinit var settingsIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_myclub, container, false)

        // Inicializar views
        logoImage = view.findViewById(R.id.logoImage)
        clubTitle = view.findViewById(R.id.clubTitle)
        gridIcon = view.findViewById(R.id.btnGrid)
        fieldIcon = view.findViewById(R.id.btnList)
        iconGrouping = view.findViewById(R.id.btnUser)
        settingsIcon = view.findViewById(R.id.btnSettings)

        // Texto dinámico
        clubTitle.text = "Casa Jade Sports"

        // Eventos
        logoImage.setOnClickListener {
            Toast.makeText(requireContext(), "Logo del club pulsado ⚽", Toast.LENGTH_SHORT).show()
        }

        fieldIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Ir al campo de juego 🏟️", Toast.LENGTH_SHORT).show()
        }

        gridIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir menú de jugadores 👥", Toast.LENGTH_SHORT).show()
        }

        settingsIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir ajustes ⚙️", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
