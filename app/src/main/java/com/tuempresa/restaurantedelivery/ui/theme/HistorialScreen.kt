package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.data.mostrarSoles
import com.tuempresa.restaurantedelivery.data.nombreMetodoPago
import com.tuempresa.restaurantedelivery.model.Pedido
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistorialScreen(
    usuarioId: String,
    onVolver: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }

    var pedidos by remember(usuarioId) {
        mutableStateOf<List<Pedido>>(emptyList())
    }

    var cargando by remember(usuarioId) {
        mutableStateOf(true)
    }

    var mensajeError by remember(usuarioId) {
        mutableStateOf("")
    }

    var pedidoSeleccionadoId by rememberSaveable(usuarioId) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(usuarioId) {
        var activo = true

        val escucha = db.collection("clientes")
            .document(usuarioId)
            .collection("pedidos")
            .addSnapshotListener { resultado, error ->
                if (activo) {
                    cargando = false

                    if (error != null) {
                        pedidos = emptyList()
                        mensajeError = "No se pudo cargar el historial."
                    } else {
                        try {
                            pedidos = resultado?.documents?.map { documento ->
                                val pedido = documento.toObject(Pedido::class.java)
                                    ?: throw IllegalArgumentException()

                                pedido.copy(id = documento.id)
                            }?.sortedByDescending {
                                it.fecha?.toDate()?.time ?: 0L
                            } ?: emptyList()

                            mensajeError = ""
                        } catch (_: Exception) {
                            pedidos = emptyList()
                            mensajeError = "Hay un pedido con datos inválidos."
                        }
                    }
                }
            }

        onDispose {
            activo = false
            escucha.remove()
        }
    }

    val pedidoSeleccionado = pedidos.firstOrNull {
        it.id == pedidoSeleccionadoId
    }

    Scaffold { espacioInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mis pedidos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onVolver) {
                Text("Volver al menú")
            }

            when {
                cargando -> {
                    Text("Cargando pedidos...")
                }

                mensajeError.isNotEmpty() -> {
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                pedidos.isEmpty() -> {
                    Text("Todavía no tienes pedidos confirmados.")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = pedidos,
                            key = { it.id }
                        ) { pedido ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = fechaDelPedido(pedido.fecha),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = pedido.productos.joinToString("\n") {
                                            "${it.cantidad} × ${it.nombre}"
                                        }
                                    )

                                    Text(
                                        text = "Estado: ${pedido.estado}",
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = "Total: ${mostrarSoles(pedido.totalCentimos)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            pedidoSeleccionadoId = pedido.id
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ver detalle")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pedidoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = {
                pedidoSeleccionadoId = null
            },
            title = {
                Text("Detalle del pedido")
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(fechaDelPedido(pedidoSeleccionado.fecha))
                    Text("Estado: ${pedidoSeleccionado.estado}")

                    Text(
                        text = "Productos",
                        fontWeight = FontWeight.Bold
                    )

                    pedidoSeleccionado.productos.forEach { producto ->
                        Column {
                            Text(
                                text = "${producto.cantidad} × ${producto.nombre}",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Precio unitario: ${
                                    mostrarSoles(producto.precioUnitarioCentimos)
                                }"
                            )

                            Text(
                                text = "Subtotal: ${
                                    mostrarSoles(producto.subtotalCentimos)
                                }"
                            )
                        }
                    }

                    Text(
                        text = "Dirección de entrega",
                        fontWeight = FontWeight.Bold
                    )

                    val direccion = pedidoSeleccionado.direccion

                    Text("${direccion.calle} ${direccion.numero}")
                    Text("${direccion.distrito}, ${direccion.ciudad}")

                    if (direccion.referencia.isNotBlank()) {
                        Text("Referencia: ${direccion.referencia}")
                    }

                    Text(
                        text = "Pago: ${
                            nombreMetodoPago(pedidoSeleccionado.metodoPago)
                        }"
                    )

                    Text(
                        text = "Subtotal: ${
                            mostrarSoles(pedidoSeleccionado.subtotalCentimos)
                        }"
                    )

                    Text(
                        text = "Total: ${
                            mostrarSoles(pedidoSeleccionado.totalCentimos)
                        }",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pedidoSeleccionadoId = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

private fun fechaDelPedido(fecha: Timestamp?): String {
    if (fecha == null) {
        return "Fecha pendiente"
    }

    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(fecha.toDate())
}