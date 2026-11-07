package com.tuempresa.restaurantedelivery.ui.theme

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.data.precioEnCentimos
import com.tuempresa.restaurantedelivery.data.recordarCatalogo
import com.tuempresa.restaurantedelivery.model.Producto
import java.math.RoundingMode

@Composable
fun AdminProductosScreen(
    usuarioId: String,
    onVolver: () -> Unit
) {
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val catalogo = recordarCatalogo(usuarioId)

    var guardando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var productoEditandoId by remember {
        mutableStateOf<String?>(null)
    }

    var nombre by remember {
        mutableStateOf("")
    }

    var precio by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
    }

    var imagenUrl by remember {
        mutableStateOf("")
    }

    var categoriaId by remember {
        mutableStateOf("")
    }

    var activo by remember {
        mutableStateOf(true)
    }

    var menuCategoriasAbierto by remember {
        mutableStateOf(false)
    }

    var errorFormulario by remember {
        mutableStateOf("")
    }

    fun abrirFormulario(producto: Producto? = null) {
        productoEditandoId = producto?.id
        nombre = producto?.nombre ?: ""
        precio = producto?.precio?.toString() ?: ""
        descripcion = producto?.descripcion ?: ""
        imagenUrl = producto?.imagenUrl ?: ""
        categoriaId = producto?.categoriaId ?: ""
        activo = producto?.activo ?: true
        menuCategoriasAbierto = false
        errorFormulario = ""
        mensaje = ""
        mostrarFormulario = true
    }

    fun guardarProducto() {
        if (guardando) return

        if (auth.currentUser?.uid != usuarioId) {
            errorFormulario = "Tu sesión cambió."
            return
        }

        val nombreLimpio = nombre.trim()
        val descripcionLimpia = descripcion.trim()
        val urlLimpia = imagenUrl.trim()

        if (
            nombreLimpio.isBlank() ||
            nombreLimpio.length > 100
        ) {
            errorFormulario =
                "Escribe un nombre de entre 1 y 100 caracteres."
            return
        }

        if (
            descripcionLimpia.isBlank() ||
            descripcionLimpia.length > 1500
        ) {
            errorFormulario =
                "Escribe una descripción de hasta 1500 caracteres."
            return
        }

        val precioDecimal = runCatching {
            precio.trim()
                .replace(",", ".")
                .toBigDecimal()
                .setScale(2, RoundingMode.UNNECESSARY)
        }.getOrNull()

        if (precioDecimal == null || precioDecimal.signum() < 0) {
            errorFormulario =
                "Escribe un precio válido con hasta dos decimales."
            return
        }

        val precioNumero = precioDecimal.toDouble()

        if (
            !precioNumero.isFinite() ||
            runCatching {
                precioEnCentimos(precioNumero)
            }.isFailure
        ) {
            errorFormulario = "El precio es demasiado grande."
            return
        }

        val uri = Uri.parse(urlLimpia)

        if (
            uri.scheme != "https" ||
            uri.host.isNullOrBlank() ||
            urlLimpia.length > 2000
        ) {
            errorFormulario =
                "Coloca una dirección de imagen que comience con https://"
            return
        }

        val categoria = catalogo.categorias.firstOrNull {
            it.id == categoriaId
        }

        if (categoria == null) {
            errorFormulario = "Selecciona una categoría."
            return
        }

        val idActual = productoEditandoId

        val referencia = if (idActual == null) {
            db.collection("productos").document()
        } else {
            db.collection("productos").document(idActual)
        }

        val datos = mapOf(
            "nombre" to nombreLimpio,
            "precio" to precioNumero,
            "descripcion" to descripcionLimpia,
            "imagenUrl" to urlLimpia,
            "categoria" to categoria.nombre,
            "categoriaId" to categoria.id,
            "activo" to activo
        )

        guardando = true
        errorFormulario = ""

        // Al editar conservamos el mismo ID.
        referencia.set(datos)
            .addOnSuccessListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    mostrarFormulario = false
                    mensaje = "Producto guardado."
                }
            }
            .addOnFailureListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    errorFormulario =
                        "No se pudo guardar. Revisa tu conexión y permisos."
                }
            }
    }

    fun cambiarEstado(producto: Producto) {
        if (guardando) return

        if (auth.currentUser?.uid != usuarioId) {
            mensaje = "Tu sesión cambió."
            return
        }

        guardando = true
        mensaje = ""

        db.collection("productos")
            .document(producto.id)
            .update("activo", !producto.activo)
            .addOnSuccessListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false

                    mensaje = if (producto.activo) {
                        "Producto desactivado."
                    } else {
                        "Producto activado."
                    }
                }
            }
            .addOnFailureListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    mensaje =
                        "No se pudo cambiar el estado. " +
                                "Abre Editar y comprueba los datos del producto."
                }
            }
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
                    text = "Productos",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Administra el catálogo del restaurante.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                BotonDelivery(
                    texto = "Nuevo producto",
                    enabled = !guardando &&
                            !catalogo.cargando &&
                            catalogo.error.isEmpty() &&
                            catalogo.categorias.isNotEmpty(),
                    onClick = {
                        abrirFormulario()
                    }
                )

                if (
                    !catalogo.cargando &&
                    catalogo.error.isEmpty() &&
                    catalogo.categorias.isEmpty()
                ) {
                    Text(
                        text = "Primero crea una categoría.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
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
                catalogo.cargando -> {
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

                catalogo.error.isNotEmpty() -> {
                    item {
                        Text(
                            text = catalogo.error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                catalogo.productos.isEmpty() -> {
                    item {
                        Text("Todavía no hay productos.")
                    }
                }

                else -> {
                    items(
                        items = catalogo.productos,
                        key = { it.id }
                    ) { producto ->
                        val categoria = catalogo.categorias.firstOrNull {
                            it.id == producto.categoriaId
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            FotoDelivery(
                                url = producto.imagenUrl,
                                descripcion = producto.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )

                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = "S/ %.2f".format(producto.precio),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = categoria?.nombre
                                        ?: "Selecciona una categoría en Editar"
                                )

                                EtiquetaDelivery(
                                    texto = when {
                                        !producto.activo -> {
                                            "Desactivado"
                                        }

                                        categoria == null -> {
                                            "Sin categoría vinculada"
                                        }

                                        !categoria.activa -> {
                                            "Categoría desactivada"
                                        }

                                        else -> {
                                            "Disponible"
                                        }
                                    }
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        enabled = !guardando,
                                        onClick = {
                                            abrirFormulario(producto)
                                        }
                                    ) {
                                        Text("Editar")
                                    }

                                    TextButton(
                                        enabled = !guardando &&
                                                categoria != null,
                                        onClick = {
                                            cambiarEstado(producto)
                                        }
                                    ) {
                                        Text(
                                            text = if (producto.activo) {
                                                "Desactivar"
                                            } else {
                                                "Activar"
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
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = {
                if (!guardando) {
                    mostrarFormulario = false
                }
            },
            title = {
                Text(
                    text = if (productoEditandoId == null) {
                        "Nuevo producto"
                    } else {
                        "Editar producto"
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardando,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio en soles") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardando,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardando,
                        minLines = 2,
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = imagenUrl,
                        onValueChange = { imagenUrl = it },
                        label = { Text("URL de la imagen") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardando,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        )
                    )

                    val categoriaElegida = catalogo.categorias.firstOrNull {
                        it.id == categoriaId
                    }

                    Box {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !guardando,
                            onClick = {
                                menuCategoriasAbierto = true
                            }
                        ) {
                            Text(
                                text = categoriaElegida?.nombre
                                    ?: "Seleccionar categoría"
                            )
                        }

                        DropdownMenu(
                            expanded = menuCategoriasAbierto,
                            onDismissRequest = {
                                menuCategoriasAbierto = false
                            }
                        ) {
                            catalogo.categorias.forEach { categoria ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = categoria.nombre +
                                                    if (categoria.activa) {
                                                        ""
                                                    } else {
                                                        " (desactivada)"
                                                    }
                                        )
                                    },
                                    onClick = {
                                        categoriaId = categoria.id
                                        menuCategoriasAbierto = false
                                    }
                                )
                            }
                        }
                    }

                    if (categoriaElegida?.activa == false) {
                        Text(
                            text = "El producto no aparecerá en el menú " +
                                    "mientras su categoría esté desactivada."
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Producto activo")

                        Switch(
                            checked = activo,
                            onCheckedChange = {
                                activo = it
                            },
                            enabled = !guardando
                        )
                    }

                    if (errorFormulario.isNotBlank()) {
                        Text(
                            text = errorFormulario,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = {
                        guardarProducto()
                    }
                ) {
                    Text(
                        text = if (guardando) {
                            "Guardando..."
                        } else {
                            "Guardar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = {
                        mostrarFormulario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}