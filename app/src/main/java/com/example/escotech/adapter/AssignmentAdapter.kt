package com.example.escotech.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.escotech.databinding.ItemAssignmentBinding
import com.example.escotech.model.Assignment
import com.example.escotech.util.formatarData

/**
 * Uma atribuição pronta a mostrar, com o título do desafio e
 * quantos alunos da turma já o fizeram.
 */
data class AssignmentItem(
    val assignment: Assignment,
    val tituloDesafio: String,
    val concluidos: Int,
    val totalAlunos: Int
)

/** Adapter da lista de atribuições no painel do professor. */
class AssignmentAdapter(
    private var itens: List<AssignmentItem>
) : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAssignmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        val b = holder.binding

        b.tvTitulo.text = item.tituloDesafio
        b.tvTurmaPrazo.text = "Turma ${item.assignment.classId} · Prazo ${formatarData(item.assignment.deadline)}"
        b.tvConcluidos.text = "${item.concluidos}/${item.totalAlunos}"

        // Percentagem de alunos da turma que já fizeram o desafio
        val percentagem = if (item.totalAlunos > 0) item.concluidos * 100 / item.totalAlunos else 0
        b.barraConclusao.progress = percentagem
    }

    override fun getItemCount(): Int = itens.size

    fun atualizar(novosItens: List<AssignmentItem>) {
        itens = novosItens
        notifyDataSetChanged()
    }
}
