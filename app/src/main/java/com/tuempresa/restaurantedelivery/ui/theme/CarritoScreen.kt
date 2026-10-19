package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tuempresa.restaurantedelivery.model.ItemCarrito
import com.tuempresa.restaurantedelivery.model.Producto
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun CarritoScreen(
    carrito: List<ItemCarrito>,
    productos: List<Producto>,
    cargando: Boolean,
    mensajeError: String,
    guardando: Boolean,
    onAumentar: (String) -> Unit,
    onDisminuir: (String) -> Unit,
    onEliminarProducto: (String) -> Unit,
    onContinuar: () -> Unit,
    onVolverMenu: () -> Unit
) {
    val productosPorId = productos.associateBy { it.id }

    val todosDisponibles = carrito.all {
        productosPorId.containsKey(it.productoId)
    }

    val total = carrito.fold(BigDecimal.ZERO) { acumulado, item ->
        val producto = productosPorId[item.productoId]

        if (producto != null) {
            acumulado.add(
                calcularSubtotal(
                    precio = producto.precio,
                    cantidad = item.cantidad
                )
            )
        } else {
            acumulado
        }
    }

    Scaffold { espacioInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .padding(16.dp)
        ) {
            Text(
                text = "Tu carrito",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (guardando) {
                Text(
                    text = "Guardando cambios...",
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                cargando -> {
                    Text(text = "Cargando carrito...")
                }

                mensajeError.isNotEmpty() -> {
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                carrito.isEmpty() -> {
                    Text(text = "Todavía no agregaste productos")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = carrito,
                            key = { it.productoId }
                        ) { item ->

                            val producto = productosPorId[item.productoId]

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (producto != null) {
                                            AsyncImage(
                                                model = producto.imagenUrl,
                                                contentDescription = producto.nombre,
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = producto?.nombre
                                                    ?: "Producto no disponible",
                                                fontWeight = FontWeight.Bold
                                            )

                                            if (producto != null) {
                                                Text(
                                                    text = "Precio: S/ %.2f".format(
                                                        producto.precio
                                                    )
                                                )

                                                val subtotal = calcularSubtotal(
                                                    producto.precio,
                                                    item.cantidad
                                                )

                                                Text(
                                                    text = "Subtotal: S/ " +
                                                            subtotal.toPlainString(),
                                                    color =
                                                        MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onDisminuir(item.productoId)
                                            },
                                            enabled = !guardando && producto != null
                                        ) {
                                            Text(text = "−")
                                        }

                                        Text(
                                            text = "${item.cantidad}",
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedButton(
                                            onClick = {
                                                onAumentar(item.productoId)
                                            },
                                            enabled = !guardando && producto != null
                                        ) {
                                            Text(text = "+")
                                        }

                                        TextButton(
                                            onClick = {
                                                onEliminarProducto(item.productoId)
                                            },
                                            enabled = !guardando
                                        ) {
                                            Text(text = "Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (todosDisponibles) {
                        Text(
                            text = "Subtotal: S/ " +
                                    total.setScale(2).toPlainString()
                        )

                        Text(
                            text = "Total: S/ " +
                                    total.setScale(2).toPlainString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Elimina los productos no disponibles " +
                                    "para calcular el total.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (
                !cargando &&
                mensajeError.isEmpty() &&
                carrito.isNotEmpty() &&
                todosDisponibles
            ) {
                Button(
                    onClick = onContinuar,
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar con el pedido")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = onVolverMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Volver al menú")
            }
        }
    }
}

private fun calcularSubtotal(
    precio: Double,
    cantidad: Long
): BigDecimal {
    return BigDecimal.valueOf(precio)
        .setScale(2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(cantidad))
}