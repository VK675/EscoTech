package com.example.escotech.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.escotech.databinding.ItemLeaderboardBinding
import com.example.escotech.model.User
import com.example.escotech.util.Avatares
import com.example.escotech.util.Niveis

/**
 * Adapter do ranking. A lista já vem ordenada por XP (do maior para o menor).
 *
 * @param uidAtual id do aluno com sessão iniciada, para destacar a sua linha
 */
class LeaderboardAdapter(
    private var alunos: List<User>,
    private val uidAtual: String?
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val aluno = alunos[position]
        val b = holder.binding

        // Medalhas para os 3 primeiros, número para os restantes
        b.tvPosicao.text = when (position) {
            0 -> "🥇"
            1 -> "🥈"
            2 -> "🥉"
            else -> "${position + 1}º"
        }
        b.tvAvatar.text = Avatares.emoji(aluno.avatarId)

        val eu = aluno.id == uidAtual
        b.tvNome.text = if (eu) "${aluno.name} (tu)" else aluno.name
        b.tvDetalhe.text = "Nível ${Niveis.calcularNivel(aluno.xp)} · ${aluno.classId}"
        b.tvXp.text = "${aluno.xp} XP"

        // Contorno azul (2dp) à volta da linha do próprio aluno
        val doisDp = (2 * b.root.resources.displayMetrics.density).toInt()
        b.cartao.strokeWidth = if (eu) doisDp else 0
    }

    override fun getItemCount(): Int = alunos.size

    fun atualizar(novosAlunos: List<User>) {
        alunos = novosAlunos
        notifyDataSetChanged()
    }
}
