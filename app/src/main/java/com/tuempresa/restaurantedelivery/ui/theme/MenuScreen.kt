package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuempresa.restaurantedelivery.model.Producto

@Composable
fun MenuScreen(
    productos: List<Producto>,
    cargando: Boolean,
    mensajeError: String,
    onSeleccionarProducto: (Producto) -> Unit,
    onAgregarProducto: (Producto) -> Unit,
    onVerCarrito: () -> Unit,
    onVerDirecciones: () -> Unit,
    onVerPedidos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        )
                ) {
                    BotonDelivery(
                        texto = "Ver mi carrito",
                        onClick = onVerCarrito
                    )
                }
            }
        }
    ) { espacioInterior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RD",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RestauranteDelivery",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Bienvenido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = onCerrarSesion) {
                        Text("Cerrar sesión")
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "¿Qué se te\nantoja hoy?",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "Explora el menú y elige tu próximo pedido.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotonDelivery(
                        texto = "Direcciones",
                        onClick = onVerDirecciones,
                        secundario = true,
                        modifier = Modifier.weight(1f)
                    )

                    BotonDelivery(
                        texto = "Mis pedidos",
                        onClick = onVerPedidos,
                        secundario = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Nuestro menú",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            when {
                cargando -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()

                            Text(
                                text = "Cargando el menú...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                mensajeError.isNotEmpty() -> {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Text(
                                text = mensajeError,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                productos.isEmpty() -> {
                    item {
                        Text(
                            text = "Todavía no hay productos disponibles.",
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    items(
                        items = productos,
                        key = { it.id }
                    ) { producto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSeleccionarProducto(producto)
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            FotoDelivery(
                                url = producto.imagenUrl,
                                descripcion = producto.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.7f)
                            )

                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                EtiquetaDelivery(
                                    texto = producto.categoria
                                )

                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = "S/ %.2f".format(producto.precio),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                BotonDelivery(
                                    texto = "Agregar al carrito",
                                    onClick = {
                                        onAgregarProducto(producto)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}