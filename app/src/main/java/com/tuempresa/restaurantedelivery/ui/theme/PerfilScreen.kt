package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun PerfilScreen(
    usuarioId: String,
    onVolver: () -> Unit
) {
    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    var nombre by remember(usuarioId) {
        mutableStateOf("")
    }

    var telefono by remember(usuarioId) {
        mutableStateOf("")
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

    var esError by remember(usuarioId) {
        mutableStateOf(false)
    }

    val correo = auth.currentUser
        ?.takeIf { it.uid == usuarioId }
        ?.email
        .orEmpty()

    // Cargamos los datos una vez al entrar.
    // Así no sobrescribimos lo que el usuario está escribiendo.
    DisposableEffect(usuarioId) {
        var pantallaActiva = true

        db.collection("clientes")
            .document(usuarioId)
            .get()
            .addOnSuccessListener { documento ->
                if (
                    pantallaActiva &&
                    auth.currentUser?.uid == usuarioId
                ) {
                    nombre = documento.getString("nombre") ?: ""
                    telefono = documento.getString("telefono") ?: ""
                    cargando = false
                    errorCarga = ""
                }
            }
            .addOnFailureListener {
                if (
                    pantallaActiva &&
                    auth.currentUser?.uid == usuarioId
                ) {
                    cargando = false
                    errorCarga =
                        "No se pudo cargar tu perfil. Vuelve a entrar."
                }
            }

        onDispose {
            pantallaActiva = false
        }
    }

    fun guardarPerfil() {
        if (guardando || cargando) return

        val usuario = auth.currentUser

        if (usuario == null || usuario.uid != usuarioId) {
            esError = true
            mensaje = "Tu sesión cambió. Vuelve a iniciar sesión."
            return
        }

        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()

        if (
            nombreLimpio.isBlank() ||
            nombreLimpio.length > 100
        ) {
            esError = true
            mensaje = "Escribe un nombre de entre 1 y 100 caracteres."
            return
        }

        // El teléfono es opcional.
        // Si se escribe, debe tener nueve dígitos.
        if (
            telefonoLimpio.isNotEmpty() &&
            !Regex("[0-9]{9}").matches(telefonoLimpio)
        ) {
            esError = true
            mensaje = "El teléfono debe tener 9 dígitos."
            return
        }

        guardando = true
        mensaje = ""
        esError = false

        val datos = mapOf(
            "nombre" to nombreLimpio,
            "telefono" to telefonoLimpio,
            "correo" to usuario.email.orEmpty()
        )

        db.collection("clientes")
            .document(usuarioId)
            .set(datos, SetOptions.merge())
            .addOnSuccessListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    nombre = nombreLimpio
                    telefono = telefonoLimpio
                    esError = false
                    mensaje = "Perfil actualizado."
                }
            }
            .addOnFailureListener {
                if (auth.currentUser?.uid == usuarioId) {
                    guardando = false
                    esError = true
                    mensaje =
                        "No se pudo guardar. Revisa tu conexión e inténtalo nuevamente."
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioInterior)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Mantén actualizados tus datos personales.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                cargando -> {
                    CircularProgressIndicator()
                }

                errorCarga.isNotBlank() -> {
                    Text(
                        text = errorCarga,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            mensaje = ""
                        },
                        label = {
                            Text("Nombre")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !guardando
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = {
                            telefono = it
                            mensaje = ""
                        },
                        label = {
                            Text("Teléfono")
                        },
                        supportingText = {
                            Text("Opcional · 9 dígitos")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !guardando,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        )
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = {},
                        label = {
                            Text("Correo de la cuenta")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true
                    )

                    BotonDelivery(
                        texto = if (guardando) {
                            "Guardando..."
                        } else {
                            "Guardar cambios"
                        },
                        enabled = !guardando,
                        onClick = {
                            guardarPerfil()
                        }
                    )

                    if (mensaje.isNotBlank()) {
                        Text(
                            text = mensaje,
                            color = if (esError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }
    }
}