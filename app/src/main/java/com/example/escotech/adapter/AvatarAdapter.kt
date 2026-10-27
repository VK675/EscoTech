package com.example.escotech.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.escotech.databinding.ItemAvatarBinding
import com.example.escotech.util.Avatares

/**
 * Grelha de avatares para o aluno escolher.
 * Devolve a posição escolhida (que é o avatarId).
 */
class AvatarAdapter(
    private val aoEscolher: (Int) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAvatarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvEmoji.text = Avatares.LISTA[position]
        holder.binding.root.setOnClickListener { aoEscolher(holder.bindingAdapterPosition) }
    }

    override fun getItemCount(): Int = Avatares.LISTA.size
}
