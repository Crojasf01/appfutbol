package com.example.appfutbol.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.R

class PartidosAdminAdapter(
    private val lista: List<PartidoAdmin>,
    private val onEditClick: (PartidoAdmin) -> Unit,
    private val onDeleteClick: (PartidoAdmin) -> Unit
) : RecyclerView.Adapter<PartidosAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDia: TextView = view.findViewById(R.id.txtDia)
        val txtHora: TextView = view.findViewById(R.id.txtHora)
        val txtLugar: TextView = view.findViewById(R.id.txtLugar)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partido = lista[position]
        holder.txtDia.text = partido.dia
        holder.txtHora.text = partido.hora
        holder.txtLugar.text = partido.lugar

        holder.btnEditar.setOnClickListener { onEditClick(partido) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(partido) }
    }

    override fun getItemCount(): Int = lista.size
}
