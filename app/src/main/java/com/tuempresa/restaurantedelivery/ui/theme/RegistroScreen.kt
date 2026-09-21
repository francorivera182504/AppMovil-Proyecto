package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onVolverInicio: () -> Unit
) {
    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var contrasena by remember {
        mutableStateOf("")
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Regístrate para pedir tus platos favoritos",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                Text(text = "Nombre")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
            },
            label = {
                Text(text = "Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
            },
            label = {
                Text(text = "Contraseña")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                when {
                    nombre.isBlank() ||
                            correo.isBlank() ||
                            contrasena.isBlank() -> {
                        mensaje = "Completa todos los campos"
                    }

                    contrasena.length < 6 -> {
                        mensaje =
                            "La contraseña debe tener al menos 6 caracteres"
                    }

                    else -> {
                        auth.createUserWithEmailAndPassword(
                            correo,
                            contrasena
                        )
                            .addOnSuccessListener { resultado ->

                                val uid = resultado.user?.uid

                                if (uid != null) {
                                    val datosCliente = mapOf(
                                        "nombre" to nombre,
                                        "correo" to correo
                                    )

                                    db.collection("clientes")
                                        .document(uid)
                                        .set(datosCliente)
                                        .addOnSuccessListener {
                                            onRegistroExitoso()
                                        }
                                        .addOnFailureListener {
                                            mensaje =
                                                "No se pudieron guardar tus datos"
                                        }
                                }
                            }
                            .addOnFailureListener {
                                mensaje =
                                    "No se pudo crear la cuenta. Revisa el correo"
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Crear cuenta")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onVolverInicio,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.error
        )
    }
}