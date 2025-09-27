package com.example.appfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Inscripcion
import com.example.appfutbol.data.model.InscripcionItem
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


class InscripcionesFragment : Fragment() {

    private lateinit var rvInscripciones: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {   // <- Asegúrate que sea View?
        val view = inflater.inflate(R.layout.fragment_inscripcion, container, false)

        rvInscripciones = view.findViewById(R.id.rvInscripciones)
        rvInscripciones.layoutManager = LinearLayoutManager(requireContext())

        cargarInscripciones()

        return view
    }

    private fun cargarInscripciones() {
        db.collection("inscripciones")
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Inscripcion>()
                for (doc in result) {
                    val ins = doc.toObject(Inscripcion::class.java)

                    // Filtrar horas de 14:00 a 19:00
                    val horaInt = ins.hora?.split(":")?.get(0)?.toIntOrNull() ?: 0
                    if (horaInt in 14..19) {
                        lista.add(ins)
                    }
                }

                // Ordenar por día y hora
                lista.sortWith(compareBy({ diaToInt(it.dia) }, { it.hora }))

                // Crear lista con headers
                val itemsConHeaders = mutableListOf<InscripcionItem>()
                var diaActual = ""
                for (ins in lista) {
                    if (ins.dia != diaActual) {
                        diaActual = ins.dia
                        itemsConHeaders.add(InscripcionItem.Header(diaActual))
                    }
                    itemsConHeaders.add(InscripcionItem.Item(ins))
                }

                rvInscripciones.adapter = InscripcionAdapter(itemsConHeaders)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar inscripciones", Toast.LENGTH_SHORT).show()
            }
    }


    private fun diaToInt(dia: String): Int {
        return when(dia.lowercase(Locale.getDefault())) {
            "martes" -> 1
            "jueves" -> 2
            "sábado" -> 3
            "domingo" -> 4
            else -> 5
        }
    }
}
