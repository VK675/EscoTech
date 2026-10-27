package com.example.escotech.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.escotech.R
import com.example.escotech.databinding.ItemChallengeBinding
import com.example.escotech.model.Challenge
import com.example.escotech.model.Submission
import com.example.escotech.util.formatarData
import com.example.escotech.util.textoDificuldade
import com.google.firebase.Timestamp

/**
 * Um desafio pronto a mostrar na lista, com a informação extra do aluno:
 * - submission: a resposta do aluno (null se ainda não respondeu)
 * - deadline: prazo, se o desafio foi atribuído à turma do aluno
 */
data class ChallengeItem(
    val challenge: Challenge,
    val submission: Submission? = null,
    val deadline: Timestamp? = null
)

/**
 * Adapter da lista de desafios. É usado no ecrã do aluno e no painel do professor.
 *
 * @param aoClicar função chamada quando se toca num desafio (pode ser null)
 */
class ChallengeAdapter(
    private var itens: List<ChallengeItem>,
    private val aoClicar: ((Challenge) -> Unit)?
) : RecyclerView.Adapter<ChallengeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        val desafio = item.challenge
        val b = holder.binding
        val contexto = b.root.context

        b.tvTitulo.text = desafio.title
        b.tvDescricao.text = desafio.description
        b.tvXp.text = "+${desafio.xpReward} XP"
        b.tvDificuldade.text = textoDificuldade(desafio.difficulty)

        // Etiqueta de estado: concluído, errado, prazo ou nada
        val submissao = item.submission
        when {
            submissao != null && submissao.isCorrect -> {
                b.tvEstado.visibility = View.VISIBLE
                b.tvEstado.text = "✓ Certo"
                b.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.sucesso))
            }
            submissao != null -> {
                b.tvEstado.visibility = View.VISIBLE
                b.tvEstado.text = "✗ Errado"
                b.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.erro))
            }
            item.deadline != null -> {
                b.tvEstado.visibility = View.VISIBLE
                val terminou = item.deadline.toDate().before(java.util.Date())
                b.tvEstado.text = if (terminou) {
                    "Prazo terminou: ${formatarData(item.deadline)}"
                } else {
                    "Prazo: ${formatarData(item.deadline)}"
                }
                val cor = if (terminou) R.color.erro else R.color.primaria
                b.tvEstado.setTextColor(ContextCompat.getColor(contexto, cor))
            }
            else -> b.tvEstado.visibility = View.GONE
        }

        if (aoClicar != null) {
            b.root.setOnClickListener { aoClicar.invoke(desafio) }
        } else {
            b.root.setOnClickListener(null)
            b.root.isClickable = false
        }
    }

    override fun getItemCount(): Int = itens.size

    /** Troca os dados da lista e redesenha-a. */
    fun atualizar(novosItens: List<ChallengeItem>) {
        itens = novosItens
        notifyDataSetChanged()
    }
}
