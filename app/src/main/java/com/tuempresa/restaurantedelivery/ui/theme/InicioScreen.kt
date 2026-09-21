package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InicioScreen(
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍔",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "RestauranteDelivery",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Comida rica, directo a tu puerta",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onIniciarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ya tengo una cuenta")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCrearCuenta,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Crear una cuenta")
        }
    }
}