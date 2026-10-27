package com.example.escotech

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.escotech.databinding.ActivityChallengeBinding
import com.example.escotech.model.Challenge
import com.example.escotech.model.Submission
import com.example.escotech.util.Niveis
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.example.escotech.util.textoDificuldade
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

/**
 * Mostra a pergunta de um desafio com 4 opções.
 * Ao confirmar, guarda a resposta em "submissions" e soma o XP ao aluno.
 * Cada aluno só pode responder uma vez a cada desafio.
 */
class ChallengeActivity : AppCompatActivity() {

    companion object {
        // Nome do "extra" usado para passar o id do desafio no Intent
        const val EXTRA_CHALLENGE_ID = "challengeId"
    }

    private lateinit var binding: ActivityChallengeBinding
    private lateinit var opcoes: List<RadioButton>

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var desafio: Challenge? = null
    private var jaRespondido = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityChallengeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Guardar os 4 RadioButtons numa lista: a posição na lista = índice da opção
        opcoes = listOf(binding.rbOpcao0, binding.rbOpcao1, binding.rbOpcao2, binding.rbOpcao3)

        binding.btnConfirmar.setOnClickListener {
            if (jaRespondido) finish() else confirmarResposta()
        }

        val challengeId = intent.getStringExtra(EXTRA_CHALLENGE_ID)
        if (challengeId == null) {
            finish()
            return
        }
        carregarDesafio(challengeId)
    }

    /** Lê o desafio e verifica se o aluno já tinha respondido. */
    private fun carregarDesafio(challengeId: String) {
        val uid = auth.currentUser?.uid ?: return

        val tarefaDesafio = db.collection("challenges").document(challengeId).get()
        // O id da resposta é "uid_challengeId", por isso cada aluno só tem uma por desafio
        val tarefaResposta = db.collection("submissions").document("${uid}_$challengeId").get()

        Tasks.whenAllSuccess<DocumentSnapshot>(tarefaDesafio, tarefaResposta)
            .addOnSuccessListener { resultados ->
                val d = resultados[0].toObject(Challenge::class.java)
                if (d == null) {
                    Toast.makeText(this, "Este desafio já não existe.", Toast.LENGTH_LONG).show()
                    finish()
                    return@addOnSuccessListener
                }
                desafio = d
                mostrarDesafio(d)

                // Se já respondeu, mostra logo o resultado anterior
                val respostaAnterior = resultados[1].toObject(Submission::class.java)
                if (respostaAnterior != null) {
                    mostrarResultado(
                        acertou = respostaAnterior.isCorrect,
                        indiceEscolhido = null,
                        titulo = if (respostaAnterior.isCorrect) "✓ Já acertaste este desafio" else "✗ Já respondeste a este desafio",
                        texto = "Ganhaste ${respostaAnterior.xpEarned} XP. A resposta certa está a verde."
                    )
                }

                binding.progresso.visibility = View.GONE
                binding.conteudo.visibility = View.VISIBLE
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao carregar o desafio: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun mostrarDesafio(d: Challenge) {
        binding.toolbar.title = d.title
        binding.tvTitulo.text = d.title
        binding.tvDificuldade.text = textoDificuldade(d.difficulty)
        binding.tvXp.text = "+${d.xpReward} XP"
        binding.tvDescricao.text = d.description
        binding.tvPergunta.text = d.question

        // Mostrar o texto de cada opção (e esconder as que faltarem)
        for (i in opcoes.indices) {
            if (i < d.options.size) {
                opcoes[i].text = d.options[i]
                opcoes[i].visibility = View.VISIBLE
            } else {
                opcoes[i].visibility = View.GONE
            }
        }
    }

    private fun confirmarResposta() {
        val d = desafio ?: return
        val uid = auth.currentUser?.uid ?: return

        // Descobrir qual opção foi escolhida
        val indiceEscolhido = opcoes.indexOfFirst { it.isChecked }
        if (indiceEscolhido == -1) {
            Toast.makeText(this, "Escolhe uma opção primeiro.", Toast.LENGTH_SHORT).show()
            return
        }

        val acertou = indiceEscolhido == d.correctAnswer
        val xpGanho = if (acertou) d.xpReward else 0

        binding.btnConfirmar.isEnabled = false

        val refUtilizador = db.collection("users").document(uid)
        val refResposta = db.collection("submissions").document("${uid}_${d.id}")

        // Uma transação garante que a resposta e o XP são gravados juntos
        // (ou nenhum dos dois, se algo falhar).
        db.runTransaction { transacao ->
            if (transacao.get(refResposta).exists()) {
                throw FirebaseFirestoreException("Já respondeste a este desafio.", FirebaseFirestoreException.Code.ABORTED)
            }
            val xpAtual = transacao.get(refUtilizador).getLong("xp")?.toInt() ?: 0
            val novoXp = xpAtual + xpGanho

            val resposta = hashMapOf(
                "userId" to uid,
                "challengeId" to d.id,
                "isCorrect" to acertou,
                "xpEarned" to xpGanho,
                "completedAt" to Timestamp.now()
            )
            transacao.set(refResposta, resposta)
            transacao.update(refUtilizador, mapOf(
                "xp" to novoXp,
                "level" to Niveis.calcularNivel(novoXp)
            ))

            novoXp // valor devolvido pela transação
        }.addOnSuccessListener { novoXp ->
            val subiuDeNivel = Niveis.calcularNivel(novoXp) > Niveis.calcularNivel(novoXp - xpGanho)
            val texto = when {
                subiuDeNivel -> "Subiste para o nível ${Niveis.calcularNivel(novoXp)}! Tens agora $novoXp XP."
                acertou -> "Tens agora $novoXp XP."
                else -> "A resposta certa está a verde. Não desistas!"
            }
            mostrarResultado(
                acertou = acertou,
                indiceEscolhido = indiceEscolhido,
                titulo = if (acertou) "🎉 Certo! +$xpGanho XP" else "😕 Resposta errada",
                texto = texto
            )
        }.addOnFailureListener { erro ->
            binding.btnConfirmar.isEnabled = true
            Toast.makeText(this, "Erro ao guardar a resposta: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Pinta a opção certa de verde (e a escolhida de vermelho, se estiver errada),
     * bloqueia as opções e mostra o cartão com o resultado.
     */
    private fun mostrarResultado(acertou: Boolean, indiceEscolhido: Int?, titulo: String, texto: String) {
        val d = desafio ?: return
        jaRespondido = true

        for ((i, opcao) in opcoes.withIndex()) {
            opcao.isEnabled = false
            when (i) {
                d.correctAnswer -> opcao.setBackgroundResource(R.drawable.bg_opcao_certa)
                indiceEscolhido -> opcao.setBackgroundResource(R.drawable.bg_opcao_errada)
            }
            // Manter o texto legível mesmo com a opção desativada
            opcao.setTextColor(ContextCompat.getColor(this, R.color.texto))
        }

        val cor = if (acertou) R.color.sucesso else R.color.erro
        binding.tvResultadoTitulo.text = titulo
        binding.tvResultadoTitulo.setTextColor(ContextCompat.getColor(this, cor))
        binding.tvResultadoTexto.text = texto
        binding.cartaoResultado.visibility = View.VISIBLE

        binding.btnConfirmar.text = getString(R.string.voltar)
        binding.btnConfirmar.isEnabled = true
    }
}
