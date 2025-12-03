package com.example.appfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Partido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PartidosFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PartidoAdapter
    private val listaPartidos = mutableListOf<Partido>()
    private val db = FirebaseFirestore.getInstance()
    // Desde partidos
    // NUll solo para prod , si deseo probar un dia y hora especifico solo lo cambio aqui
    private val FAKE_DAY: String? = "lunes"   // ejemplo: "jueves"
    private val FAKE_HOUR: Int? = 11     // ejemplo: 9
    private val FAKE_MINUTE: Int? = 30   // ejemplo: 59

    // 🧪 ===============================================
    //             FAKE DAY PARA PRUEBAS
    // ================================================
    // Cambia el valor para simular un día:
    // "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"
    // Déjalo en null para usar el día real del sistema.
    //Asi debe quedar para cuando pasemos a prod
    //private val fakeDia: String? = null
    // Asi es para testing
     private val fakeDia: String? = "lunes"   // EJEMPLO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_partidos, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPartidos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PartidoAdapter(listaPartidos) { partido ->
            registrarReserva(partido)
        }
        recyclerView.adapter = adapter

        cargarPartidos()
        return view
    }

    // ===================================================
    // 📌 1. Verifica si ya abrió el día (con fake incluido)
    // ===================================================
    private fun haPasadoHoraApertura(): Boolean {

        val zonaPeru = TimeZone.getTimeZone("America/Lima")
        val calendario = Calendar.getInstance(zonaPeru)

        // 🔹 Aplicar hora falsa si existe
        val horaActual = FAKE_HOUR ?: calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = FAKE_MINUTE ?: calendario.get(Calendar.MINUTE)

        // 🔹 Aplicar día falso si existe
        val diaHoy = (FAKE_DAY ?: SimpleDateFormat("EEEE", Locale("es", "ES"))
            .format(calendario.time)).lowercase()

        // Solo estos días tienen horario de apertura
        val (horaApertura, minutoApertura) = when (diaHoy) {
            "lunes" -> 10 to 0
            "miércoles", "miercoles" -> 10 to 0
            "viernes" -> 12 to 0
            "sábado", "sabado" -> 0 to 0  // abre domingo todo el día
            else -> return true  // martes, jueves, domingo no muestran apertura
        }

        return (horaActual > horaApertura) ||
                (horaActual == horaApertura && minutoActual >= minutoApertura)
    }

    // =================================================
    // 📌 2. Calcula el día permitido (con fake incluido)
    // =================================================
    private fun diaPermitido(): String {

        val zonaPeru = TimeZone.getTimeZone("America/Lima")
        val calendario = Calendar.getInstance(zonaPeru)

        // ⬇️ Usa fakeDia si existe, sino el día real
        val diaHoy = fakeDia?.lowercase()
            ?: SimpleDateFormat("EEEE", Locale("es", "ES"))
                .format(calendario.time)
                .lowercase()

        return when (diaHoy) {
            "lunes" -> "martes"
            "martes" -> "martes"
            "miércoles", "miercoles" -> "jueves"
            "jueves" -> "jueves"
            "viernes" -> "sábado"
            "sábado", "sabado" -> "domingo"
            "domingo" -> "domingo"
            else -> diaHoy
        }
    }

    // ==================================
    // 📌 3. Cargar y filtrar partidos
    // ==================================
    private fun cargarPartidos() {

        if (!haPasadoHoraApertura()) {
            listaPartidos.clear()
            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Aún no inicia el horario de apertura", Toast.LENGTH_SHORT).show()
            return
        }

        val diaMostrar = diaPermitido()

        db.collection("club")
            .document("club_bosque")
            .collection("partidos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                listaPartidos.clear()

                snapshot?.documents?.forEach { doc ->
                    val partido = doc.toObject(Partido::class.java)
                    partido?.let {
                        val dia = it.dia?.lowercase() ?: ""

                        if (dia == diaMostrar.lowercase()) {
                            listaPartidos.add(it)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            }
    }

    // ============================
    // 📌 4. Registro de reservación
    // ============================
    private fun registrarReserva(partido: Partido) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para reservar", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val dia = partido.dia ?: return
        val hora = partido.hora ?: return
        val lugar = partido.lugar ?: "Cancha principal"

        if (!horaValidaParaDia(dia, hora)) {
            Toast.makeText(requireContext(), "Horario no permitido para $dia", Toast.LENGTH_LONG).show()
            return
        }

        // Trae la visualizacion de las reservas del usuario en partidos fragment desde tabla reservas para daniela user
        db.collection("reservas")
            .whereEqualTo("userId", userId)
            .whereEqualTo("dia", dia)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty()) {
                    Toast.makeText(requireContext(), "Ya tienes una reserva este día", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val nombreUsuario = userDoc.getString("nombre")
                            ?: user.displayName
                            ?: "Jugador"

                        val reserva = hashMapOf(
                            "userId" to userId,
                            "nombreUsuario" to nombreUsuario,
                            "dia" to dia,
                            "hora" to hora,
                            "lugar" to lugar,
                            "registradoEn" to System.currentTimeMillis()
                        )

                        db.collection("reservas")
                            .add(reserva)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Reserva registrada", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al registrar reserva", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
    }

    private fun horaValidaParaDia(dia: String, horaTexto: String): Boolean {
        return when (dia.lowercase()) {
            "martes", "jueves" ->
                horaTexto in listOf("2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM", "7:00 PM")

            "sábado", "sabado", "domingo" ->
                horaTexto in listOf("7:00 AM", "8:00 AM", "9:00 AM")

            else -> false
        }
    }
}
