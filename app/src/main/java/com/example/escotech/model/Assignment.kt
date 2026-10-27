package com.example.escotech.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/** Atribuição de um desafio a uma turma, guardada em assignments/{assignmentId}. */
data class Assignment(
    @DocumentId
    var id: String = "",
    var challengeId: String = "",
    var classId: String = "",
    var deadline: Timestamp? = null,
    var createdBy: String = ""
)
