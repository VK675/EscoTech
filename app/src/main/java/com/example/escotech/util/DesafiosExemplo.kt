package com.example.escotech.util

/**
 * Desafios de C# de exemplo. O professor pode adicioná-los com um botão
 * no painel, para não começar com a lista vazia.
 *
 * Cada desafio é um Map com os mesmos campos da coleção "challenges".
 */
object DesafiosExemplo {

    fun criar(professorId: String): List<Map<String, Any>> = listOf(
        desafio(
            professorId,
            title = "Variáveis em C#",
            description = "Declarar variáveis com o tipo certo.",
            question = "Qual destas linhas declara corretamente um número inteiro em C#?",
            options = listOf("int idade = 17;", "integer idade = 17;", "idade int = 17;", "var idade: int = 17;"),
            correctAnswer = 0,
            xpReward = 30,
            difficulty = "beginner"
        ),
        desafio(
            professorId,
            title = "Escrever na consola",
            description = "Mostrar texto ao utilizador.",
            question = "Que instrução mostra \"Olá!\" na consola?",
            options = listOf("print(\"Olá!\");", "Console.WriteLine(\"Olá!\");", "System.out.println(\"Olá!\");", "echo \"Olá!\";"),
            correctAnswer = 1,
            xpReward = 30,
            difficulty = "beginner"
        ),
        desafio(
            professorId,
            title = "Ciclo for",
            description = "Contar quantas vezes um ciclo é executado.",
            question = "for (int i = 0; i < 5; i++)\n{\n    Console.Write(i);\n}\n\nO que aparece na consola?",
            options = listOf("12345", "01234", "012345", "0123"),
            correctAnswer = 1,
            xpReward = 50,
            difficulty = "beginner"
        ),
        desafio(
            professorId,
            title = "Operador módulo",
            description = "Perceber o resto da divisão.",
            question = "int x = 17 % 5;\n\nQual é o valor de x?",
            options = listOf("3", "2", "3.4", "12"),
            correctAnswer = 1,
            xpReward = 50,
            difficulty = "beginner"
        ),
        desafio(
            professorId,
            title = "Listas genéricas",
            description = "Trabalhar com List<T>.",
            question = "var nums = new List<int> { 4, 8, 15 };\nnums.Add(16);\n\nQuanto vale nums.Count?",
            options = listOf("3", "16", "4", "43"),
            correctAnswer = 2,
            xpReward = 80,
            difficulty = "intermediate"
        ),
        desafio(
            professorId,
            title = "Strings imutáveis",
            description = "O que acontece ao alterar uma string.",
            question = "string s = \"code\";\ns.ToUpper();\nConsole.WriteLine(s);\n\nO que aparece na consola?",
            options = listOf("CODE", "Code", "code", "Erro de compilação"),
            correctAnswer = 2,
            xpReward = 80,
            difficulty = "intermediate"
        )
    )

    private fun desafio(
        professorId: String,
        title: String,
        description: String,
        question: String,
        options: List<String>,
        correctAnswer: Int,
        xpReward: Int,
        difficulty: String
    ): Map<String, Any> = hashMapOf(
        "title" to title,
        "description" to description,
        "question" to question,
        "options" to options,
        "correctAnswer" to correctAnswer,
        "xpReward" to xpReward,
        "difficulty" to difficulty,
        "createdBy" to professorId
    )
}
