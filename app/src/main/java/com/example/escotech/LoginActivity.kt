package com.example.escotech

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.escotech.databinding.ActivityLoginBinding
import com.example.escotech.util.abrirEcraPrincipal
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Primeiro ecrã da app: login com email e palavra-passe.
 * Depois do login, lê o papel (role) do utilizador no Firestore
 * e abre o ecrã do aluno ou do professor.
 */
class LoginActivity : AppCompatActivity() {

    // ViewBinding: dá acesso direto às vistas do activity_login.xml
    private lateinit var binding: ActivityLoginBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.topo)

        binding.btnEntrar.setOnClickListener { fazerLogin() }

        binding.tvIrRegisto.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Se o utilizador já tinha sessão iniciada, salta o login
        val utilizadorAtual = auth.currentUser
        if (utilizadorAtual != null) {
            redirecionarPorPapel(utilizadorAtual.uid)
        }
    }

    /** Valida os campos e tenta iniciar sessão no Firebase Auth. */
    private fun fazerLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Limpar erros anteriores
        binding.campoEmail.error = null
        binding.campoPassword.error = null

        if (email.isEmpty()) {
            binding.campoEmail.error = "Escreve o teu email"
            return
        }
        if (password.isEmpty()) {
            binding.campoPassword.error = "Escreve a tua palavra-passe"
            return
        }

        mostrarCarregamento(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { resultado ->
                redirecionarPorPapel(resultado.user!!.uid)
            }
            .addOnFailureListener { erro ->
                mostrarCarregamento(false)
                val mensagem = when (erro) {
                    is FirebaseAuthInvalidUserException,
                    is FirebaseAuthInvalidCredentialsException -> "Email ou palavra-passe incorretos."
                    else -> "Não foi possível entrar: ${erro.localizedMessage}"
                }
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
    }

    /** Lê o documento users/{uid} e abre o ecrã certo para o papel do utilizador. */
    private fun redirecionarPorPapel(uid: String) {
        mostrarCarregamento(true)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { documento ->
                val role = documento.getString("role")
                if (role == null) {
                    // A conta existe no Auth mas não tem perfil no Firestore
                    mostrarCarregamento(false)
                    auth.signOut()
                    Toast.makeText(this, "Perfil não encontrado. Cria a conta de novo.", Toast.LENGTH_LONG).show()
                } else {
                    abrirEcraPrincipal(this, role)
                }
            }
            .addOnFailureListener { erro ->
                mostrarCarregamento(false)
                Toast.makeText(this, "Erro ao ler o perfil: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /** Mostra a roda de carregamento e bloqueia o botão enquanto esperamos pelo Firebase. */
    private fun mostrarCarregamento(aCarregar: Boolean) {
        binding.progresso.visibility = if (aCarregar) View.VISIBLE else View.GONE
        binding.btnEntrar.isEnabled = !aCarregar
    }
}
