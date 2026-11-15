package com.example.escotech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun LoginScreen(modifier: Modifier = Modifier){
var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var emailError by remember {mutableStateOf("")}
    var passwordError by remember {mutableStateOf("")}
    var loginMessage by remember {mutableStateOf("")}

    Column(modifier = modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {

        Text(text="Bem vindo ao EscoTech!", )

        TextField(
            value = email,
            onValueChange = {email = it},
            label = { Text( "Email")},
            isError = emailError != "",
            supportingText = {Text(emailError)}
        )

        TextField(
            value = password,
            onValueChange = {password = it},
            label = { Text( "Password")},
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != "",
            supportingText = {Text(passwordError)}
        )

        Button(onClick = {
            //email vereficatio
            if (email.isBlank()) {

                emailError = "Preencha o email!"
            }
            else if(!email.contains("@")){
                emailError = "Email invalido"
            }
            else{
                emailError = ""
            }


            //passowrd
            if(password.isBlank()){
                passwordError = "Preenche password"
            }
            else{
                passwordError = ""
            }

            //Login sem base de dados(teste)
            if(emailError == "" && passwordError == ""){

                if (email == "a2000@alunos.sefo.pt" && password == "12345"){
                    loginMessage = "Login com sucesso"
                } else {
                    loginMessage = "Email ou pass incorretos"
                }
            }
            else{
                loginMessage = ""
            }

        } ){

            Text("Entrar")
        }
            Text(loginMessage)
    }
}

@Preview(showBackground=true)
@Composable
fun LoginScreenPreview(){
    LoginScreen()
}

