package com.example.escotech

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.escotech.databinding.ActivityCreateChallengeBinding
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Formulário para o professor criar um desafio de escolha múltipla. */
class CreateChallengeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateChallengeBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityCreateChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { guardarDesafio() }
    }

    private fun guardarDesafio() {
        val uid = auth.currentUser?.uid ?: return

        // Pares (campo, caixa de texto) dos campos obrigatórios
        val camposObrigatorios = listOf(
            binding.campoTitulo to binding.etTitulo,
            binding.campoDescricao to binding.etDescricao,
            binding.campoPergunta to binding.etPergunta,
            binding.campoOpcaoA to binding.etOpcaoA,
            binding.campoOpcaoB to binding.etOpcaoB,
            binding.campoOpcaoC to binding.etOpcaoC,
            binding.campoOpcaoD to binding.etOpcaoD
        )
        if (!validarPreenchidos(camposObrigatorios)) return

        val xp = binding.etXp.text.toString().toIntOrNull()
        binding.campoXp.error = null
        if (xp == null || xp <= 0 || xp > 500) {
            binding.campoXp.error = "Escreve um valor entre 1 e 500"
            return
        }

        val opcoes = listOf(
            binding.etOpcaoA.text.toString().trim(),
            binding.etOpcaoB.text.toString().trim(),
            binding.etOpcaoC.text.toString().trim(),
            binding.etOpcaoD.text.toString().trim()
        )

        // O botão escolhido no grupo A/B/C/D dá o índice da resposta certa
        val respostaCerta = when (binding.grupoRespostaCerta.checkedButtonId) {
            binding.btnCertaB.id -> 1
            binding.btnCertaC.id -> 2
            binding.btnCertaD.id -> 3
            else -> 0
        }
        val dificuldade = if (binding.grupoDificuldade.checkedButtonId == binding.btnIntermedio.id) {
            "intermediate"
        } else {
            "beginner"
        }

        val desafio = hashMapOf(
            "title" to binding.etTitulo.text.toString().trim(),
            "description" to binding.etDescricao.text.toString().trim(),
            // A pergunta não leva trim() no início para manter a indentação do código
            "question" to binding.etPergunta.text.toString().trimEnd(),
            "options" to opcoes,
            "correctAnswer" to respostaCerta,
            "xpReward" to xp,
            "difficulty" to dificuldade,
            "createdBy" to uid
        )

        binding.btnGuardar.isEnabled = false

        // add() cria um documento com id automático
        db.collection("challenges").add(desafio)
            .addOnSuccessListener {
                Toast.makeText(this, "Desafio criado!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { erro ->
                binding.btnGuardar.isEnabled = true
                Toast.makeText(this, "Erro ao guardar: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /** Mostra "Campo obrigatório" nos campos vazios. Devolve true se estiver tudo preenchido. */
    private fun validarPreenchidos(campos: List<Pair<TextInputLayout, TextInputEditText>>): Boolean {
        var tudoPreenchido = true
        for ((campo, caixa) in campos) {
            if (caixa.text.toString().isBlank()) {
                campo.error = "Campo obrigatório"
                tudoPreenchido = false
            } else {
                campo.error = null
            }
        }
        return tudoPreenchido
    }
}
