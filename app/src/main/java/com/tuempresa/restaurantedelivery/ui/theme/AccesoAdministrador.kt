package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore

enum class AccesoAdministrador {
    CARGANDO,
    ADMINISTRADOR,
    CLIENTE,
    ERROR
}

@Composable
fun recordarAccesoAdministrador(
    usuarioId: String?
): AccesoAdministrador {
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    var acceso by remember(usuarioId) {
        mutableStateOf(
            if (usuarioId == null) {
                AccesoAdministrador.CLIENTE
            } else {
                AccesoAdministrador.CARGANDO
            }
        )
    }

    DisposableEffect(usuarioId) {
        var escuchaActiva = true

        val escucha = usuarioId?.let { uid ->
            db.collection("roles")
                .document(uid)
                .addSnapshotListener { documento, error ->
                    if (escuchaActiva) {
                        acceso = when {
                            error != null -> {
                                AccesoAdministrador.ERROR
                            }

                            documento?.getString("rol") == "admin" -> {
                                AccesoAdministrador.ADMINISTRADOR
                            }

                            else -> {
                                AccesoAdministrador.CLIENTE
                            }
                        }
                    }
                }
        }

        onDispose {
            escuchaActiva = false
            escucha?.remove()
        }
    }

    return acceso
}