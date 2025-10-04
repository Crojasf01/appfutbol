package com.example.appfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Partido
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PartidosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PartidoAdapter
    private val listaPartidos = mutableListOf<Partido>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout del fragment (asegúrate que este layout NO tenga fondo blanco)
        val view = inflater.inflate(R.layout.fragment_partidos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPartidos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PartidoAdapter(listaPartidos)
        recyclerView.adapter = adapter

        //Leer argumento para decidir que query hacer
        val ordernarPorHora = arguments?.getBoolean("ordenarPorHora", false) ?: false
        cargarPartidos(ordernarPorHora)
        return view
    }

    private fun cargarPartidos(ordenarPorHora: Boolean) {
        val db = FirebaseFirestore.getInstance()

        // Si quieres TODOS los partidos:
        db.collection("partidos")
            .get()
            .addOnSuccessListener { documents ->
                listaPartidos.clear()
                for (document in documents) {
                    val partido = document.toObject(Partido::class.java)
                    listaPartidos.add(partido)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

        // 👇 Si quieres filtrados por hora/día:
        // db.collection("partidos")
        //   .whereEqualTo("dia", "martes")
        //   .get()
        //   .addOnSuccessListener { ... }
    }

}
