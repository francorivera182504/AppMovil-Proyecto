package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tuempresa.restaurantedelivery.model.Producto
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ProductoDetalleScreen(
    producto: Producto,
    guardando: Boolean,
    onAgregarProducto: (Long) -> Unit,
    onVolverMenu: () -> Unit
) {
    var cantidad by rememberSaveable(producto.id) {
        mutableStateOf(1L)
    }

    val precioUnitario = BigDecimal
        .valueOf(producto.precio)
        .setScale(2, RoundingMode.HALF_UP)

    val subtotal = precioUnitario.multiply(
        BigDecimal.valueOf(cantidad)
    )

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp)
                ) {
                    TextButton(
                        onClick = onVolverMenu,
                        enabled = !guardando
                    ) {
                        Text("← Volver al menú")
                    }
                }
            }
        },
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "S/ ${subtotal.toPlainString()}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    BotonDelivery(
                        texto = if (guardando) {
                            "Agregando..."
                        } else {
                            "Agregar al carrito"
                        },
                        enabled = !guardando,
                        onClick = {
                            onAgregarProducto(cantidad)
                        }
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
                FotoDelivery(
                    url = producto.imagenUrl,
                    descripcion = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(28.dp))
                )
            }

            item {
                EtiquetaDelivery(
                    texto = producto.categoria
                )
            }

            item {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            item {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Precio por unidad: S/ ${precioUnitario.toPlainString()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Elige la cantidad",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    cantidad--
                                },
                                enabled = !guardando && cantidad > 1L
                            ) {
                                Text(
                                    text = "−",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Text(
                                text = cantidad.toString(),
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Button(
                                onClick = {
                                    cantidad++
                                },
                                enabled = !guardando &&
                                        cantidad < Long.MAX_VALUE
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}