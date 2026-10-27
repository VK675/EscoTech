package com.example.escotech.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/** Resposta de um aluno a um desafio, guardada em submissions/{userId_challengeId}. */
data class Submission(
    @DocumentId
    var id: String = "",
    var userId: String = "",
    var challengeId: String = "",
    // Sem @PropertyName o Firestore leria este campo como "correct" em vez de "isCorrect",
    // porque em Kotlin o getter de "isCorrect" chama-se isCorrect() e não getIsCorrect().
    @get:PropertyName("isCorrect")
    @set:PropertyName("isCorrect")
    var isCorrect: Boolean = false,
    var xpEarned: Int = 0,
    var completedAt: Timestamp? = null
)
