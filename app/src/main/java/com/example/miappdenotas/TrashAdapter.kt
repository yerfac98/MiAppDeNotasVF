package com.example.miappdenotas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.miappdenotas.model.Nota
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrashAdapter : ListAdapter<Nota, TrashAdapter.TrashHolder>(DiffCallback()) {

    private var listener: OnTrashItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return TrashHolder(itemView)
    }

    override fun onBindViewHolder(holder: TrashHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrashHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.text_view_title)
        private val textViewContent: TextView = itemView.findViewById(R.id.text_view_content)
        private val textViewDate: TextView = itemView.findViewById(R.id.text_view_date)
        private val textViewFavorite: TextView = itemView.findViewById(R.id.text_view_favorite)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onTrashItemClick(getItem(position))
                }
            }
        }

        fun bind(nota: Nota) {
            textViewTitle.text = nota.titulo
            textViewFavorite.text = "🗑"

            textViewContent.text = if (nota.contenido.length > 35) {
                nota.contenido.substring(0, 35) + "..."
            } else {
                nota.contenido
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            textViewDate.text = dateFormat.format(Date(nota.fecha))
        }
    }

    interface OnTrashItemClickListener {
        fun onTrashItemClick(nota: Nota)
    }

    fun setOnTrashItemClickListener(listener: OnTrashItemClickListener) {
        this.listener = listener
    }

    class DiffCallback : DiffUtil.ItemCallback<Nota>() {
        override fun areItemsTheSame(oldItem: Nota, newItem: Nota): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Nota, newItem: Nota): Boolean {
            return oldItem == newItem
        }
    }
}