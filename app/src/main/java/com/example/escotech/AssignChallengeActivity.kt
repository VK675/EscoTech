package com.example.escotech

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escotech.adapter.SelectChallengeAdapter
import com.example.escotech.databinding.ActivityAssignChallengeBinding
import com.example.escotech.model.Challenge
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.example.escotech.util.formatarData
import com.example.escotech.util.normalizarTurma
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

/** O professor escolhe um desafio, uma turma e um prazo, e cria a atribuição. */
class AssignChallengeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignChallengeBinding
    private lateinit var adapter: SelectChallengeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Prazo escolhido no calendário (null = ainda não escolheu)
    private var prazo: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityAssignChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = SelectChallengeAdapter(emptyList())
        binding.rvDesafios.layoutManager = LinearLayoutManager(this)
        binding.rvDesafios.adapter = adapter

        binding.btnPrazo.setOnClickListener { escolherPrazo() }
        binding.btnAtribuir.setOnClickListener { atribuir() }

        carregarDesafios()
    }

    /** Mostra todos os desafios (do próprio professor e dos colegas). */
    private fun carregarDesafios() {
        db.collection("challenges").get()
            .addOnSuccessListener { resultado ->
                val desafios = resultado.toObjects(Challenge::class.java).sortedBy { it.title }
                binding.tvVazio.visibility = if (desafios.isEmpty()) View.VISIBLE else View.GONE
                adapter.atualizar(desafios)
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao carregar desafios: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /** Abre um calendário. O prazo fica no fim do dia escolhido (23:59). */
    private fun escolherPrazo() {
        val hoje = Calendar.getInstance()

        val calendario = DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                val data = Calendar.getInstance()
                data.set(ano, mes, dia, 23, 59, 59)
                prazo = Timestamp(data.time)
                binding.btnPrazo.text = "Prazo: ${formatarData(prazo)}"
            },
            hoje.get(Calendar.YEAR),
            hoje.get(Calendar.MONTH),
            hoje.get(Calendar.DAY_OF_MONTH)
        )
        // Não deixa escolher dias que já passaram
        calendario.datePicker.minDate = hoje.timeInMillis
        calendario.show()
    }

    private fun atribuir() {
        val uid = auth.currentUser?.uid ?: return
        val turma = normalizarTurma(binding.etTurma.text.toString())
        val desafio = adapter.desafioEscolhido()

        binding.campoTurma.error = null
        if (turma.isEmpty()) {
            binding.campoTurma.error = "Escreve a turma"
            return
        }
        if (prazo == null) {
            Toast.makeText(this, "Escolhe um prazo.", Toast.LENGTH_SHORT).show()
            return
        }
        if (desafio == null) {
            Toast.makeText(this, "Escolhe um desafio da lista.", Toast.LENGTH_SHORT).show()
            return
        }

        val atribuicao = hashMapOf(
            "challengeId" to desafio.id,
            "classId" to turma,
            "deadline" to prazo,
            "createdBy" to uid
        )

        binding.btnAtribuir.isEnabled = false

        db.collection("assignments").add(atribuicao)
            .addOnSuccessListener {
                Toast.makeText(this, "\"${desafio.title}\" atribuído à turma $turma!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { erro ->
                binding.btnAtribuir.isEnabled = true
                Toast.makeText(this, "Erro ao atribuir: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
