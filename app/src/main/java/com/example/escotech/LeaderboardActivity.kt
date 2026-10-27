package com.example.escotech

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.escotech.adapter.LeaderboardAdapter
import com.example.escotech.databinding.ActivityLeaderboardBinding
import com.example.escotech.model.User
import com.example.escotech.util.aplicarInsets
import com.example.escotech.util.configurarBarrasDeSistema
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Ranking de todos os alunos, ordenado por XP. */
class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var adapter: LeaderboardAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configurarBarrasDeSistema()
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root, binding.toolbar)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = LeaderboardAdapter(emptyList(), FirebaseAuth.getInstance().currentUser?.uid)
        binding.rvRanking.layoutManager = LinearLayoutManager(this)
        binding.rvRanking.adapter = adapter

        carregarRanking()
    }

    private fun carregarRanking() {
        // Buscamos só os alunos e ordenamos aqui na app.
        // (Ordenar no Firestore com where + orderBy exigiria criar um índice.)
        db.collection("users")
            .whereEqualTo("role", "student")
            .get()
            .addOnSuccessListener { resultado ->
                val alunos = resultado.toObjects(User::class.java)
                    .sortedByDescending { it.xp }

                binding.progresso.visibility = View.GONE
                binding.tvVazio.visibility = if (alunos.isEmpty()) View.VISIBLE else View.GONE
                adapter.atualizar(alunos)
            }
            .addOnFailureListener { erro ->
                binding.progresso.visibility = View.GONE
                Toast.makeText(this, "Erro ao carregar o ranking: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
