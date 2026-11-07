package com.tuempresa.restaurantedelivery.data

import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.model.Categoria
import com.tuempresa.restaurantedelivery.model.Producto

data class EstadoCatalogo(
    val productos: List<Producto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val cargando: Boolean = true,
    val error: String = ""
)

@Composable
fun recordarCatalogo(
    usuarioId: String?
): EstadoCatalogo {
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    var productos by remember(usuarioId) {
        mutableStateOf<List<Producto>>(emptyList())
    }

    var categorias by remember(usuarioId) {
        mutableStateOf<List<Categoria>>(emptyList())
    }

    var cargandoProductos by remember(usuarioId) {
        mutableStateOf(usuarioId != null)
    }

    var cargandoCategorias by remember(usuarioId) {
        mutableStateOf(usuarioId != null)
    }

    var errorProductos by remember(usuarioId) {
        mutableStateOf("")
    }

    var errorCategorias by remember(usuarioId) {
        mutableStateOf("")
    }

    DisposableEffect(usuarioId) {
        var escuchaActiva = true

        val escuchaProductos = usuarioId?.let {
            db.collection("productos")
                .addSnapshotListener { resultado, error ->
                    if (escuchaActiva) {
                        cargandoProductos = false

                        if (error != null) {
                            productos = emptyList()
                            errorProductos =
                                "No se pudieron cargar los productos."
                        } else {
                            try {
                                productos = resultado
                                    ?.documents
                                    ?.map { documento ->
                                        val producto = documento.toObject(
                                            Producto::class.java
                                        ) ?: error("Producto inválido")

                                        require(
                                            documento.contains("precio") &&
                                                    producto.nombre.isNotBlank() &&
                                                    producto.precio.isFinite() &&
                                                    producto.precio >= 0
                                        )

                                        producto.copy(
                                            id = documento.id
                                        )
                                    }
                                    ?: emptyList()

                                errorProductos = ""
                            } catch (_: Exception) {
                                productos = emptyList()
                                errorProductos =
                                    "Hay productos con datos inválidos en Firebase."
                            }
                        }
                    }
                }
        }

        val escuchaCategorias = usuarioId?.let {
            db.collection("categorias")
                .addSnapshotListener { resultado, error ->
                    if (escuchaActiva) {
                        cargandoCategorias = false

                        if (error != null) {
                            categorias = emptyList()
                            errorCategorias =
                                "No se pudieron cargar las categorías."
                        } else {
                            try {
                                categorias = resultado
                                    ?.documents
                                    ?.map { documento ->
                                        val categoria = documento.toObject(
                                            Categoria::class.java
                                        ) ?: error("Categoría inválida")

                                        require(
                                            categoria.nombre.isNotBlank() &&
                                                    documento.getBoolean("activa") != null
                                        )

                                        categoria.copy(
                                            id = documento.id
                                        )
                                    }
                                    ?.sortedBy {
                                        it.nombre.lowercase()
                                    }
                                    ?: emptyList()

                                errorCategorias = ""
                            } catch (_: Exception) {
                                categorias = emptyList()
                                errorCategorias =
                                    "Hay categorías con datos inválidos en Firebase."
                            }
                        }
                    }
                }
        }

        onDispose {
            escuchaActiva = false
            escuchaProductos?.remove()
            escuchaCategorias?.remove()
        }
    }

    val categoriasPorId = categorias.associateBy {
        it.id
    }

    // Mostramos siempre el nombre actual de la categoría.
    val productosActualizados = productos.map { producto ->
        val categoria = categoriasPorId[producto.categoriaId]

        producto.copy(
            categoria = categoria?.nombre ?: producto.categoria
        )
    }.sortedBy {
        it.nombre.lowercase()
    }

    return EstadoCatalogo(
        productos = productosActualizados,
        categorias = categorias,
        cargando = cargandoProductos || cargandoCategorias,
        error = errorProductos.ifEmpty {
            errorCategorias
        }
    )
}

// Solo estos productos se muestran al cliente.
fun productosDisponibles(
    catalogo: EstadoCatalogo
): List<Producto> {
    if (catalogo.cargando || catalogo.error.isNotBlank()) {
        return emptyList()
    }

    val categoriasActivas = catalogo.categorias
        .filter { it.activa }
        .map { it.id }
        .toSet()

    return catalogo.productos.filter { producto ->
        producto.activo &&
                producto.categoriaId in categoriasActivas
    }
}