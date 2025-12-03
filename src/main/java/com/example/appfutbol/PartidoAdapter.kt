package com.example.appfutbol

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Partido
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PartidoAdapter(
    private val listaPartidos: List<Partido>,
    private val onReservar: (Partido) -> Unit
) : RecyclerView.Adapter<PartidoAdapter.ViewHolder>() {

    private val reservasPorPartido = mutableMapOf<String, String>()
    private var reservasCargadas = false

    // =============================
    // 👻 MODO FAKE PARA PRUEBAS
    // =============================
    private val usarDiaFake = true  // ← true para pruebas, false producción
    private val diaFake = "lunes"
    private val horaFake = 11
    private val minutoFake = 30
    private val segundoFake = 15

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        val tvDia: TextView = itemView.findViewById(R.id.tvDia)
        val tvMes: TextView = itemView.findViewById(R.id.tvMes)
        val layoutHoras: View = itemView.findViewById(R.id.layoutHoras)

        val btnHora2: TextView = itemView.findViewById(R.id.btnHora2)
        val btnHora3: TextView = itemView.findViewById(R.id.btnHora3)
        val btnHora4: TextView = itemView.findViewById(R.id.btnHora4)
        val btnHora5: TextView = itemView.findViewById(R.id.btnHora5)
        val btnHora6: TextView = itemView.findViewById(R.id.btnHora6)
        val btnHora7: TextView = itemView.findViewById(R.id.btnHora7)
        val btnHora7am: TextView = itemView.findViewById(R.id.btnHora7am)
        val btnHora8am: TextView = itemView.findViewById(R.id.btnHora8am)
        val btnHora9am: TextView = itemView.findViewById(R.id.btnHora9am)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val partido = listaPartidos[position]
        val context = holder.itemView.context
        val diaLower = partido.dia?.lowercase(Locale.getDefault()) ?: ""
        println("📅 Día recibido de Firestore: '${partido.dia}'")

        // =============================
        // CALCULAR DÍA Y HORA ACTUAL (REAL O FAKE)
        // =============================
        val calendarActual = if (usarDiaFake) {
            Calendar.getInstance().apply {
                val diaSemana = when(diaFake.lowercase()) {
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

        val formatoDiaActual = SimpleDateFormat("EEEE", Locale("es", "ES"))
        val diaHoySistema = formatoDiaActual.format(calendarActual.time).lowercase()
        val horaAhora = calendarActual.get(Calendar.HOUR_OF_DAY)
        val minutoAhora = calendarActual.get(Calendar.MINUTE)
        val segundoAhora = calendarActual.get(Calendar.SECOND)

        // =============================
        // CONFIGURAR VISTA DEL PARTIDO
        // =============================
        holder.tvTitulo.text = partido.dia ?: "Sin día"

        partido.fechaPartido?.toDate()?.let { fechaDate ->
            val formatoDia = SimpleDateFormat("dd", Locale.getDefault())
            holder.tvDia.text = formatoDia.format(fechaDate)

            val formatoMes = SimpleDateFormat("MMM", Locale("es"))
            holder.tvMes.text = formatoMes.format(fechaDate)

            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.tvHora.text = formatoHora.format(fechaDate)
        }

        holder.layoutHoras.visibility = if (partido.isExpanded) View.VISIBLE else View.GONE
        holder.tvTitulo.setOnClickListener {
            partido.isExpanded = !partido.isExpanded
            notifyItemChanged(position)
        }

        // Ocultar botones al inicio
        val botonesSemana = listOf(
            holder.btnHora2, holder.btnHora3, holder.btnHora4,
            holder.btnHora5, holder.btnHora6, holder.btnHora7
        )
        val botonesFinSemana = listOf(
            holder.btnHora7am, holder.btnHora8am, holder.btnHora9am
        )
        botonesSemana.forEach { it.visibility = View.GONE }
        botonesFinSemana.forEach { it.visibility = View.GONE }

        // Mostrar horarios según el día
        val botones = when (diaLower) {
            "martes", "jueves" -> {
                val horas = listOf("14:00 PM", "15:00 PM", "16:00 PM", "17:00 PM", "18:00 PM", "19:00 PM")
                botonesSemana.forEachIndexed { i, b ->
                    b.visibility = View.VISIBLE
                    b.text = horas[i]
                }
                botonesSemana
            }
            "sabado", "domingo" , "feriado"-> {
                val horas = listOf("7:00 AM", "8:00 AM", "9:00 AM")
                botonesFinSemana.forEachIndexed { i, b ->
                    b.visibility = View.VISIBLE
                    b.text = horas[i]
                }
                botonesFinSemana
            }
            else -> emptyList()
        }

        // =============================
        // REGLAS DE APERTURA POR DÍA
        // =============================
        var esAdmin = false
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        FirebaseFirestore.getInstance().collection("users")
            .document(usuarioActual?.uid ?: "")
            .get()
            .addOnSuccessListener { doc ->
                esAdmin = doc.getString("rol") == "admin"

                val esLunesHoy = diaHoySistema == "lunes"
                val esMiercolesHoy = diaHoySistema == "miércoles" || diaHoySistema == "miercoles"
                val esViernesHoy = diaHoySistema == "viernes"

                val antesDe10 = horaAhora < 10
                val antesDe12 = horaAhora < 12

                var bloquear = false
                var mensaje = ""

                if (esLunesHoy && antesDe10) {
                    bloquear = true
                    mensaje = "⏱ Las inscripciones del lunes se habilitan a las 10:00 AM."
                }
                if (esMiercolesHoy && antesDe10) {
                    bloquear = true
                    mensaje = "⏱ Las inscripciones del miércoles se habilitan a las 10:00 AM."
                }
                if (esViernesHoy && antesDe12) {
                    bloquear = true
                    mensaje = "⏱ Las inscripciones del viernes se habilitan a las 12:00 PM."
                }

                if (bloquear && !esAdmin) {
                    botones.forEach { boton ->
                        boton.isEnabled = false
                        boton.setBackgroundColor(
                            ContextCompat.getColor(holder.itemView.context, R.color.defaultButton)
                        )
                    }
                    Toast.makeText(holder.itemView.context, mensaje, Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                if (bloquear && esAdmin) {
                    Toast.makeText(
                        holder.itemView.context,
                        "👑 Admin: $mensaje",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        // =============================
        // GESTIÓN DE RESERVAS
        // =============================
        val diaVal = partido.dia ?: ""
        val lugarVal = partido.lugar ?: ""
        val clavePartido = "${diaVal}_$lugarVal"

        if (reservasPorPartido.containsKey(clavePartido)) {
            val horaReservada = reservasPorPartido[clavePartido]
            botones.forEach {
                it.isEnabled = false
                it.setBackgroundColor(ContextCompat.getColor(context, R.color.defaultButton))
            }
            botones.find { it.text.toString().trim() == (horaReservada ?: "").trim() }?.apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.selectedGreen))
            }
            if (partido.horaSeleccionada == null && horaReservada != null) {
                partido.horaSeleccionada = horaReservada
            }
            return
        }

        botones.forEach { btn ->
            btn.setBackgroundColor(ContextCompat.getColor(context, R.color.defaultButton))
            btn.isEnabled = partido.horaSeleccionada == null

            btn.setOnClickListener {
                if (partido.horaSeleccionada != null) {
                    Toast.makeText(context, "Ya seleccionaste una hora", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid
                if (userId == null) {
                    Toast.makeText(context, "Debes iniciar sesión para reservar", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val btnHoraText = btn.text.toString().trim()
                val diaLower = diaVal.lowercase(Locale.getDefault())
                val horaValida = when (diaLower) {
                    "martes", "jueves" -> btnHoraText.contains(Regex("(14|15|16|17|18|19).*pm", RegexOption.IGNORE_CASE))
                    "sabado", "domingo", "feriado" -> btnHoraText.contains(Regex("(7|8|9).*am", RegexOption.IGNORE_CASE))
                    else -> false
                }

                if (!horaValida) {
                    Toast.makeText(context, "Horario no válido para este día", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                db.collection("reservas")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("dia", diaVal)
                    .get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            Toast.makeText(context, "Ya tienes una reserva para este día", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        db.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                val nombreUsuario = userDoc.getString("nombre") ?: user.email ?: "Usuario"
                                val sdf = SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault())
                                val horaActual = sdf.format(Date())
                                val reservaMap = hashMapOf(
                                    "userId" to userId,
                                    "creador" to nombreUsuario,
                                    "dia" to diaVal,
                                    "hora" to btnHoraText,
                                    "lugar" to lugarVal,
                                    "registradoEn" to horaActual
                                )
                                db.collection("reservas").add(reservaMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Reserva registrada como $nombreUsuario ✅", Toast.LENGTH_SHORT).show()
                                        partido.horaSeleccionada = btnHoraText
                                        reservasPorPartido[clavePartido] = btnHoraText
                                        notifyItemChanged(position)
                                    }
                            }
                    }
            }
        }

        if (!reservasCargadas) cargarReservasUsuario()
    }

    private fun cargarReservasUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("reservas")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    val dia = doc.getString("dia") ?: continue
                    val hora = doc.getString("hora") ?: continue
                    val lugar = doc.getString("lugar") ?: continue
                    reservasPorPartido["${dia}_$lugar"] = hora
                }
                reservasCargadas = true
                notifyDataSetChanged()
            }
    }

    override fun getItemCount() = listaPartidos.size
}
