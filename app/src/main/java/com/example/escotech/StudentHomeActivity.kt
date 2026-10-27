package com.example.escotech

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escotech.adapter.ChallengeAdapter
import com.example.escotech.adapter.ChallengeItem
import com.example.escotech.databinding.ActivityStudentHomeBinding
import com.example.escotech.model.Assignment
import com.example.escotech.model.Challenge
import com.example.escotech.model.Submission
import com.example.escotech.model.User
import com.example.escotech.util.Avatares
import com.example.escotech.util.Niveis
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

/**
 * Ecrã inicial do aluno: saudação, barra de XP e lista de desafios.
 */
class StudentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentHomeBinding
    private lateinit var adapter: ChallengeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        // Lista de desafios: ao tocar num desafio abre o ChallengeActivity
        adapter = ChallengeAdapter(emptyList()) { desafio ->
            val intent = Intent(this, ChallengeActivity::class.java)
            intent.putExtra(ChallengeActivity.EXTRA_CHALLENGE_ID, desafio.id)
            startActivity(intent)
        }
        binding.rvDesafios.layoutManager = LinearLayoutManager(this)
        binding.rvDesafios.adapter = adapter

        binding.btnRanking.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
        }
        binding.btnSair.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // onResume corre sempre que voltamos a este ecrã (ex.: depois de responder
    // a um desafio), por isso o XP e a lista ficam sempre atualizados.
    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    private fun carregarDados() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { documento ->
                val aluno = documento.toObject(User::class.java) ?: return@addOnSuccessListener
                mostrarCabecalho(aluno)
                carregarDesafios(aluno)
            }
            .addOnFailureListener { erro ->
                binding.progresso.visibility = View.GONE
                Toast.makeText(this, "Erro ao carregar o perfil: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /** Preenche a saudação, o nível e a barra de progresso de XP. */
    private fun mostrarCabecalho(aluno: User) {
        val primeiroNome = aluno.name.split(" ").first()
        val nivel = Niveis.calcularNivel(aluno.xp)
        val xpNoNivel = Niveis.xpNoNivelAtual(aluno.xp)

        binding.tvAvatar.text = Avatares.emoji(aluno.avatarId)
        binding.tvSaudacao.text = "Olá, $primeiroNome! 👋"
        binding.tvNivel.text = "Nível $nivel · Turma ${aluno.classId}"
        binding.barraXp.max = Niveis.XP_POR_NIVEL
        binding.barraXp.setProgressCompat(xpNoNivel, true)
        binding.tvXp.text = "$xpNoNivel / ${Niveis.XP_POR_NIVEL} XP até ao nível ${nivel + 1} · ${aluno.xp} XP no total"
    }

    /**
     * Lê três coleções ao mesmo tempo (desafios, respostas do aluno e
     * atribuições da turma) e junta tudo numa só lista.
     */
    private fun carregarDesafios(aluno: User) {
        val tarefaDesafios = db.collection("challenges").get()
        val tarefaRespostas = db.collection("submissions").whereEqualTo("userId", aluno.id).get()
        val tarefaAtribuicoes = db.collection("assignments").whereEqualTo("classId", aluno.classId).get()

        // whenAllSuccess espera que as três leituras terminem
        Tasks.whenAllSuccess<QuerySnapshot>(tarefaDesafios, tarefaRespostas, tarefaAtribuicoes)
            .addOnSuccessListener { resultados ->
                val desafios = resultados[0].toObjects(Challenge::class.java)
                val respostas = resultados[1].toObjects(Submission::class.java)
                val atribuicoes = resultados[2].toObjects(Assignment::class.java)

                val itens = desafios.map { desafio ->
                    // Resposta do aluno a este desafio (se existir)
                    val resposta = respostas.find { it.challengeId == desafio.id }
                    // Prazo mais próximo, se o desafio foi atribuído à turma
                    val prazo = atribuicoes
                        .filter { it.challengeId == desafio.id && it.deadline != null }
                        .minByOrNull { it.deadline!! }
                        ?.deadline
                    ChallengeItem(desafio, resposta, prazo)
                }

                // Ordem: primeiro os que faltam fazer (os com prazo à frente), depois os feitos
                val ordenados = itens.sortedWith(
                    compareBy<ChallengeItem> { it.submission != null }
                        .thenBy { it.deadline == null }
                        .thenBy { it.deadline }
                )

                binding.progresso.visibility = View.GONE
                binding.tvVazio.visibility = if (ordenados.isEmpty()) View.VISIBLE else View.GONE
                adapter.atualizar(ordenados)
            }
            .addOnFailureListener { erro ->
                binding.progresso.visibility = View.GONE
                Toast.makeText(this, "Erro ao carregar desafios: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
