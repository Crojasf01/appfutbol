package com.example.appfutbol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Partido

class PartidoAdapter(private val listaPartidos: List<Partido>, activity: LoginActivity) :
    RecyclerView.Adapter<PartidoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvLugar: TextView = itemView.findViewById(R.id.tvLugar)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvCupos: TextView = itemView.findViewById(R.id.tvCupos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partido = listaPartidos[position]
        holder.tvTitulo.text = partido.titulo
        holder.tvLugar.text = partido.lugar
        holder.tvFecha.text = "${partido.fecha} ${partido.hora}"
        holder.tvCupos.text = "Cupos: ${partido.inscritos}/${partido.cupos}"
    }

    override fun getItemCount() = listaPartidos.size
}
