package com.example.escotech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.escotech.ui.theme.EscoTechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EscoTechTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var alunoEmail by rememberSaveable { mutableStateOf<String?>(null) }
                    val email = alunoEmail

                    if (email == null) {
                        LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLogin = { alunoEmail = it }
                        )
                    } else {
                        AlunoScreen(
                            email = email,
                            onSair = { alunoEmail = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

