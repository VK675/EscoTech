package com.example.escotech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                    KotlinPraticScreen(

                    )
                }
            }
        }
    }
}



@Composable
fun KotlinPraticScreen(modifier = Modifier.padding(innerPadding))
{

    val name = "Valerii"
    val courseNome = "PSI9"

    println("Nome: $name")
    println("Curso: $courseNome")

    Column() {
        Text("Nome: $name")
        Text("Curso: $courseNome")
    }
}