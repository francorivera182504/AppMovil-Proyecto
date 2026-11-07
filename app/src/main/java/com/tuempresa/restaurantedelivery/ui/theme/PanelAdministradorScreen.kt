package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.model.Categoria

@Composable
fun PanelAdministradorScreen(
    usuarioId: String,
    onVerProductos: () -> Unit,
    onVerPedidos: () -> Unit,
    onVolver: () -> Unit
) {
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var categorias by remember(usuarioId) {
        mutableStateOf<List<Categoria>>(emptyList())
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

    var mostrarFormulario by remember(usuarioId) {
        mutableStateOf(false)
    }

    var categoriaEditandoId by remember(usuarioId) {
        mutableStateOf<String?>(null)
    }

    var nombre by remember(usuarioId) {
        mutableStateOf("")
    }

    var activa by remember(usuarioId) {
        mutableStateOf(true)
    }

    var errorFormulario by remember(usuarioId) {
        mutableStateOf("")
    }

    // Cargar las categorías y escuchar sus cambios.
    DisposableEffect(usuarioId) {
        var escuchaActiva = true

        val escucha = db.collection("categorias")
            .addSnapshotListener { resultado, error ->
                if (escuchaActiva) {
                    cargando = false

                    if (error != null) {
                        categorias = emptyList()
                        errorCarga =
                            "No se pudieron cargar las categorías."
                    } else {
                        try {
                            categorias = resultado
                                ?.documents
                                ?.map { documento ->
                                    val categoria = documento.toObject(
                                        Categoria::class.java
                                    ) ?: throw IllegalArgumentException(
                                        "Categoría inválida"
                                    )

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

                            errorCarga = ""
                        } catch (_: Exception) {
                            categorias = emptyList()
                            errorCarga =
                                "Hay una categoría con datos inválidos."
                        }
                    }
                }
            }

        onDispose {
            escuchaActiva = false
            escucha.remove()
        }
    }

    fun abrirFormulario(categoria: Categoria? = null) {
        categoriaEditandoId = categoria?.id
        nombre = categoria?.nombre ?: ""
        activa = categoria?.activa ?: true
        errorFormulario = ""
        mensaje = ""
        mostrarFormulario = true
    }

    fun guardarCategoria() {
        if (guardando) return

        if (auth.currentUser?.uid != usuarioId) {
            errorFormulario =
                "Tu sesión cambió. Vuelve a iniciar sesión."
            return
        }

        val nombreLimpio = nombre.trim()

        if (nombreLimpio.isBlank()) {
            errorFormulario = "Escribe el nombre de la categoría."
            return
        }

        if (nombreLimpio.length > 60) {
            errorFormulario = "El nombre admite hasta 60 caracteres."
            return
        }

        val idActual = categoriaEditandoId

        val nombreRepetido = categorias.any {
            it.id != idActual &&
                    it.nombre.trim().equals(
                        nombreLimpio,
                        ignoreCase = true
                    )
        }

        if (nombreRepetido) {
            errorFormulario = "Ya existe una categoría con ese nombre."
            return
        }

        val referencia = if (idActual == null) {
            db.collection("categorias").document()
        } else {
            db.collection("categorias").document(idActual)
        }

        val datos = mapOf(
            "nombre" to nombreLimpio,
            "activa" to activa
        )

        guardando = true
        errorFormulario = ""

        val tarea = if (idActual == null) {
            referencia.set(datos)
        } else {
            referencia.update(datos)
        }

        tarea
            .addOnSuccessListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    mostrarFormulario = false
                    mensaje = "Categoría guardada."
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

    fun cambiarEstado(categoria: Categoria) {
        if (guardando) return

        if (auth.currentUser?.uid != usuarioId) {
            mensaje = "Tu sesión cambió. Vuelve a iniciar sesión."
            return
        }

        guardando = true
        mensaje = ""

        db.collection("categorias")
            .document(categoria.id)
            .update("activa", !categoria.activa)
            .addOnSuccessListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false

                    mensaje = if (categoria.activa) {
                        "Categoría desactivada."
                    } else {
                        "Categoría activada."
                    }
                }
            }
            .addOnFailureListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    mensaje =
                        "No se pudo cambiar el estado. Revisa tus permisos."
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
                        Text("← Volver al menú")
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
                    text = "Administración",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Gestiona las categorías del restaurante.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                BotonDelivery(
                    texto = "Gestionar productos",
                    onClick = onVerProductos,
                    enabled = !guardando,
                    secundario = true
                )
            }
            item {
                BotonDelivery(
                    texto = "Pedidos recibidos",
                    onClick = onVerPedidos,
                    enabled = !guardando,
                    secundario = true
                )
            }

            item {
                BotonDelivery(
                    texto = "Nueva categoría",
                    enabled = !guardando && !cargando &&
                            errorCarga.isEmpty(),
                    onClick = {
                        abrirFormulario()
                    }
                )
            }

            if (mensaje.isNotBlank()) {
                item {
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodyMedium
                    )
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

                categorias.isEmpty() -> {
                    item {
                        Text(
                            text = "Todavía no has creado categorías."
                        )
                    }
                }

                else -> {
                    items(
                        items = categorias,
                        key = { it.id }
                    ) { categoria ->
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
                                    text = categoria.nombre,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                EtiquetaDelivery(
                                    texto = if (categoria.activa) {
                                        "Activa"
                                    } else {
                                        "Desactivada"
                                    }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        enabled = !guardando,
                                        onClick = {
                                            abrirFormulario(categoria)
                                        }
                                    ) {
                                        Text("Editar")
                                    }

                                    TextButton(
                                        enabled = !guardando,
                                        onClick = {
                                            cambiarEstado(categoria)
                                        }
                                    ) {
                                        Text(
                                            text = if (categoria.activa) {
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
                    text = if (categoriaEditandoId == null) {
                        "Nueva categoría"
                    } else {
                        "Editar categoría"
                    }
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorFormulario = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Nombre")
                        },
                        singleLine = true,
                        enabled = !guardando,
                        isError = errorFormulario.isNotBlank()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categoría activa")

                        Switch(
                            checked = activa,
                            onCheckedChange = {
                                activa = it
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
                        guardarCategoria()
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