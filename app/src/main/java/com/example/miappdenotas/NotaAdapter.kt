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

class NotaAdapter : ListAdapter<Nota, NotaAdapter.NotaHolder>(DiffCallback()) {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return NotaHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotaHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getNoteAt(position: Int): Nota {
        return getItem(position)
    }

    inner class NotaHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.text_view_title)
        private val textViewContent: TextView = itemView.findViewById(R.id.text_view_content)
        private val textViewDate: TextView = itemView.findViewById(R.id.text_view_date)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(getItem(position))
                }
            }

            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemLongClick(getItem(position))
                    true
                } else {
                    false
                }
            }
        }

        fun bind(nota: Nota) {
            textViewTitle.text = nota.titulo

            textViewContent.text = if (nota.contenido.length > 50) {
                nota.contenido.substring(0, 50) + "..."
            } else {
                nota.contenido
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            textViewDate.text = dateFormat.format(Date(nota.fecha))
        }
    }

    interface OnItemClickListener {
        fun onItemClick(nota: Nota)
        fun onItemLongClick(nota: Nota)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
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