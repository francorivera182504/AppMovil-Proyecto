package com.tuempresa.restaurantedelivery.model

import com.google.firebase.Timestamp

data class ProductoPedido(
    val productoId: String = "",
    val nombre: String = "",
    val imagenUrl: String = "",
    val cantidad: Long = 0,
    val precioUnitarioCentimos: Long = 0,
    val subtotalCentimos: Long = 0
)

data class Pedido(
    val id: String = "",
    val productos: List<ProductoPedido> = emptyList(),
    val direccion: Direccion = Direccion(),
    val subtotalCentimos: Long = 0,
    val totalCentimos: Long = 0,
    val metodoPago: String = "",
    val estado: String = "",
    val fecha: Timestamp? = null
)