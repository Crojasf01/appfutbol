package com.example.appfutbol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Partido
import java.text.SimpleDateFormat
import java.util.*

class PartidoAdapter(private val listaPartidos: List<Partido>) :
    RecyclerView.Adapter<PartidoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)   // derecha
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)       // abajo izquierda
        val tvDia: TextView = itemView.findViewById(R.id.tvDia)         // grande arriba izq
        val tvMes: TextView = itemView.findViewById(R.id.tvMes)         // subtítulo izq

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partido = listaPartidos[position]

        // 🔹 Título (lado derecho)
        holder.tvTitulo.text = partido.titulo ?: "Sin título"

        // 🔹 Fecha (lado izquierdo)
        partido.fechaPartido?.toDate()?.let { fechaDate ->

            // Día (ejemplo: 20)
            val formatoDia = SimpleDateFormat("dd", Locale.getDefault())
            holder.tvDia.text = formatoDia.format(fechaDate)

            // Mes abreviado (ejemplo: ago, sep, dic)
            val formatoMes = SimpleDateFormat("MMM", Locale("es"))
            holder.tvMes.text = formatoMes.format(fechaDate)

            // Hora (ejemplo: 19:50)
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.tvHora.text = formatoHora.format(fechaDate)
        }
    }


    override fun getItemCount() = listaPartidos.size
}
