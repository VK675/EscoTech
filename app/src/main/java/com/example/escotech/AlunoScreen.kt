package com.example.escotech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class Tarefa(val titulo: String, val disciplina: String, val prazo: String)

// dados de teste enquanto não há base de dados
val tarefas = listOf(
    Tarefa("Algoritmo em C#", "Programação", "25/11"),
    Tarefa("Projeto da app", "Programação", "05/12")
)

val avisos = listOf(
    "Façam a ficha até amanhã!"
)

@Composable
fun AlunoScreen(email: String, onSair: () -> Unit, modifier: Modifier = Modifier) {
    val numero = email.substringBefore("@")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Olá, aluno $numero!", style = MaterialTheme.typography.headlineSmall)
            Text(email, style = MaterialTheme.typography.bodyMedium)
        }

        item {
            Secao("Tarefas") {
                tarefas.forEach { tarefa ->
                    var feita by remember { mutableStateOf(false) }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = feita, onCheckedChange = { feita = it })
                        Column(Modifier.weight(1f)) {
                            Text(
                                tarefa.titulo,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (feita) TextDecoration.LineThrough else null
                            )
                            Text(tarefa.disciplina, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(tarefa.prazo)
                    }
                }
            }
        }

        item {
            Secao("Avisos") {
                avisos.forEach { Text("• $it") }
            }
        }

        item {
            OutlinedButton(onClick = onSair, modifier = Modifier.fillMaxWidth()) {
                Text("Sair")
            }
        }
    }
}

@Composable
fun Secao(titulo: String, conteudo: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            conteudo()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlunoScreenPreview() {
    AlunoScreen(email = "a2000@alunos.sefo.pt", onSair = {})
}
