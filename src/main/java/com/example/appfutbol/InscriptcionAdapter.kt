package com.example.appfutbol

import InscripcionItem
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Inscripcion

class InscripcionAdapter(private val items: List<InscripcionItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER_DIA = 0
        private const val TYPE_HEADER_HORA = 1
        private const val TYPE_ITEM = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is InscripcionItem.HeaderDia -> TYPE_HEADER_DIA
            is InscripcionItem.HeaderHora -> TYPE_HEADER_HORA
            is InscripcionItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            TYPE_HEADER_DIA -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header_dia, parent, false)
                HeaderDiaViewHolder(view)
            }

            TYPE_HEADER_HORA -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header_hora, parent, false)
                HeaderHoraViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_inscripcion, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is HeaderDiaViewHolder -> {
                val header = items[position] as InscripcionItem.HeaderDia
                holder.tvHeaderDia.text = header.dia
            }

            is HeaderHoraViewHolder -> {
                val header = items[position] as InscripcionItem.HeaderHora
                holder.tvHeaderHora.text = header.hora
            }

            is ItemViewHolder -> {
                val insItem = items[position] as InscripcionItem.Item
                val ins = insItem.inscripcion

                val displayName = ins.nombre ?: ins.usuarioId ?: "Sin nombre"
                val displayCategoria = ins.categoriaEdad ?: ""

                holder.tvNumber.text = insItem.index?.toString() ?: ""
                holder.tvUsuario.text = displayName
                holder.tvCategoria.text = displayCategoria

                if (displayCategoria == "mayor_60") {
                    holder.itemView.setBackgroundColor(Color.BLACK)
                    holder.tvUsuario.setTextColor(Color.WHITE)
                    holder.tvCategoria.setTextColor(Color.WHITE)
                    holder.tvNumber.setTextColor(Color.WHITE)
                } else {
                    holder.itemView.setBackgroundColor(Color.DKGRAY)
                    holder.tvUsuario.setTextColor(Color.WHITE)
                    holder.tvCategoria.setTextColor(Color.WHITE)
                    holder.tvNumber.setTextColor(Color.WHITE)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolders
    inner class HeaderDiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeaderDia: TextView = itemView.findViewById(R.id.tvHeaderDia)
    }

    inner class HeaderHoraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeaderHora: TextView = itemView.findViewById(R.id.tvHeaderHora)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
    }
}
