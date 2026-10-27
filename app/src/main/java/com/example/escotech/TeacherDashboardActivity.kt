package com.example.escotech

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escotech.adapter.AssignmentAdapter
import com.example.escotech.adapter.AssignmentItem
import com.example.escotech.adapter.ChallengeAdapter
import com.example.escotech.adapter.ChallengeItem
import com.example.escotech.databinding.ActivityTeacherDashboardBinding
import com.example.escotech.model.Assignment
import com.example.escotech.model.Challenge
import com.example.escotech.model.Submission
import com.example.escotech.model.User
import com.example.escotech.util.DesafiosExemplo
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

/**
 * Painel do professor: estatísticas, atribuições e desafios criados.
 */
class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherDashboardBinding
    private lateinit var adapterAtribuicoes: AssignmentAdapter
    private lateinit var adapterDesafios: ChallengeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityTeacherDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        adapterAtribuicoes = AssignmentAdapter(emptyList())
        binding.rvAtribuicoes.layoutManager = LinearLayoutManager(this)
        binding.rvAtribuicoes.adapter = adapterAtribuicoes

        // No painel os desafios só são mostrados (null = sem ação ao tocar)
        adapterDesafios = ChallengeAdapter(emptyList(), null)
        binding.rvDesafios.layoutManager = LinearLayoutManager(this)
        binding.rvDesafios.adapter = adapterDesafios

        binding.btnNovoDesafio.setOnClickListener {
            startActivity(Intent(this, CreateChallengeActivity::class.java))
        }
        binding.btnAtribuir.setOnClickListener {
            startActivity(Intent(this, AssignChallengeActivity::class.java))
        }
        binding.btnExemplos.setOnClickListener { adicionarDesafiosExemplo() }
        binding.btnSair.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Recarrega sempre que voltamos ao painel (ex.: depois de criar um desafio)
    override fun onResume() {
        super.onResume()
        carregarDados()
    }

    private fun carregarDados() {
        val uid = auth.currentUser?.uid ?: return

        // Nome do professor para a saudação
        db.collection("users").document(uid).get()
            .addOnSuccessListener { documento ->
                val nome = documento.getString("name") ?: ""
                binding.tvSaudacao.text = "Olá, ${nome.split(" ").first()}! 👋"
            }

        // Lemos 4 coleções ao mesmo tempo e calculamos as estatísticas na app
        val tarefaAlunos = db.collection("users").whereEqualTo("role", "student").get()
        val tarefaRespostas = db.collection("submissions").get()
        val tarefaAtribuicoes = db.collection("assignments").whereEqualTo("createdBy", uid).get()
        val tarefaDesafios = db.collection("challenges").get()

        Tasks.whenAllSuccess<QuerySnapshot>(tarefaAlunos, tarefaRespostas, tarefaAtribuicoes, tarefaDesafios)
            .addOnSuccessListener { resultados ->
                val alunos = resultados[0].toObjects(User::class.java)
                val respostas = resultados[1].toObjects(Submission::class.java)
                val atribuicoes = resultados[2].toObjects(Assignment::class.java)
                val desafios = resultados[3].toObjects(Challenge::class.java)

                mostrarEstatisticas(alunos, respostas, atribuicoes)
                mostrarAtribuicoes(alunos, respostas, atribuicoes, desafios)
                mostrarDesafios(desafios.filter { it.createdBy == uid })
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao carregar o painel: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarEstatisticas(
        alunos: List<User>,
        respostas: List<Submission>,
        atribuicoes: List<Assignment>
    ) {
        // Alunos ativos = alunos que responderam a algum desafio nos últimos 7 dias
        val seteDiasAtras = Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
        val ativos = respostas
            .filter { it.completedAt != null && it.completedAt!!.toDate().after(seteDiasAtras) }
            .map { it.userId }
            .toSet()
            .size
        binding.tvAlunosAtivos.text = "$ativos/${alunos.size}"

        binding.tvAtribuicoes.text = atribuicoes.size.toString()

        // Taxa de conclusão = desafios feitos / desafios que deviam ser feitos,
        // contando todos os alunos de cada turma que recebeu uma atribuição
        var esperados = 0
        var feitos = 0
        for (atribuicao in atribuicoes) {
            val alunosDaTurma = alunos.filter { it.classId == atribuicao.classId }
            esperados += alunosDaTurma.size
            feitos += contarConcluidos(alunosDaTurma, respostas, atribuicao.challengeId)
        }
        binding.tvTaxaConclusao.text = if (esperados == 0) "-" else "${feitos * 100 / esperados}%"
    }

    private fun mostrarAtribuicoes(
        alunos: List<User>,
        respostas: List<Submission>,
        atribuicoes: List<Assignment>,
        desafios: List<Challenge>
    ) {
        val itens = atribuicoes
            .sortedByDescending { it.deadline }
            .map { atribuicao ->
                val alunosDaTurma = alunos.filter { it.classId == atribuicao.classId }
                val titulo = desafios.find { it.id == atribuicao.challengeId }?.title ?: "(desafio apagado)"
                AssignmentItem(
                    assignment = atribuicao,
                    tituloDesafio = titulo,
                    concluidos = contarConcluidos(alunosDaTurma, respostas, atribuicao.challengeId),
                    totalAlunos = alunosDaTurma.size
                )
            }

        binding.tvSemAtribuicoes.visibility = if (itens.isEmpty()) View.VISIBLE else View.GONE
        adapterAtribuicoes.atualizar(itens)
    }

    private fun mostrarDesafios(meusDesafios: List<Challenge>) {
        binding.semDesafios.visibility = if (meusDesafios.isEmpty()) View.VISIBLE else View.GONE
        adapterDesafios.atualizar(meusDesafios.map { ChallengeItem(it) })
    }

    /** Quantos destes alunos já responderam ao desafio indicado. */
    private fun contarConcluidos(alunos: List<User>, respostas: List<Submission>, challengeId: String): Int =
        alunos.count { aluno ->
            respostas.any { it.userId == aluno.id && it.challengeId == challengeId }
        }

    /** Grava os desafios de exemplo de uma só vez (WriteBatch). */
    private fun adicionarDesafiosExemplo() {
        val uid = auth.currentUser?.uid ?: return
        binding.btnExemplos.isEnabled = false

        val lote = db.batch()
        for (desafio in DesafiosExemplo.criar(uid)) {
            val novoDocumento = db.collection("challenges").document() // id automático
            lote.set(novoDocumento, desafio)
        }
        lote.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Desafios de exemplo adicionados!", Toast.LENGTH_SHORT).show()
                binding.btnExemplos.isEnabled = true
                carregarDados()
            }
            .addOnFailureListener { erro ->
                binding.btnExemplos.isEnabled = true
                Toast.makeText(this, "Erro: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
