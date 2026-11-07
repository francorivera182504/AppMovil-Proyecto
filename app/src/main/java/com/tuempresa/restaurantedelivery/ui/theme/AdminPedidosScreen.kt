package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.data.mostrarSoles
import com.tuempresa.restaurantedelivery.data.nombreEstadoPedido
import com.tuempresa.restaurantedelivery.data.nombreMetodoPago
import com.tuempresa.restaurantedelivery.data.siguienteEstadoPedido
import com.tuempresa.restaurantedelivery.model.Pedido
import java.text.SimpleDateFormat
import java.util.Locale

// La ruta permite encontrar el pedido dentro de su cliente.
private data class PedidoAdministracion(
    val ruta: String,
    val pedido: Pedido
)

@Composable
fun AdminPedidosScreen(
    usuarioId: String,
    onVolver: () -> Unit
) {
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var pedidos by remember(usuarioId) {
        mutableStateOf<List<PedidoAdministracion>>(emptyList())
    }

    var cargando by remember(usuarioId) {
        mutableStateOf(true)
    }

    var guardando by remember(usuarioId) {
        mutableStateOf(false)
    }

    var errorCarga by remember(usuarioId) {
        mutableStateOf("")
    }

    var mensaje by remember(usuarioId) {
        mutableStateOf("")
    }

    var rutaSeleccionada by rememberSaveable(usuarioId) {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(usuarioId) {
        var escuchaActiva = true

        // Consulta las subcolecciones "pedidos" de todos los clientes.
        val escucha = db.collectionGroup("pedidos")
            .addSnapshotListener { resultado, error ->
                if (escuchaActiva) {
                    cargando = false

                    if (error != null) {
                        pedidos = emptyList()
                        errorCarga =
                            "No se pudieron cargar los pedidos: ${error.code}"
                    } else {
                        try {
                            pedidos = resultado
                                ?.documents
                                ?.mapNotNull { documento ->
                                    val partes = documento.reference.path
                                        .split("/")

                                    // Solo utilizamos clientes/UID/pedidos/ID.
                                    if (
                                        partes.size != 4 ||
                                        partes[0] != "clientes" ||
                                        partes[2] != "pedidos"
                                    ) {
                                        null
                                    } else {
                                        val pedido = documento.toObject(
                                            Pedido::class.java
                                        ) ?: throw IllegalArgumentException(
                                            "Pedido inválido"
                                        )

                                        PedidoAdministracion(
                                            ruta = documento.reference.path,
                                            pedido = pedido.copy(
                                                id = documento.id
                                            )
                                        )
                                    }
                                }
                                ?.sortedByDescending {
                                    it.pedido.fecha?.toDate()?.time ?: 0L
                                }
                                ?: emptyList()

                            errorCarga = ""
                        } catch (_: Exception) {
                            pedidos = emptyList()
                            errorCarga =
                                "Hay un pedido con datos inválidos."
                        }
                    }
                }
            }

        onDispose {
            escuchaActiva = false
            escucha.remove()
        }
    }

    fun avanzarEstado(elemento: PedidoAdministracion) {
        if (guardando) return

        if (auth.currentUser?.uid != usuarioId) {
            mensaje = "Tu sesión cambió. Vuelve a iniciar sesión."
            return
        }

        val siguiente = siguienteEstadoPedido(
            elemento.pedido.estado
        ) ?: return

        guardando = true
        mensaje = ""

        val referencia = db.document(elemento.ruta)

        db.runTransaction { transaccion ->
            require(auth.currentUser?.uid == usuarioId) {
                "Tu sesión cambió."
            }

            val documentoActual = transaccion.get(referencia)

            require(documentoActual.exists()) {
                "El pedido ya no existe."
            }

            val estadoActual = documentoActual.getString("estado") ?: ""

            // Evita avanzar dos veces si otro administrador lo cambió.
            require(estadoActual == elemento.pedido.estado) {
                "El pedido cambió. Revisa su estado actual."
            }

            require(
                siguienteEstadoPedido(estadoActual) == siguiente
            ) {
                "El cambio de estado no es válido."
            }

            // Únicamente se modifica el estado.
            transaccion.update(
                referencia,
                "estado",
                siguiente
            )

            siguiente
        }.addOnSuccessListener { nuevoEstado ->
            if (auth.currentUser?.uid == usuarioId) {
                guardando = false
                mensaje = "Pedido actualizado: $nuevoEstado."
            }
        }.addOnFailureListener {
            if (auth.currentUser?.uid == usuarioId) {
                guardando = false
                mensaje =
                    "No se pudo actualizar. Revisa el estado actual, " +
                            "tu conexión y los permisos."
            }
        }
    }

    // Se obtiene de la lista actualizada para que el diálogo cambie en vivo.
    val seleccionado = pedidos.firstOrNull {
        it.ruta == rutaSeleccionada
    }

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
                        onClick = onVolver,
                        enabled = !guardando
                    ) {
                        Text("← Volver a administración")
                    }
                }
            }
        }
    ) { espacioInterior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Pedidos recibidos",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Revisa los pedidos y actualiza su estado.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (mensaje.isNotBlank()) {
                item {
                    Text(mensaje)
                }
            }

            if (guardando) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            when {
                cargando -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                errorCarga.isNotBlank() -> {
                    item {
                        Text(
                            text = errorCarga,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                pedidos.isEmpty() -> {
                    item {
                        Text("Todavía no hay pedidos recibidos.")
                    }
                }

                else -> {
                    items(
                        items = pedidos,
                        key = { it.ruta }
                    ) { elemento ->
                        val pedido = elemento.pedido

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Pedido #${pedido.id.take(8)}",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = fechaPedidoAdmin(pedido.fecha),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                EtiquetaDelivery(
                                    texto = nombreEstadoPedido(pedido.estado)
                                )

                                Text(
                                    text = pedido.productos.joinToString("\n") {
                                        "${it.cantidad} × ${it.nombre}"
                                    }
                                )

                                Text(
                                    text = "Total: ${
                                        mostrarSoles(pedido.totalCentimos)
                                    }",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                BotonDelivery(
                                    texto = "Ver detalle y gestionar",
                                    secundario = true,
                                    onClick = {
                                        mensaje = ""
                                        rutaSeleccionada = elemento.ruta
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (seleccionado != null) {
        val pedido = seleccionado.pedido
        val siguiente = siguienteEstadoPedido(pedido.estado)

        AlertDialog(
            onDismissRequest = {
                if (!guardando) {
                    rutaSeleccionada = null
                }
            },
            title = {
                Text("Pedido #${pedido.id.take(8)}")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(fechaPedidoAdmin(pedido.fecha))

                    EtiquetaDelivery(
                        texto = nombreEstadoPedido(pedido.estado)
                    )

                    Text(
                        text = "Productos",
                        fontWeight = FontWeight.Bold
                    )

                    pedido.productos.forEach { producto ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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

                    val direccion = pedido.direccion

                    Text("${direccion.calle} ${direccion.numero}")
                    Text("${direccion.distrito}, ${direccion.ciudad}")

                    if (direccion.referencia.isNotBlank()) {
                        Text("Referencia: ${direccion.referencia}")
                    }

                    Text(
                        text = "Pago: ${nombreMetodoPago(pedido.metodoPago)}"
                    )

                    Text(
                        text = "Subtotal: ${
                            mostrarSoles(pedido.subtotalCentimos)
                        }"
                    )

                    Text(
                        text = "Total: ${mostrarSoles(pedido.totalCentimos)}",
                        fontWeight = FontWeight.Bold
                    )

                    if (mensaje.isNotBlank()) {
                        Text(mensaje)
                    }

                    if (siguiente != null) {
                        BotonDelivery(
                            texto = if (guardando) {
                                "Actualizando..."
                            } else {
                                "$siguiente"
                            },
                            enabled = !guardando,
                            onClick = {
                                avanzarEstado(seleccionado)
                            }
                        )
                    } else if (pedido.estado == "Entregado") {
                        Text(
                            text = "Este pedido ya fue entregado.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = {
                        rutaSeleccionada = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

private fun fechaPedidoAdmin(fecha: Timestamp?): String {
    if (fecha == null) return "Fecha pendiente"

    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(fecha.toDate())
}