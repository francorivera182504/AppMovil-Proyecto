package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InicioScreen(
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    Scaffold { espacioInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                FotoDelivery(
                    url = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=80",
                    descripcion = "Hamburguesa de portada",
                    modifier = Modifier.matchParentSize()
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RestauranteDelivery",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Elige. Pide. Disfruta.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = "Tu próximo antojo\nempieza aquí.",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Ingresa a tu cuenta o regístrate para elegir tu pedido.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotonDelivery(
                    texto = "Ya tengo cuenta",
                    onClick = onIniciarSesion
                )

                BotonDelivery(
                    texto = "Crear cuenta",
                    onClick = onCrearCuenta,
                    secundario = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}