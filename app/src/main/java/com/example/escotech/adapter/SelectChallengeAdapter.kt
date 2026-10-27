package com.example.escotech.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.escotech.R
import com.example.escotech.databinding.ItemChallengeBinding
import com.example.escotech.model.Challenge
import com.example.escotech.util.textoDificuldade

/**
 * Lista de desafios onde se pode escolher UM (usada para atribuir a uma turma).
 * Reaproveita o layout item_challenge.xml e marca o escolhido com um contorno.
 */
class SelectChallengeAdapter(
    private var desafios: List<Challenge>
) : RecyclerView.Adapter<SelectChallengeAdapter.ViewHolder>() {

    // Posição do desafio escolhido (-1 = nenhum)
    private var posicaoEscolhida = -1

    class ViewHolder(val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val desafio = desafios[position]
        val b = holder.binding

        b.tvTitulo.text = desafio.title
        b.tvDescricao.text = desafio.description
        b.tvXp.text = "+${desafio.xpReward} XP"
        b.tvDificuldade.text = textoDificuldade(desafio.difficulty)

        val escolhido = position == posicaoEscolhida
        b.tvEstado.visibility = if (escolhido) View.VISIBLE else View.GONE
        b.tvEstado.text = "✓ Escolhido"
        b.tvEstado.setTextColor(ContextCompat.getColor(b.root.context, R.color.secundaria))

        // Contorno azul de 3dp no cartão escolhido
        val tresDp = (3 * b.root.resources.displayMetrics.density).toInt()
        b.root.strokeColor = ContextCompat.getColor(b.root.context, R.color.secundaria)
        b.root.strokeWidth = if (escolhido) tresDp else 0

        b.root.setOnClickListener {
            val anterior = posicaoEscolhida
            posicaoEscolhida = holder.bindingAdapterPosition
            // Redesenhar só os dois cartões que mudaram
            if (anterior != -1) notifyItemChanged(anterior)
            notifyItemChanged(posicaoEscolhida)
        }
    }

    override fun getItemCount(): Int = desafios.size

    /** Devolve o desafio escolhido, ou null se ainda não escolheu nenhum. */
    fun desafioEscolhido(): Challenge? = desafios.getOrNull(posicaoEscolhida)

    fun atualizar(novosDesafios: List<Challenge>) {
        desafios = novosDesafios
        posicaoEscolhida = -1
        notifyDataSetChanged()
    }
}
