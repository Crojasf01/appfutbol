package com.example.appfutbol.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.R
import com.google.firebase.firestore.FirebaseFirestore

data class PartidoAdmin(
    val id: String = "",
    val dia: String = "",
    val hora: String = "",
    val lugar: String = "",
    val creador: String = ""
)

class AdminGestionPartidosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val listaPartidos = mutableListOf<PartidoAdmin>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)
        recyclerView = view.findViewById(R.id.recyclerPartidosAdmin)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarPartidos()

        return view
    }

    private fun cargarPartidos() {
        db.collection("partido_1").get()
            .addOnSuccessListener { result ->
                listaPartidos.clear()
                for (doc in result) {
                    val partido = PartidoAdmin(
                        id = doc.id,
                        dia = doc.getString("dia") ?: "",
                        hora = doc.getString("hora") ?: "",
                        lugar = doc.getString("lugar") ?: "",
                        creador = doc.getString("creador") ?: ""
                    )
                    listaPartidos.add(partido)
                }
                recyclerView.adapter = PartidosAdminAdapter(listaPartidos,
                    onEditClick = { partido -> mostrarDialogoEditar(partido) },
                    onDeleteClick = { partido -> eliminarPartido(partido) }
                )
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar partidos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarPartido(partido: PartidoAdmin) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar partido")
            .setMessage("¿Seguro que deseas eliminar el partido del ${partido.dia} en ${partido.lugar}?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("partido_1").document(partido.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Partido eliminado", Toast.LENGTH_SHORT).show()
                        cargarPartidos()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(partido: PartidoAdmin) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_crear_partido, null)
        val spinnerLugar = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerLugar)
        val spinnerHora = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerHora)
        val spinnerDia = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerDia)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Partido")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoDia = spinnerDia.selectedItem.toString()
                val nuevoLugar = spinnerLugar.selectedItem.toString()
                val nuevaHora = spinnerHora.selectedItem.toString()

                if (nuevoLugar.isNotEmpty() && nuevaHora.isNotEmpty()) {
                    db.collection("partido_1").document(partido.id)
                        .update(mapOf("dia" to nuevoDia, "hora" to nuevaHora, "lugar" to nuevoLugar))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Partido actualizado", Toast.LENGTH_SHORT).show()
                            cargarPartidos()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
