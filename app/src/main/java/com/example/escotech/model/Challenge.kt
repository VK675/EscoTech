package com.example.escotech.model

import com.google.firebase.firestore.DocumentId

/** Desafio de C# guardado em challenges/{challengeId}. */
data class Challenge(
    @DocumentId
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var question: String = "",
    var options: List<String> = emptyList(),  // sempre 4 opções
    var correctAnswer: Int = 0,               // índice da opção certa (0 a 3)
    var xpReward: Int = 0,
    var difficulty: String = "beginner",      // "beginner" ou "intermediate"
    var createdBy: String = ""                // uid do professor
)
