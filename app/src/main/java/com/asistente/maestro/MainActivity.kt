package com.asistente.maestro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Chat() } }
    }
}

@Composable
fun Chat() {
    val mensajes = remember { mutableStateListOf("Maestro: ¡Hola! ¿Qué querés aprender hoy?") }
    var texto by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(12.dp)) {
        LazyColumn(Modifier.weight(1f)) {
            items(mensajes) { Text(it, Modifier.padding(vertical = 6.dp)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(texto, { texto = it }, Modifier.weight(1f), placeholder = { Text("Escribí acá") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (texto.isNotBlank()) {
                    mensajes.add("Vos: $texto")
                    mensajes.add("Maestro: (todavía sin cerebro, pero te escucho)")
                    texto = ""
                }
            }) { Text("Enviar") }
        }
    }
}
