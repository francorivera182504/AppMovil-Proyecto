package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tuempresa.restaurantedelivery.data.productosEjemplo
import com.tuempresa.restaurantedelivery.model.Producto

@Composable
fun MenuScreen(
    onSeleccionarProducto: (Producto) -> Unit,
    onAgregarProducto: (Producto) -> Unit,
    onVerCarrito: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onVerCarrito
            ) {
                Text(text = "🛒")
            }
        }
    ) { espacioInterior ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hola, ¿qué deseas comer?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Elige un producto para ver sus detalles",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(productosEjemplo) { producto ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSeleccionarProducto(producto)
                            },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme
                                .colorScheme
                                .surfaceVariant
                        )
                    ) {
                        AsyncImage(
                            model = producto.imagenUrl,
                            contentDescription = producto.nombre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp
                                    )
                                ),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "S/ %.2f"
                                        .format(producto.precio),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onAgregarProducto(producto)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Agregar al carrito")
                            }
                        }
                    }
                }
            }
        }
    }
}