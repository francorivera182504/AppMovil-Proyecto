package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuempresa.restaurantedelivery.model.Producto
import java.text.Normalizer
import java.util.Locale

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
    onCerrarSesion: () -> Unit,
    esAdministrador: Boolean,
    onVerAdministracion: () -> Unit,
    onVerPerfil: () -> Unit
) {
    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    // null significa que se muestran todas las categorías.
    var categoriaSeleccionada by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // Las categorías salen de los productos reales de Firebase.
    val categorias = remember(productos) {
        productos
            .map { it.categoria.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ROOT) }
            .sortedBy { it.lowercase(Locale.ROOT) }
    }

    // Si desaparece la categoría seleccionada, volvemos a "Todas".
    LaunchedEffect(categorias, cargando, mensajeError) {
        if (!cargando && mensajeError.isEmpty()) {
            val seleccion = categoriaSeleccionada

            if (
                seleccion != null &&
                categorias.none {
                    it.equals(seleccion, ignoreCase = true)
                }
            ) {
                categoriaSeleccionada = null
            }
        }
    }

    val productosFiltrados = remember(
        productos,
        busqueda,
        categoriaSeleccionada
    ) {
        val textoBuscado = normalizarBusqueda(busqueda)
        val categoria = categoriaSeleccionada

        productos.filter { producto ->
            val coincideNombre = normalizarBusqueda(
                producto.nombre
            ).contains(textoBuscado)

            val coincideCategoria =
                categoria == null ||
                        producto.categoria.trim().equals(
                            categoria,
                            ignoreCase = true
                        )

            coincideNombre && coincideCategoria
        }
    }

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

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
                BotonDelivery(
                    texto = "Mi perfil",
                    onClick = onVerPerfil,
                    secundario = true
                )
            }
            if (esAdministrador) {
                item {
                    BotonDelivery(
                        texto = "Panel de administración",
                        onClick = onVerAdministracion,
                        secundario = true
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = {
                        busqueda = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Buscar productos")
                    },
                    placeholder = {
                        Text("Escribe el nombre de un producto")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    busqueda = ""
                                }
                            ) {
                                Text("Borrar")
                            }
                        }
                    }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = categoriaSeleccionada == null,
                            onClick = {
                                categoriaSeleccionada = null
                            },
                            label = {
                                Text("Todas")
                            }
                        )
                    }

                    items(categorias) { categoria ->
                        FilterChip(
                            selected = categoriaSeleccionada.equals(
                                categoria,
                                ignoreCase = true
                            ),
                            onClick = {
                                categoriaSeleccionada = categoria
                            },
                            label = {
                                Text(categoria)
                            }
                        )
                    }
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

                            Text("Cargando el menú...")
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
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }

                productosFiltrados.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Sin resultados",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = "Prueba otro nombre o categoría.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            BotonDelivery(
                                texto = "Limpiar filtros",
                                secundario = true,
                                onClick = {
                                    busqueda = ""
                                    categoriaSeleccionada = null
                                }
                            )
                        }
                    }
                }

                else -> {
                    items(
                        items = productosFiltrados,
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

// Permite buscar "limon" y encontrar "Limón".
private fun normalizarBusqueda(texto: String): String {
    return Normalizer
        .normalize(texto.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)
}