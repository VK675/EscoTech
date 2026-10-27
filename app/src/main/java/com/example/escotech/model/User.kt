package com.example.escotech.model

import com.google.firebase.firestore.DocumentId

/**
 * Utilizador guardado em users/{userId}.
 *
 * Todos os campos têm valor por defeito: o Firestore precisa de um
 * construtor vazio para converter o documento com toObject().
 */
data class User(
    @DocumentId
    var id: String = "",          // id do documento (= uid do Firebase Auth)
    var name: String = "",
    var email: String = "",
    var role: String = "",        // "student" ou "teacher"
    var xp: Int = 0,
    var level: Int = 1,
    var avatarId: Int = 0,        // posição na lista Avatares.LISTA
    var classId: String = ""      // turma do aluno (ex.: "12A"); vazio para professores
)
