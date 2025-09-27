package com.example.appfutbol

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.appfutbol.data.model.Inscripcion
import com.example.appfutbol.data.model.InscripcionItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp


class InscripcionAdapter(private val items: List<InscripcionItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeader: TextView = itemView.findViewById(R.id.tvHeaderDia)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is InscripcionItem.Header -> TYPE_HEADER
            is InscripcionItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header_dia, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_inscripcion, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is InscripcionItem.Header) {
            holder.tvHeader.text = item.dia
        } else if (holder is ItemViewHolder && item is InscripcionItem.Item) {
            // hora ya es String, usar directamente
            val horaStr = item.inscripcion.hora ?: "Hora no disponible"

            holder.tvHora.text = horaStr
            holder.tvUsuario.text = item.inscripcion.usuarioId
            holder.tvCategoria.text = item.inscripcion.categoriaEdad

            // Cambiar color según categoría
            if (item.inscripcion.categoriaEdad == "mayor_60") {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2"))
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"))
            }
        }
    }



}
