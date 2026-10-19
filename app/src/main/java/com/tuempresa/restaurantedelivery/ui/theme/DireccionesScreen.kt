package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tuempresa.restaurantedelivery.model.Direccion

@Composable
fun DireccionesScreen(
    usuarioId: String,
    onVolver: () -> Unit
) {
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val clienteRef = remember(usuarioId) {
        db.collection("clientes").document(usuarioId)
    }

    val direccionesRef = remember(usuarioId) {
        clienteRef.collection("direcciones")
    }

    var direcciones by remember(usuarioId) {
        mutableStateOf<List<Direccion>>(emptyList())
    }

    var seleccionadaId by remember(usuarioId) {
        mutableStateOf("")
    }

    var cargandoDirecciones by remember(usuarioId) {
        mutableStateOf(true)
    }

    var cargandoSeleccion by remember(usuarioId) {
        mutableStateOf(true)
    }

    var errorDirecciones by remember(usuarioId) {
        mutableStateOf("")
    }

    var errorSeleccion by remember(usuarioId) {
        mutableStateOf("")
    }

    var errorAccion by remember(usuarioId) {
        mutableStateOf("")
    }

    var guardando by remember(usuarioId) {
        mutableStateOf(false)
    }

    var mostrarFormulario by rememberSaveable(usuarioId) {
        mutableStateOf(false)
    }

    var idEditando by rememberSaveable(usuarioId) {
        mutableStateOf<String?>(null)
    }

    var calle by rememberSaveable(usuarioId) {
        mutableStateOf("")
    }

    var numero by rememberSaveable(usuarioId) {
        mutableStateOf("")
    }

    var distrito by rememberSaveable(usuarioId) {
        mutableStateOf("")
    }

    var ciudad by rememberSaveable(usuarioId) {
        mutableStateOf("")
    }

    var referencia by rememberSaveable(usuarioId) {
        mutableStateOf("")
    }

    var errorFormulario by remember(usuarioId) {
        mutableStateOf("")
    }

    // Leer las direcciones y la selección guardada.
    DisposableEffect(usuarioId) {
        var activo = true

        val escuchaDirecciones = direccionesRef
            .addSnapshotListener { resultado, error ->
                if (activo) {
                    cargandoDirecciones = false

                    if (error != null) {
                        direcciones = emptyList()
                        errorDirecciones =
                            "No se pudieron cargar tus direcciones."
                    } else {
                        try {
                            direcciones = resultado
                                ?.documents
                                ?.map { documento ->
                                    val direccion = documento.toObject(
                                        Direccion::class.java
                                    ) ?: throw IllegalArgumentException()

                                    direccion.copy(id = documento.id)
                                }
                                ?.sortedBy { it.calle }
                                ?: emptyList()

                            errorDirecciones = ""

                        } catch (_: Exception) {
                            direcciones = emptyList()
                            errorDirecciones =
                                "No se pudieron leer los datos de las direcciones."
                        }
                    }
                }
            }

        val escuchaSeleccion = clienteRef
            .addSnapshotListener { documento, error ->
                if (activo) {
                    cargandoSeleccion = false

                    if (error != null) {
                        seleccionadaId = ""
                        errorSeleccion =
                            "No se pudo cargar la dirección seleccionada."
                    } else {
                        try {
                            seleccionadaId = documento
                                ?.getString("direccionSeleccionadaId")
                                ?: ""

                            errorSeleccion = ""

                        } catch (_: Exception) {
                            seleccionadaId = ""
                            errorSeleccion =
                                "No se pudo leer la dirección seleccionada."
                        }
                    }
                }
            }

        onDispose {
            activo = false
            escuchaDirecciones.remove()
            escuchaSeleccion.remove()
        }
    }

    fun abrirFormulario(direccion: Direccion? = null) {
        idEditando = direccion?.id
        calle = direccion?.calle ?: ""
        numero = direccion?.numero ?: ""
        distrito = direccion?.distrito ?: ""
        ciudad = direccion?.ciudad ?: ""
        referencia = direccion?.referencia ?: ""

        errorFormulario = ""
        errorAccion = ""
        mostrarFormulario = true
    }

    fun guardarDireccion() {
        if (guardando) return

        val calleLimpia = calle.trim()
        val numeroLimpio = numero.trim()
        val distritoLimpio = distrito.trim()
        val ciudadLimpia = ciudad.trim()
        val referenciaLimpia = referencia.trim()

        errorFormulario = when {
            calleLimpia.isEmpty() ||
                    numeroLimpio.isEmpty() ||
                    distritoLimpio.isEmpty() ||
                    ciudadLimpia.isEmpty() -> {
                "Completa calle, número, distrito y ciudad."
            }

            calleLimpia.length > 150 -> {
                "La calle debe tener como máximo 150 caracteres."
            }

            numeroLimpio.length > 20 -> {
                "El número debe tener como máximo 20 caracteres."
            }

            distritoLimpio.length > 100 ||
                    ciudadLimpia.length > 100 -> {
                "Distrito y ciudad admiten hasta 100 caracteres cada uno."
            }

            referenciaLimpia.length > 250 -> {
                "La referencia debe tener como máximo 250 caracteres."
            }

            else -> ""
        }

        if (errorFormulario.isNotEmpty()) return

        guardando = true

        val datos = mapOf(
            "calle" to calleLimpia,
            "numero" to numeroLimpio,
            "distrito" to distritoLimpio,
            "ciudad" to ciudadLimpia,
            "referencia" to referenciaLimpia
        )

        val idActual = idEditando

        val tarea = if (idActual == null) {
            direccionesRef.document().set(datos)
        } else {
            direccionesRef.document(idActual).update(datos)
        }

        tarea.addOnCompleteListener { resultado ->
            guardando = false

            if (resultado.isSuccessful) {
                mostrarFormulario = false
                errorFormulario = ""
            } else {
                errorFormulario =
                    "No se pudo guardar la dirección. Inténtalo nuevamente."
            }
        }
    }

    fun seleccionarDireccion(direccion: Direccion) {
        if (guardando) return

        guardando = true
        errorAccion = ""

        db.runTransaction { transaccion ->
            val documento = transaccion.get(
                direccionesRef.document(direccion.id)
            )

            check(documento.exists()) {
                "La dirección ya no existe"
            }

            transaccion.set(
                clienteRef,
                mapOf("direccionSeleccionadaId" to direccion.id),
                SetOptions.merge()
            )

            null
        }.addOnCompleteListener { resultado ->
            guardando = false

            if (!resultado.isSuccessful) {
                errorAccion =
                    "No se pudo seleccionar la dirección. Inténtalo nuevamente."
            }
        }
    }

    fun eliminarDireccion(direccion: Direccion) {
        if (guardando) return

        guardando = true
        errorAccion = ""

        db.runTransaction { transaccion ->
            val cliente = transaccion.get(clienteRef)

            transaccion.delete(
                direccionesRef.document(direccion.id)
            )

            if (
                cliente.getString("direccionSeleccionadaId") ==
                direccion.id
            ) {
                transaccion.set(
                    clienteRef,
                    mapOf("direccionSeleccionadaId" to ""),
                    SetOptions.merge()
                )
            }

            null
        }.addOnCompleteListener { resultado ->
            guardando = false

            if (!resultado.isSuccessful) {
                errorAccion =
                    "No se pudo eliminar la dirección. Inténtalo nuevamente."
            }
        }
    }

    val cargando = cargandoDirecciones || cargandoSeleccion

    val errorCarga = errorDirecciones.ifEmpty {
        errorSeleccion
    }

    val puedeOperar = !cargando &&
            !guardando &&
            errorCarga.isEmpty()

    Scaffold { espacioInterior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis direcciones",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onVolver,
                    enabled = !guardando
                ) {
                    Text(text = "Volver")
                }
            }

            Text(text = "Elige dónde entregar tu pedido.")

            Button(
                onClick = {
                    abrirFormulario()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = puedeOperar
            ) {
                Text(text = "Agregar dirección")
            }

            if (guardando) {
                Text(
                    text = "Guardando cambios...",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (errorAccion.isNotEmpty()) {
                Text(
                    text = errorAccion,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when {
                cargando -> {
                    Text(text = "Cargando direcciones...")
                }

                errorCarga.isNotEmpty() -> {
                    Text(
                        text = errorCarga,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                direcciones.isEmpty() -> {
                    Text(text = "Todavía no registraste direcciones.")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = direcciones,
                            key = { it.id }
                        ) { direccion ->

                            val elegida = direccion.id == seleccionadaId

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${direccion.calle} " +
                                                direccion.numero,
                                        style =
                                            MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "${direccion.distrito}, " +
                                                direccion.ciudad
                                    )

                                    if (direccion.referencia.isNotBlank()) {
                                        Text(
                                            text = "Referencia: " +
                                                    direccion.referencia
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            seleccionarDireccion(direccion)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = puedeOperar && !elegida
                                    ) {
                                        Text(
                                            text = if (elegida) {
                                                "Seleccionada para entrega"
                                            } else {
                                                "Usar esta dirección"
                                            }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(
                                            onClick = {
                                                abrirFormulario(direccion)
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = puedeOperar
                                        ) {
                                            Text(text = "Editar")
                                        }

                                        TextButton(
                                            onClick = {
                                                eliminarDireccion(direccion)
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = puedeOperar
                                        ) {
                                            Text(text = "Eliminar")
                                        }
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
                    text = if (idEditando == null) {
                        "Nueva dirección"
                    } else {
                        "Editar dirección"
                    }
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoDireccion(
                        etiqueta = "Calle o avenida",
                        valor = calle,
                        habilitado = !guardando,
                        alCambiar = { calle = it }
                    )

                    CampoDireccion(
                        etiqueta = "Número (o S/N)",
                        valor = numero,
                        habilitado = !guardando,
                        alCambiar = { numero = it }
                    )

                    CampoDireccion(
                        etiqueta = "Distrito",
                        valor = distrito,
                        habilitado = !guardando,
                        alCambiar = { distrito = it }
                    )

                    CampoDireccion(
                        etiqueta = "Ciudad",
                        valor = ciudad,
                        habilitado = !guardando,
                        alCambiar = { ciudad = it }
                    )

                    CampoDireccion(
                        etiqueta = "Referencia (opcional)",
                        valor = referencia,
                        habilitado = !guardando,
                        alCambiar = { referencia = it }
                    )

                    if (errorFormulario.isNotEmpty()) {
                        Text(
                            text = errorFormulario,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        guardarDireccion()
                    },
                    enabled = !guardando
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
                    onClick = {
                        mostrarFormulario = false
                    },
                    enabled = !guardando
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CampoDireccion(
    etiqueta: String,
    valor: String,
    habilitado: Boolean,
    alCambiar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = {
            Text(text = etiqueta)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = habilitado,
        singleLine = true
    )
}