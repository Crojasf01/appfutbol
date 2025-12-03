package com.example.appfutbol

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class AdminFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        val btnAbrirPartido = view.findViewById<Button>(R.id.btnAbrirPartido)
        val btnCerrarPartido = view.findViewById<Button>(R.id.btnCerrarPartido)
        val btnAbrirDiaCompleto = view.findViewById<Button>(R.id.btnAbrirDiaCompleto)
        val btnCerrarDiaCompleto = view.findViewById<Button>(R.id.btnCerrarDiaCompleto)

        btnAbrirPartido.setOnClickListener { mostrarDialogoPartido(true) }
        btnCerrarPartido.setOnClickListener { mostrarDialogoPartido(false) }

        btnAbrirDiaCompleto.setOnClickListener { mostrarDialogoDiaCompleto(true) }
        btnCerrarDiaCompleto.setOnClickListener { mostrarDialogoDiaCompleto(false) }

        return view
    }

    // 🔹 Dialogo para abrir/cerrar un partido específico
    private fun mostrarDialogoPartido(habilitar: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (habilitar) "Abrir Partido" else "Cerrar Partido")

        val dialogView = layoutInflater.inflate(R.layout.dialog_crear_partido, null)
        val spinnerLugar = dialogView.findViewById<Spinner>(R.id.spinnerLugar)
        val spinnerDia = dialogView.findViewById<Spinner>(R.id.spinnerDia)
        val spinnerHora = dialogView.findViewById<Spinner>(R.id.spinnerHora)

        builder.setView(dialogView)

        val lugares = listOf("Surco")
        spinnerLugar.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lugares)

        val dias = listOf("Martes", "Jueves", "Sábado", "Domingo", "Feriado")
        spinnerDia.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dias)

        val horariosSemana = listOf("2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM", "7:00 PM")
        val horariosFinSemana = listOf("7:00 AM", "8:00 AM", "9:00 AM")

        spinnerDia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val diaSeleccionado = dias[position]
                val horarios = when (diaSeleccionado) {
                    "Martes", "Jueves", "Feriado" -> horariosSemana
                    "Sábado", "Domingo" -> horariosFinSemana
                    else -> emptyList()
                }
                spinnerHora.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, horarios)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        builder.setPositiveButton(if (habilitar) "Abrir" else "Cerrar") { _, _ ->
            val lugar = spinnerLugar.selectedItem?.toString() ?: ""
            val dia = spinnerDia.selectedItem?.toString() ?: ""
            val hora = spinnerHora.selectedItem?.toString() ?: ""

            verificarYActualizarEstado(dia, hora, lugar, habilitar)
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    // 🔹 Dialogo para abrir/cerrar todos los partidos de un día
    private fun mostrarDialogoDiaCompleto(habilitar: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (habilitar) "Abrir Día Completo" else "Cerrar Día Completo")

        val dialogView = layoutInflater.inflate(R.layout.dialog_dia_completo, null)
        val spinnerLugar = dialogView.findViewById<Spinner>(R.id.spinnerLugar)
        val spinnerDia = dialogView.findViewById<Spinner>(R.id.spinnerDia)

        builder.setView(dialogView)

        val lugares = listOf("Surco")
        spinnerLugar.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lugares)

        val dias = listOf("Martes", "Jueves", "Sábado", "Domingo", "Feriado")
        spinnerDia.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dias)

        builder.setPositiveButton(if (habilitar) "Abrir Día" else "Cerrar Día") { _, _ ->
            val lugar = spinnerLugar.selectedItem?.toString() ?: ""
            val dia = spinnerDia.selectedItem?.toString() ?: ""

            abrirTodosLosPartidosDelDia(dia, lugar, habilitar)
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    // 🔹 Abre/cierra todos los partidos del día seleccionado
    private fun abrirTodosLosPartidosDelDia(dia: String, lugar: String, habilitar: Boolean) {
        val horarios = when (dia) {
            "Martes", "Jueves", "Feriado" -> listOf("2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM", "7:00 PM")
            "Sábado", "Domingo" -> listOf("7:00 AM", "8:00 AM", "9:00 AM")
            else -> emptyList()
        }

        for (hora in horarios) {
            verificarYActualizarEstado(dia, hora, lugar, habilitar)
        }

        val msg = if (habilitar)
            "✅ Todos los partidos de $dia han sido ABIERTOS"
        else
            "🚫 Todos los partidos de $dia han sido CERRADOS"

        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    // 🔹 Verifica y actualiza el estado de un partido en Firestore
    private fun verificarYActualizarEstado(dia: String, hora: String, lugar: String, habilitar: Boolean) {
        if (dia.isEmpty() || hora.isEmpty() || lugar.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione día, hora y lugar", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("partido_1")
            .whereEqualTo("dia", dia)
            .whereEqualTo("hora", hora)
            .whereEqualTo("lugar", lugar)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val partido = snapshot.documents[0]
                    val estadoActual = partido.getBoolean("habilitado") ?: false

                    if (habilitar && estadoActual) {
                        Toast.makeText(requireContext(), "⚠️ Este partido ya está ABIERTO", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    } else if (!habilitar && !estadoActual) {
                        Toast.makeText(requireContext(), "ℹ️ Este partido ya está CERRADO", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    db.collection("partido_1").document(partido.id)
                        .update("habilitado", habilitar)
                        .addOnSuccessListener {
                            val msg = if (habilitar) "✅ Partido ABIERTO correctamente" else "🚫 Partido CERRADO correctamente"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "❌ Error al actualizar estado", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    val nuevoPartido = hashMapOf(
                        "dia" to dia,
                        "hora" to hora,
                        "lugar" to lugar,
                        "habilitado" to habilitar,
                        "creador" to "admin"
                    )

                    db.collection("partido_1").add(nuevoPartido)
                        .addOnSuccessListener {
                            val msg = if (habilitar) "✅ Partido creado y ABIERTO" else "🚫 Partido creado y CERRADO"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "❌ Error al crear partido", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Error al consultar la base de datos", Toast.LENGTH_SHORT).show()
            }
    }
}
