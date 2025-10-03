package com.example.appfutbol

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfutbol.data.model.Inscripcion

// 🔹 Sealed class con nuevo HeaderPartido
sealed class InscripcionItem {
    data class HeaderPartido(val dia: String, val hora: String) : InscripcionItem()
    data class Item(val inscripcion: Inscripcion, val index: Int?) : InscripcionItem()
}

class InscripcionAdapter(private val items: List<InscripcionItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER_PARTIDO = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is InscripcionItem.HeaderPartido -> TYPE_HEADER_PARTIDO
            is InscripcionItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER_PARTIDO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header_partido, parent, false)
                HeaderPartidoViewHolder(view)
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
            is HeaderPartidoViewHolder -> {
                val header = items[position] as InscripcionItem.HeaderPartido
                holder.tvHeaderPartido.text = "${header.dia} - ${header.hora}"
            }
            is ItemViewHolder -> {
                val insItem = items[position] as InscripcionItem.Item
                val ins = insItem.inscripcion

                // Nombre con fallback
                val displayName = when {
                    !ins.nombre.isNullOrBlank() -> ins.nombre
                    !ins.usuarioId.isNullOrBlank() -> ins.usuarioId
                    else -> "Sin nombre"
                }

                // Categoría con fallback
                val displayCategoria = ins.categoriaEdad ?: ""

                // Número
                holder.tvNumber.text = insItem.index?.toString() ?: ""

                // Asignar valores
                holder.tvUsuario.text = displayName
                holder.tvCategoria.text = displayCategoria

                // Estilo
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

    // 🔹 ViewHolders
    inner class HeaderPartidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHeaderPartido: TextView = itemView.findViewById(R.id.tvHeaderPartido)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
        val tvUsuario: TextView = itemView.findViewById(R.id.tvUsuario)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
    }
}
