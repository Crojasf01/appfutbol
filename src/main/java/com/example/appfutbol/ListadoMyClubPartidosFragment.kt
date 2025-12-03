package com.example.appfutbol

import InscripcionItem
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Inscripcion
import com.example.appfutbol.data.model.Partido
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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

        adapter = PartidoAdapter(partidosList) { partido ->
            //registrarReserva(partido)
        }
        recyclerView.adapter = adapter
        cargarReservasDelDia()

        return view
    }

    // -- Método principal: cargar reservas filtradas por el "día a mostrar"
    private fun cargarReservasDelDia() {

        val diaActualNorm = obtenerDiaActual()   // ejemplo: "jueves"
        Log.i("DIA_ACTUAL", "Hoy es: $diaActualNorm")
        //reservas
        db.collection("reservas")
            .get()
            .addOnSuccessListener { result ->

                val reservasFiltradas = mutableListOf<Inscripcion>()

                for (doc in result.documents) {
                    val diaFirestoreRaw = doc.getString("dia") ?: ""
                    val diaFirestore = normalizar(diaFirestoreRaw)
                    val hora = doc.getString("hora") ?: ""

                    //println("📅 Día recibido de Firestore: '${diaFirestoreRaw}'")

                    reservasFiltradas.add(
                        Inscripcion(
                            nombre = doc.getString("creador")
                                ?: doc.getString("userId")
                                ?: "Sin nombre",
                            hora = hora,
                            fecha = doc.get("registradoEn")?.toString() ?: "",
                            categoriaEdad = "",
                            dia = diaFirestore
                        )
                    )
                }

                // 🔥 FILTRAR SOLO EL DÍA ACTUAL
                val reservasHoy = reservasFiltradas.filter {
                    it.dia == diaActualNorm
                }

                Log.w("FILTRADO", "Reservas totales: ${reservasFiltradas.size}")
                Log.w("FILTRADO", "Reservas SOLO HOY ($diaActualNorm): ${reservasHoy.size}")

                reservasHoy.forEach {
                    Log.i("FILTRADO", "Día: ${it.dia}, Hora: ${it.hora}, Nombre: ${it.nombre}")
                }

                reservasHoy.forEach {
                    Log.e("DIA_ERROR", "Deben quedar SOLO jueves → queda: ${it.dia}")
                }

                // Si NO hay reservas hoy → vaciar adapter
                if (reservasHoy.isEmpty()) {
                    recyclerView.adapter = InscripcionAdapter(emptyList())
                    return@addOnSuccessListener
                }

                // Orden por hora
                val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                val reservasOrdenadas = reservasHoy.sortedBy { formatoHora.parse(it.hora) }

                // 1. Obtener día actual
                val hoy = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                val diaActual = hoy.replaceFirstChar { it.uppercase() }
                Log.d("DIA_ACTUAL", "El día actual es: $diaActual")

                // 2. Filtrar reservas solo del día actual
                val soloDelDia = reservasOrdenadas.filter { it.dia == diaActual }
                Log.d("FILTRO_DEBUG", "Total de partidos para '$diaActual': ${soloDelDia.size}")


                // AGRUPAR POR HORA
                val reservasPorHora = reservasOrdenadas.groupBy { it.hora }
                val horasOrdenadas = reservasPorHora.keys.sortedWith(compareBy { formatoHora.parse(it) })

                val itemsConHeaders = mutableListOf<InscripcionItem>()

                for (horaKey in horasOrdenadas) {

                    itemsConHeaders.add(
                        InscripcionItem.HeaderDia(
                            diaActualNorm.replaceFirstChar { it.uppercase() },
                            horaKey
                        )
                    )

                    reservasPorHora[horaKey]?.forEachIndexed { index, ins ->
                        itemsConHeaders.add(InscripcionItem.Item(ins, index + 1))
                    }
                }

                recyclerView.adapter = InscripcionAdapter(itemsConHeaders)
            }
    }


    // -- Helpers: normalización de texto y día actual/a mostrar
    private fun normalizar(text: String?): String {
        if (text == null) return ""
        return text.trim()
            .lowercase(Locale.getDefault())
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
    }

    private fun obtenerDiaActual(): String {
        val sdf = SimpleDateFormat("EEEE", Locale("es", "ES"))
        val dia = sdf.format(java.util.Date())
        val diaNorm = normalizar(dia)
        Log.d("RESERVAS_DEBUG", "Día actual detectado: '$dia' -> normalizado '$diaNorm'")
        return diaNorm
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
