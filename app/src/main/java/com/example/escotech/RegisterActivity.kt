package com.example.escotech

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.escotech.databinding.ActivityRegisterBinding
import com.example.escotech.util.abrirEcraPrincipal
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.example.escotech.util.normalizarTurma
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Registo de um novo utilizador: cria a conta no Firebase Auth
 * e guarda o perfil (nome, papel, XP...) em users/{uid}.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvIrLogin.setOnClickListener { finish() }

        // O campo "Turma" só faz sentido para alunos
        binding.grupoPapel.addOnButtonCheckedListener { _, idBotao, selecionado ->
            if (selecionado) {
                binding.campoTurma.visibility =
                    if (idBotao == binding.btnAluno.id) View.VISIBLE else View.GONE
            }
        }

        binding.btnCriarConta.setOnClickListener { criarConta() }
    }

    private fun criarConta() {
        val nome = binding.etNome.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val eAluno = binding.grupoPapel.checkedButtonId == binding.btnAluno.id
        val role = if (eAluno) "student" else "teacher"
        val turma = if (eAluno) normalizarTurma(binding.etTurma.text.toString()) else ""

        // ----- Validação dos campos -----
        binding.campoNome.error = null
        binding.campoEmail.error = null
        binding.campoPassword.error = null
        binding.campoTurma.error = null

        if (nome.isEmpty()) {
            binding.campoNome.error = "Escreve o teu nome"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.campoEmail.error = "Email inválido"
            return
        }
        if (password.length < 6) {
            binding.campoPassword.error = "A palavra-passe precisa de pelo menos 6 caracteres"
            return
        }
        if (eAluno && turma.isEmpty()) {
            binding.campoTurma.error = "Escreve a tua turma"
            return
        }

        mostrarCarregamento(true)

        // 1) Criar a conta no Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user!!.uid

                // 2) Guardar o perfil no Firestore, com XP a zero e nível 1
                val novoUtilizador = hashMapOf(
                    "name" to nome,
                    "email" to email,
                    "role" to role,
                    "xp" to 0,
                    "level" to 1,
                    "avatarId" to 0,
                    "classId" to turma
                )

                db.collection("users").document(uid).set(novoUtilizador)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Conta criada. Bem-vindo, $nome!", Toast.LENGTH_SHORT).show()
                        abrirEcraPrincipal(this, role)
                    }
                    .addOnFailureListener { erro ->
                        mostrarCarregamento(false)
                        Toast.makeText(this, "Erro ao guardar o perfil: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { erro ->
                mostrarCarregamento(false)
                val mensagem = when (erro) {
                    is FirebaseAuthUserCollisionException -> "Já existe uma conta com este email."
                    is FirebaseAuthWeakPasswordException -> "Palavra-passe demasiado fraca."
                    else -> "Não foi possível criar a conta: ${erro.localizedMessage}"
                }
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarCarregamento(aCarregar: Boolean) {
        binding.progresso.visibility = if (aCarregar) View.VISIBLE else View.GONE
        binding.btnCriarConta.isEnabled = !aCarregar
    }
}
