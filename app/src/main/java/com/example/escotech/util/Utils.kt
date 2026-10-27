package com.example.escotech.util

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.escotech.StudentHomeActivity
import com.example.escotech.TeacherDashboardActivity
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/** Regras de níveis: cada nível precisa de 100 XP. */
object Niveis {
    const val XP_POR_NIVEL = 100

    /** 0–99 XP = nível 1, 100–199 XP = nível 2, etc. */
    fun calcularNivel(xp: Int): Int = xp / XP_POR_NIVEL + 1

    /** Quanto XP o aluno já tem dentro do nível atual (para a barra de progresso). */
    fun xpNoNivelAtual(xp: Int): Int = xp % XP_POR_NIVEL
}

/** Lista de avatares. O campo avatarId do utilizador é a posição nesta lista. */
object Avatares {
    val LISTA = listOf("🐱", "🐶", "🦊", "🐼", "🐸", "🦉", "🐧", "🐯", "🤖", "👾", "🦄", "🐙")

    fun emoji(avatarId: Int): String = LISTA.getOrElse(avatarId) { LISTA[0] }
}

/**
 * Configura o ecrã "de ponta a ponta" (edge-to-edge) com ícones claros na
 * barra de estado, porque o topo de todos os ecrãs é azul-escuro.
 */
fun ComponentActivity.configurarBarrasDeSistema() {
    enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
}

/**
 * Evita que o conteúdo fique por baixo da barra de estado, da barra de
 * navegação ou do teclado.
 *
 * @param raiz  vista principal do layout (recebe espaço em baixo e dos lados)
 * @param topo  vista do topo, normalmente a Toolbar (recebe espaço em cima)
 */
fun aplicarInsets(raiz: View, topo: View) {
    // Guardar o padding original para não o somar várias vezes
    val topoPaddingInicial = topo.paddingTop
    val raizPaddingInicial = raiz.paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(raiz) { _, insets ->
        val barras = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        topo.setPadding(topo.paddingLeft, topoPaddingInicial + barras.top, topo.paddingRight, topo.paddingBottom)
        raiz.setPadding(barras.left, raiz.paddingTop, barras.right, raizPaddingInicial + barras.bottom)
        insets
    }
}

/**
 * Abre o ecrã inicial certo para o papel do utilizador e fecha os ecrãs
 * anteriores (para o botão "voltar" não regressar ao login).
 */
fun abrirEcraPrincipal(activity: Activity, role: String) {
    val destino = if (role == "teacher") {
        TeacherDashboardActivity::class.java
    } else {
        StudentHomeActivity::class.java
    }
    val intent = Intent(activity, destino)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    activity.startActivity(intent)
    activity.finish()
}

/** Turmas escritas de forma diferente ("12 a", "12A") ficam iguais: "12A". */
fun normalizarTurma(texto: String): String = texto.trim().uppercase().replace(" ", "")

/** Converte um Timestamp do Firestore em texto, ex.: "24/10/2026". */
fun formatarData(data: Timestamp?): String {
    if (data == null) return "-"
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-PT"))
    return formato.format(data.toDate())
}

/** Texto em português para o valor guardado no campo difficulty. */
fun textoDificuldade(difficulty: String): String =
    if (difficulty == "intermediate") "Intermédio" else "Iniciante"
