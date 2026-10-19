package com.tuempresa.restaurantedelivery.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.model.Direccion
import com.tuempresa.restaurantedelivery.model.ItemCarrito
import com.tuempresa.restaurantedelivery.model.Producto
import com.tuempresa.restaurantedelivery.model.ProductoPedido
import java.math.BigDecimal
import java.math.RoundingMode

fun precioEnCentimos(precio: Double): Long {
    require(precio.isFinite() && precio >= 0) {
        "Hay un producto con un precio inválido."
    }

    return BigDecimal.valueOf(precio)
        .setScale(2, RoundingMode.HALF_UP)
        .movePointRight(2)
        .longValueExact()
}

fun mostrarSoles(centimos: Long): String {
    return "S/ " + BigDecimal.valueOf(centimos, 2).toPlainString()
}

fun nombreMetodoPago(metodo: String): String {
    return when (metodo) {
        "efectivo" -> "Efectivo contra entrega"
        "tarjeta" -> "Tarjeta contra entrega"
        else -> "Sin seleccionar"
    }
}

fun crearProductosPedido(
    carrito: List<ItemCarrito>,
    productos: List<Producto>
): List<ProductoPedido> {
    require(carrito.isNotEmpty()) {
        "Tu carrito está vacío."
    }

    val productosPorId = productos.associateBy { it.id }

    return carrito.map { item ->
        val producto = productosPorId[item.productoId]

        require(producto != null) {
            "Elimina del carrito los productos que ya no están disponibles."
        }

        require(item.cantidad > 0) {
            "Hay una cantidad inválida en el carrito."
        }

        val precio = precioEnCentimos(producto.precio)
        val subtotal = Math.multiplyExact(precio, item.cantidad)

        ProductoPedido(
            productoId = producto.id,
            nombre = producto.nombre,
            imagenUrl = producto.imagenUrl,
            cantidad = item.cantidad,
            precioUnitarioCentimos = precio,
            subtotalCentimos = subtotal
        )
    }
}

fun totalDelPedido(productos: List<ProductoPedido>): Long {
    return productos.fold(0L) { acumulado, producto ->
        Math.addExact(acumulado, producto.subtotalCentimos)
    }
}

fun guardarPedido(
    usuarioId: String,
    pedidoId: String,
    productos: List<ProductoPedido>,
    direccion: Direccion,
    metodoPago: String
): Task<String> {
    val db = FirebaseFirestore.getInstance()

    val clienteRef = db.collection("clientes").document(usuarioId)
    val pedidoRef = clienteRef.collection("pedidos").document(pedidoId)

    return db.runTransaction { transaccion ->
        require(FirebaseAuth.getInstance().currentUser?.uid == usuarioId) {
            "Tu sesión cambió. Vuelve a iniciar sesión."
        }

        // Si este intento ya se guardó, no creamos otro pedido.
        val pedidoExistente = transaccion.get(pedidoRef)

        if (pedidoExistente.exists()) {
            return@runTransaction pedidoId
        }

        require(productos.isNotEmpty()) {
            "Tu carrito está vacío."
        }

        require(productos.map { it.productoId }.distinct().size == productos.size) {
            "El carrito contiene productos duplicados."
        }

        require(metodoPago == "efectivo" || metodoPago == "tarjeta") {
            "Selecciona un método de pago."
        }

        require(direccion.id.isNotBlank()) {
            "Selecciona una dirección de entrega."
        }

        val clienteActual = transaccion.get(clienteRef)

        require(
            clienteActual.getString("direccionSeleccionadaId") == direccion.id
        ) {
            "La dirección seleccionada cambió. Revisa el pedido."
        }

        val direccionRef = clienteRef
            .collection("direcciones")
            .document(direccion.id)

        val direccionActual = transaccion.get(direccionRef)
            .toObject(Direccion::class.java)
            ?.copy(id = direccion.id)

        require(direccionActual == direccion) {
            "La dirección cambió o fue eliminada. Selecciónala nuevamente."
        }

        val referenciasCarrito = productos.map {
            clienteRef.collection("carrito").document(it.productoId)
        }

        val documentosCarrito = referenciasCarrito.map {
            transaccion.get(it)
        }

        val documentosProductos = productos.map {
            transaccion.get(
                db.collection("productos").document(it.productoId)
            )
        }

        // Comprobamos los datos actuales antes de guardar.
        productos.forEachIndexed { indice, productoPedido ->
            val cantidadActual = documentosCarrito[indice]
                .getLong("cantidad")

            require(
                productoPedido.cantidad > 0 &&
                        cantidadActual == productoPedido.cantidad
            ) {
                "El carrito cambió. Revisa las cantidades antes de confirmar."
            }

            val productoActual = documentosProductos[indice]
                .toObject(Producto::class.java)

            require(productoActual != null) {
                "Un producto ya no está disponible. Revisa tu carrito."
            }

            require(
                precioEnCentimos(productoActual.precio) ==
                        productoPedido.precioUnitarioCentimos &&
                        productoActual.nombre == productoPedido.nombre
            ) {
                "Un producto cambió. Revisa el resumen y confirma nuevamente."
            }

            require(
                productoPedido.subtotalCentimos == Math.multiplyExact(
                    productoPedido.precioUnitarioCentimos,
                    productoPedido.cantidad
                )
            ) {
                "El subtotal de un producto es incorrecto."
            }
        }

        val total = totalDelPedido(productos)

        val datosPedido = mapOf(
            "productos" to productos,
            "direccion" to direccion,
            "subtotalCentimos" to total,
            "totalCentimos" to total,
            "metodoPago" to metodoPago,
            "estado" to "Confirmado",
            "fecha" to FieldValue.serverTimestamp()
        )

        // Primero hicimos todas las lecturas. Ahora escribimos.
        transaccion.set(pedidoRef, datosPedido)

        referenciasCarrito.forEach {
            transaccion.delete(it)
        }

        pedidoId
    }
}