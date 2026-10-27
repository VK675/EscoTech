package com.example.escotech

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.escotech.adapter.AvatarAdapter
import com.example.escotech.databinding.ActivityStudentProfileBinding
import com.example.escotech.model.Submission
import com.example.escotech.model.User
import com.example.escotech.util.Avatares
import com.example.escotech.util.Niveis
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Perfil do aluno: avatar, nível, XP, desafios feitos e dias seguidos (streak).
 */
class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvAvatar.setOnClickListener { escolherAvatar() }

        binding.btnSair.setOnClickListener {
            auth.signOut()
            // Volta ao login e apaga o histórico de ecrãs
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        carregarPerfil()
    }

    private fun carregarPerfil() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { documento ->
                val aluno = documento.toObject(User::class.java) ?: return@addOnSuccessListener
                binding.tvAvatar.text = Avatares.emoji(aluno.avatarId)
                binding.tvNome.text = aluno.name
                binding.tvEmailTurma.text = "${aluno.email} · Turma ${aluno.classId}"
                binding.tvNivel.text = Niveis.calcularNivel(aluno.xp).toString()
                binding.tvXp.text = aluno.xp.toString()
            }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao carregar o perfil: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }

        // Respostas do aluno: servem para contar desafios feitos e calcular a sequência
        db.collection("submissions").whereEqualTo("userId", uid).get()
            .addOnSuccessListener { resultado ->
                val respostas = resultado.toObjects(Submission::class.java)
                val certas = respostas.count { it.isCorrect }

                binding.tvConcluidos.text = respostas.size.toString()
                binding.tvConcluidosDetalhe.text = "Desafios feitos\n($certas certos)"
                binding.tvSequencia.text = "🔥 ${calcularSequencia(respostas)}"
            }
    }

    /**
     * Conta quantos dias seguidos o aluno respondeu a pelo menos um desafio,
     * a terminar hoje (ou ontem, para a sequência não se perder logo de manhã).
     */
    private fun calcularSequencia(respostas: List<Submission>): Int {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Conjunto com os dias (em texto) em que houve respostas
        val diasComResposta = respostas
            .mapNotNull { it.completedAt?.toDate() }
            .map { formato.format(it) }
            .toSet()

        val dia = Calendar.getInstance()
        // Se hoje ainda não respondeu, começa a contar a partir de ontem
        if (formato.format(dia.time) !in diasComResposta) {
            dia.add(Calendar.DAY_OF_YEAR, -1)
        }

        var sequencia = 0
        while (formato.format(dia.time) in diasComResposta) {
            sequencia++
            dia.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sequencia
    }

    /** Abre uma janela com a grelha de avatares e guarda o escolhido no Firestore. */
    private fun escolherAvatar() {
        val uid = auth.currentUser?.uid ?: return

        val grelha = RecyclerView(this)
        grelha.layoutManager = GridLayoutManager(this, 4)
        val margem = (16 * resources.displayMetrics.density).toInt()
        grelha.setPadding(margem, margem, margem, 0)

        val janela = AlertDialog.Builder(this)
            .setTitle(R.string.escolhe_avatar)
            .setView(grelha)
            .setNegativeButton("Cancelar", null)
            .create()

        grelha.adapter = AvatarAdapter { avatarId ->
            janela.dismiss()
            binding.tvAvatar.text = Avatares.emoji(avatarId)
            db.collection("users").document(uid).update("avatarId", avatarId)
                .addOnFailureListener { erro ->
                    Toast.makeText(this, "Não foi possível guardar: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
        janela.show()
    }
}
