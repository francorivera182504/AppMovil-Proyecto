package com.tuempresa.restaurantedelivery.data

fun nombreEstadoPedido(estado: String): String {
    return when (estado) {
        // Compatibilidad con los pedidos de la entrega anterior.
        "Confirmado" -> "Recibido"

        "Recibido",
        "En preparación",
        "En camino",
        "Entregado" -> estado

        else -> "Estado desconocido"
    }
}

fun siguienteEstadoPedido(estado: String): String? {
    return when (nombreEstadoPedido(estado)) {
        "Recibido" -> "En preparación"
        "En preparación" -> "En camino"
        "En camino" -> "Entregado"
        else -> null
    }
}