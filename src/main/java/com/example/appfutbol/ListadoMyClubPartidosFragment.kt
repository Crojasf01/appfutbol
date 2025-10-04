package com.example.appfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Partido
import com.google.firebase.firestore.FirebaseFirestore

class ListadoMyClubPartidosFragment : Fragment() {

    private lateinit var rvPartidos: RecyclerView
    private val listaPartidos = mutableListOf<Partido>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PartidoAdapter
    private val db = FirebaseFirestore.getInstance()
    private val partidosList = mutableListOf<Partido>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_listadomyclubpartidos, container, false)

        recyclerView = view.findViewById(R.id.rvPartidos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PartidoAdapter(partidosList)
        recyclerView.adapter = adapter
        cargarPartidos("club_bosque")

        rvPartidos = view.findViewById(R.id.rvPartidos)

        rvPartidos.layoutManager = LinearLayoutManager(requireContext())
        rvPartidos.adapter = PartidoAdapter(listaPartidos)
        cargarPartidosDesdeFirestore()

        return view
    }

    private fun cargarPartidosDesdeFirestore() {
        listaPartidos.clear()
        db.collection("partidos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val partido = document.toObject(Partido::class.java)
                    listaPartidos.add(partido)
                }
                rvPartidos.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                println("Error al cargar los partidos: $exception")
            }
    }

    private fun cargarPartidos(clubId: String) {
        db.collection("club")
            .document(clubId)
            .collection("partidos")
            .orderBy("fechaPartido")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error al cargar partidos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                partidosList.clear()
                snapshot?.documents?.forEach { doc ->
                    val partido = doc.toObject(Partido::class.java)
                    partido?.let { partidosList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
