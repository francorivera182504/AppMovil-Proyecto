package com.tuempresa.restaurantedelivery.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.data.crearProductosPedido
import com.tuempresa.restaurantedelivery.data.guardarPedido
import com.tuempresa.restaurantedelivery.data.mostrarSoles
import com.tuempresa.restaurantedelivery.data.nombreMetodoPago
import com.tuempresa.restaurantedelivery.data.totalDelPedido
import com.tuempresa.restaurantedelivery.model.Direccion
import com.tuempresa.restaurantedelivery.model.ItemCarrito
import com.tuempresa.restaurantedelivery.model.Producto

@Composable
fun ConfirmarPedidoScreen(
    usuarioId: String,
    carrito: List<ItemCarrito>,
    productos: List<Producto>,
    cargando: Boolean,
    mensajeError: String,
    onVerDirecciones: () -> Unit,
    onVerPedidos: () -> Unit,
    onVolver: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }

    val clienteRef = remember(usuarioId) {
        db.collection("clientes").document(usuarioId)
    }

    val pedidoId = rememberSaveable(usuarioId) {
        clienteRef.collection("pedidos").document().id
    }

    var direcciones by remember(usuarioId) {
        mutableStateOf<List<Direccion>>(emptyList())
    }

    var direccionSeleccionadaId by remember(usuarioId) {
        mutableStateOf("")
    }

    var cargandoDirecciones by remember(usuarioId) {
        mutableStateOf(true)
    }

    var cargandoCliente by remember(usuarioId) {
        mutableStateOf(true)
    }

    var errorDirecciones by remember(usuarioId) {
        mutableStateOf("")
    }

    var errorCliente by remember(usuarioId) {
        mutableStateOf("")
    }

    var metodoPago by rememberSaveable(usuarioId) {
        mutableStateOf("")
    }

    var enviando by remember(usuarioId, pedidoId) {
        mutableStateOf(false)
    }

    var confirmado by rememberSaveable(usuarioId, pedidoId) {
        mutableStateOf(false)
    }

    var mensaje by remember(usuarioId, pedidoId) {
        mutableStateOf("")
    }

    DisposableEffect(usuarioId, pedidoId) {
        var activo = true

        val escuchaCliente = clienteRef.addSnapshotListener { documento, error ->
            if (activo) {
                cargandoCliente = false

                if (error != null) {
                    errorCliente = "No se pudo consultar la dirección elegida."
                } else {
                    try {
                        direccionSeleccionadaId =
                            documento?.getString("direccionSeleccionadaId") ?: ""

                        errorCliente = ""
                    } catch (_: Exception) {
                        errorCliente = "La dirección seleccionada tiene datos inválidos."
                    }
                }
            }
        }

        val escuchaDirecciones = clienteRef.collection("direcciones")
            .addSnapshotListener { resultado, error ->
                if (activo) {
                    cargandoDirecciones = false

                    if (error != null) {
                        errorDirecciones = "No se pudieron cargar tus direcciones."
                    } else {
                        try {
                            direcciones = resultado?.documents?.map { documento ->
                                val direccion = documento
                                    .toObject(Direccion::class.java)
                                    ?: throw IllegalArgumentException()

                                direccion.copy(id = documento.id)
                            } ?: emptyList()

                            errorDirecciones = ""
                        } catch (_: Exception) {
                            errorDirecciones = "Hay una dirección con datos inválidos."
                        }
                    }
                }
            }

        // Permite recuperar la confirmación si se gira la pantalla.
        val escuchaPedido = clienteRef.collection("pedidos")
            .document(pedidoId)
            .addSnapshotListener { documento, error ->
                if (activo) {
                    if (error != null) {
                        mensaje = "No se pudo comprobar el pedido. Revisa los permisos."
                    } else if (
                        documento != null &&
                        documento.exists() &&
                        !documento.metadata.hasPendingWrites()
                    ) {
                        confirmado = true
                        enviando = false
                        mensaje = ""
                    }
                }
            }

        onDispose {
            activo = false
            escuchaCliente.remove()
            escuchaDirecciones.remove()
            escuchaPedido.remove()
        }
    }

    val direccion = direcciones.firstOrNull {
        it.id == direccionSeleccionadaId
    }

    val resumen = remember(carrito, productos) {
        runCatching {
            val lineas = crearProductosPedido(carrito, productos)
            lineas to totalDelPedido(lineas)
        }
    }

    val lineas = resumen.getOrNull()?.first.orEmpty()
    val total = resumen.getOrNull()?.second ?: 0L

    val esperandoDatos =
        cargando || cargandoDirecciones || cargandoCliente

    val errorCarga = listOf(
        mensajeError,
        errorCliente,
        errorDirecciones
    ).firstOrNull { it.isNotBlank() }.orEmpty()

    val puedeConfirmar =
        !esperandoDatos &&
                errorCarga.isEmpty() &&
                resumen.isSuccess &&
                direccion != null &&
                metodoPago.isNotEmpty() &&
                !enviando

    BackHandler(enabled = enviando) {
        // Esperamos el resultado antes de salir.
    }

    Scaffold { espacioInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (confirmado) {
                Text(
                    text = "¡Pedido confirmado!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Tu pedido quedó guardado. Pagarás al recibirlo."
                )

                Button(
                    onClick = onVerPedidos,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mis pedidos")
                }
            } else {
                TextButton(
                    onClick = onVolver,
                    enabled = !enviando
                ) {
                    Text("Volver al carrito")
                }

                Text(
                    text = "Confirma tu pedido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (esperandoDatos) {
                    Text("Cargando información...")
                }

                if (errorCarga.isNotEmpty()) {
                    Text(
                        text = errorCarga,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (
                    !esperandoDatos &&
                    errorCarga.isEmpty() &&
                    resumen.isFailure
                ) {
                    Text(
                        text = resumen.exceptionOrNull()?.message
                            ?: "Revisa los productos de tu carrito.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        lineas.forEach { producto ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${producto.cantidad} × ${producto.nombre}"
                                    )

                                    Text(
                                        text = "${mostrarSoles(producto.precioUnitarioCentimos)} cada uno",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    text = mostrarSoles(producto.subtotalCentimos)
                                )
                            }
                        }

                        Text(
                            text = "Subtotal: ${mostrarSoles(total)}"
                        )

                        Text(
                            text = "Total: ${mostrarSoles(total)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Dirección de entrega",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (direccion != null) {
                            Text("${direccion.calle} ${direccion.numero}")
                            Text("${direccion.distrito}, ${direccion.ciudad}")

                            if (direccion.referencia.isNotBlank()) {
                                Text("Referencia: ${direccion.referencia}")
                            }
                        } else {
                            Text("Selecciona una dirección para continuar.")
                        }

                        OutlinedButton(
                            onClick = onVerDirecciones,
                            enabled = !enviando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Elegir o cambiar dirección")
                        }
                    }
                }

                Text(
                    text = "Método de pago",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text("Pagarás al recibir tu pedido.")

                listOf("efectivo", "tarjeta").forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !enviando) {
                                metodoPago = opcion
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = metodoPago == opcion,
                            onClick = null,
                            enabled = !enviando
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(nombreMetodoPago(opcion))
                    }
                }

                if (mensaje.isNotEmpty()) {
                    Text(
                        text = mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    enabled = puedeConfirmar,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val direccionElegida = direccion

                        if (direccionElegida != null && !enviando) {
                            enviando = true
                            mensaje = ""

                            guardarPedido(
                                usuarioId = usuarioId,
                                pedidoId = pedidoId,
                                productos = lineas,
                                direccion = direccionElegida,
                                metodoPago = metodoPago
                            ).addOnCompleteListener { tarea ->
                                enviando = false

                                if (tarea.isSuccessful) {
                                    confirmado = true
                                } else {
                                    val causa = generateSequence(
                                        tarea.exception as Throwable?
                                    ) {
                                        it.cause
                                    }.filterIsInstance<IllegalArgumentException>()
                                        .firstOrNull()

                                    mensaje = causa?.message
                                        ?: "No se pudo confirmar. Revisa tu conexión y los permisos de Firestore."
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (enviando) "Confirmando..."
                        else "Confirmar pedido"
                    )
                }
            }
        }
    }
}