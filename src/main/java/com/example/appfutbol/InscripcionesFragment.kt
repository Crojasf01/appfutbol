package com.example.appfutbol

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
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.play.integrity.internal.h
import InscripcionItem



class InscripcionesFragment : Fragment() {

    private lateinit var rvInscripciones: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    // =============================
    // 👻 MODO FAKE PARA PRUEBAS
    // =============================
    private val usarDiaFake = true  // ← true para pruebas, false para producción
    private val diaFake = "lunes"
    private val horaFake = 11
    private val minutoFake = 30
    private val segundoFake = 15

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // trae la listsa de inscripciones por hora desde el fragment icono de grupo
        val view = inflater.inflate(R.layout.fragment_inscripcion, container, false)

        // ====== Toolbar Back ======
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarInscripciones)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        rvInscripciones = view.findViewById(R.id.recyclerInscripciones)
        rvInscripciones.layoutManager = LinearLayoutManager(requireContext())

        cargarInscripciones()

        return view
    }

    //Desde inscripciones fragment trae tambien la lista para el icono de grupo q esta en mi club fragment
    /*
    1️⃣ MyClubFragment detecta clic en iconGrouping
    ↓
    2️⃣ MyClubFragment abre InscripcionesFragment
    ↓
    3️⃣ InscripcionesFragment se crea
    ↓
    4️⃣ En onCreateView, llama a cargarInscripciones()
    ↓
    5️⃣ cargarInscripciones() llama a Firestore
    ↓
    6️⃣ Filtra por día actual
    ↓
    7️⃣ Agrupa por hora
    ↓
    8️⃣ Muestra la lista con el adapter

     */

    private fun cargarInscripciones() {
        Log.d("InscripcionesDebug", "Inicio cargarInscripciones() - usarDiaFake=$usarDiaFake")

        db.collection("reservas")
            .orderBy("dia", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d("InscripcionesDebug", "Firestore returned ${result.size()} documentos")

                // 1) Calendar actual (fake o real)
                val calendarActual = if (usarDiaFake) {
                    Calendar.getInstance().apply {
                        val diaSemana = when (diaFake.lowercase(Locale("es"))) {
                            "domingo" -> Calendar.SUNDAY
                            "lunes" -> Calendar.MONDAY
                            "martes" -> Calendar.TUESDAY
                            "miércoles", "miercoles" -> Calendar.WEDNESDAY
                            "jueves" -> Calendar.THURSDAY
                            "viernes" -> Calendar.FRIDAY
                            "sábado", "sabado" -> Calendar.SATURDAY
                            else -> get(Calendar.DAY_OF_WEEK)
                        }
                        set(Calendar.DAY_OF_WEEK, diaSemana)
                        set(Calendar.HOUR_OF_DAY, horaFake)
                        set(Calendar.MINUTE, minutoFake)
                        set(Calendar.SECOND, segundoFake)
                        set(Calendar.MILLISECOND, 0)
                    }
                } else {
                    Calendar.getInstance()
                }

                val sdf = SimpleDateFormat("EEEE", Locale("es", "ES"))
                val diaActualRaw = sdf.format(calendarActual.time)
                val diaActual = normalizar(diaActualRaw)
                val diaActualCapital = diaActual.replaceFirstChar { it.uppercase() }

                Log.d("InscripcionesDebug", "Día actual='$diaActual'")

                // 2) Obtener el día objetivo según regla especial
                val diaObjetivo = diaSiguientePermitido(diaActual)

                if (diaObjetivo == null) {
                    Log.w("InscripcionesDebug", "Hoy ($diaActual) no se deben mostrar inscripciones")
                    rvInscripciones.adapter = InscripcionAdapter(emptyList())
                    return@addOnSuccessListener
                }

                Log.d("InscripcionesDebug", "Día objetivo a mostrar = '$diaObjetivo'")

                // 3) Mapear documentos
                val reservas = mutableListOf<Reserva>()
                for (doc in result.documents) {
                    Log.d("InscripcionesDebug", "DocId=${doc.id} -> data=${doc.data}")

                    val tryObj = try {
                        doc.toObject(Reserva::class.java)
                    } catch (t: Throwable) {
                        null
                    }
                    if (tryObj != null) {
                        reservas.add(tryObj)
                        continue
                    }

                    val creador = doc.getString("creador") ?: "Sin creador"
                    val dia = doc.getString("dia") ?: ""
                    val hora = doc.getString("hora") ?: ""
                    val lugar = doc.getString("lugar") ?: ""
                    val usuarioId = doc.getString("usuarioId") ?: ""

                    val r = Reserva(
                        creador = creador,
                        dia = dia,
                        hora = hora,
                        lugar = lugar,
                        usuarioId = usuarioId,
                        registradoEn = doc.getString("registradoEn")
                    )
                    reservas.add(r)
                }

                Log.d("InscripcionesDebug", "Reservas mapeadas=${reservas.size}")

                // 4) Filtrar SOLO reservas del día objetivo
                val reservasDelDia = reservas.filter {
                    normalizar(it.dia) == normalizar(diaObjetivo)
                }

                Log.d("InscripcionesDebug", "Reservas del día objetivo ($diaObjetivo) = ${reservasDelDia.size}")

                if (reservasDelDia.isEmpty()) {
                    rvInscripciones.adapter = InscripcionAdapter(emptyList())
                    return@addOnSuccessListener
                }

                // 5) Ordenar y construir lista final con headers
                val itemsConHeaders = mutableListOf<InscripcionItem>()
                itemsConHeaders.add(
                    InscripcionItem.HeaderDia(
                        diaObjetivo.replaceFirstChar { it.uppercase() },
                        ""
                    )
                )

                // OJO aquie estaba el error por el formato de la hora, que se pudo arreglar cuando hago clic en lista por hora
                val formatoHora = SimpleDateFormat("hh:mm a", Locale.getDefault())

                fun normalizarHora(h: String?): String {
                    if (h.isNullOrBlank()) return "12:00 AM"
                    val txt = h.trim()
                        .replace("am", "AM")
                        .replace("pm", "PM")

                    // Si le falta AM/PM → asignar por defecto
                    return if (!txt.contains("AM") && !txt.contains("PM")) {
                        "$txt AM"
                    } else txt
                }

                val agrupadoPorHora = reservasDelDia.groupBy { normalizarHora(it.hora) }

                // 1) Detectar horas corruptas
                agrupadoPorHora.keys.forEach { hora ->
                    val horaNorm = normalizarHora(hora)

                    // Hora válida debe tener formato HH:mm (24h)
                    val regex24 = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

                    if (!regex24.matches(horaNorm)) {
                        Log.e(
                            "HORAS_CORRUPTAS",
                            "Hora inválida encontrada: '$hora' → normalizada: '$horaNorm'"
                        )
                    }
                }

                // 2) Ordenar simplemente (24h ya se ordena correctamente como string)
                val horasOrdenadas = agrupadoPorHora.keys
                    .map { normalizarHora(it) }
                    .sorted()
                //cnt-4bb46d80-f3a3-4106-b23c-125aa0899993.containerhub.tripleten-services.com/


                horasOrdenadas.forEach { hora ->
                    val horaLabel = hora.replace("am", "AM").replace("pm", "PM")
                    itemsConHeaders.add(InscripcionItem.HeaderDia(horaLabel, null))

                    val lista = agrupadoPorHora[hora] ?: emptyList()
                    lista.forEachIndexed { index, reserva ->
                        val inscripcion = Inscripcion(
                            nombre = reserva.creador ?: "Sin nombre",
                            hora = horaLabel,
                            fecha = diaObjetivo.replaceFirstChar { it.uppercase() },
                            categoriaEdad = "",
                            usuarioId = reserva.usuarioId
                        )
                        itemsConHeaders.add(InscripcionItem.Item(inscripcion, index + 1))
                    }
                }

                rvInscripciones.adapter = InscripcionAdapter(itemsConHeaders)
            }
            .addOnFailureListener { e ->
                Log.e("InscripcionesDebug", "Error al cargar reservas", e)
                Toast.makeText(requireContext(), "Error al cargar inscripciones", Toast.LENGTH_SHORT).show()
            }
    }

    // Función normalizar (AÑADE ESTO FUERA DEL MÉTODO)
    private fun normalizar(texto: String?): String {
        if (texto == null) return ""
        val temp = java.text.Normalizer.normalize(texto.lowercase(), java.text.Normalizer.Form.NFD)
        return temp.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
    }

    private fun fixHora(hora: String?): String {
        if (hora == null) return "00:00 AM"
        return if (hora.matches(Regex("^[0-9]:.*"))) {
            "0$hora"
        } else hora
    }

    private fun diaSiguientePermitido(diaActual: String): String? {
        return when (diaActual.lowercase()) {
            "lunes" -> "martes"
            "miércoles", "miercoles" -> "jueves"
            "viernes" -> "sabado"
            "sábado", "sabado" -> "domingo"
            else -> null  // martes, jueves, domingo → no mostrar nada
        }
    }


}
