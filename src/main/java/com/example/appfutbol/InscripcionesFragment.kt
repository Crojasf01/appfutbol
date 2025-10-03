package com.example.appfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.appfutbol.data.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.forEachIndexed
import com.google.android.material.appbar.MaterialToolbar

class InscripcionesFragment : Fragment() {

    private lateinit var rvInscripciones: RecyclerView
    private lateinit var adapter: InscripcionAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {   // <- Asegúrate que sea View?
        val view = inflater.inflate(R.layout.fragment_inscripcion, container, false)

        rvInscripciones = view.findViewById(R.id.recyclerInscripciones)
        rvInscripciones.layoutManager = LinearLayoutManager(requireContext())

        cargarPartidos()

        return view
    }

    private fun cargarPartidos() {
        val db = FirebaseFirestore.getInstance()

        db.collection("partidos")
            .orderBy("fechaPartido", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                // 1) Mapear partidos
                val lista = mutableListOf<Partido>()
                for (doc in result) {
                    val partido = doc.toObject(Partido::class.java)
                    if (partido != null) lista.add(partido)
                }

                val diasFijos = listOf("Martes", "Jueves", "Sábado", "Domingo")
                val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

                val itemsConHeaders = mutableListOf<InscripcionItem>()

                // Helper local para normalizar "jugador" (puede venir como Map o como objeto)
                data class Player(val nombre: String?, val hora: String?, val categoriaEdad: String?)

                for (dia in diasFijos) {
                    // Filtrar partidos del día
                    val partidosDelDia = lista.filter { it.titulo?.equals(dia, ignoreCase = true) == true }

                    partidosDelDia.forEach { partido ->
                        // Normalizar lista de jugadores (soporta Map<String, Any?> o objetos)
                        val jugadoresRaw = partido.jugadores ?: emptyList<Any?>()

                        val jugadoresNorm: List<Player> = jugadoresRaw.mapNotNull { raw ->
                            when (raw) {
                                is Map<*, *> -> {
                                    val nombre = raw["nombre"] as? String
                                    val hora = raw["hora"] as? String
                                    val cat = (raw["categoriaEdad"] as? String) ?: (raw["categoria"] as? String)
                                    Player(nombre = nombre, hora = hora, categoriaEdad = cat)
                                }
                                else -> {
                                    // Si tienes una clase Jugador en data.model con esos campos:
                                    try {
                                        // intentar castear reflexivamente (si es tu propia clase)
                                        val nombreField = raw?.javaClass?.getMethod("getNombre")?.invoke(raw) as? String
                                        val horaField = raw?.javaClass?.getMethod("getHora")?.invoke(raw) as? String
                                        val catField = try { raw?.javaClass?.getMethod("getCategoriaEdad")?.invoke(raw) as? String } catch(_: Exception) { null }
                                        Player(nombre = nombreField, hora = horaField, categoriaEdad = catField)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                        }

                        // Agrupar por hora (si hora nula la agrupamos por "")
                        val jugadoresPorHora = jugadoresNorm
                            .groupBy { it.hora ?: "" }
                            .toSortedMap(Comparator { a, b ->
                                try {
                                    val da = formatoHora.parse(a)
                                    val db = formatoHora.parse(b)
                                    when {
                                        da == null && db == null -> 0
                                        da == null -> 1
                                        db == null -> -1
                                        else -> da.compareTo(db)
                                    }
                                } catch (ex: Exception) {
                                    a.compareTo(b)
                                }
                            })

                        // Por cada hora, añadimos un header partido (dia + hora) y luego jugadores numerados
                        jugadoresPorHora.forEach { (hora, jugadoresList) ->
                            val horaLabel = if (hora.isBlank()) "Hora no disponible" else hora
                            itemsConHeaders.add(InscripcionItem.HeaderPartido(dia, horaLabel))

                            jugadoresList.forEachIndexed { idx, player ->
                                val nombre = player.nombre ?: "Sin nombre"
                                val horaJugador = player.hora ?: horaLabel
                                val categoria = player.categoriaEdad ?: ""

                                val ins = com.example.appfutbol.data.model.Inscripcion(
                                    nombre = nombre,
                                    hora = horaJugador,
                                    fecha = partido.fechaPartido,
                                    categoriaEdad = categoria
                                )

                                itemsConHeaders.add(InscripcionItem.Item(ins, idx + 1))
                            }
                        }
                    }
                }

                // Asignar adapter (asegúrate que tu adapter importa InscripcionItem desde data.model)
                rvInscripciones.adapter = InscripcionAdapter(itemsConHeaders)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al cargar partidos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.rvInscripciones)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


}
