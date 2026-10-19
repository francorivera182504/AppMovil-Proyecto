package com.tuempresa.restaurantedelivery.ui.theme

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onVolverInicio: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido de nuevo",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresa para continuar con tu pedido",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
            label = {
                Text(text = "Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando,
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
                mensaje = ""
            },
            label = {
                Text(text = "Contraseña")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val correoLimpio = correo.trim()

                when {
                    correoLimpio.isEmpty() || contrasena.isEmpty() -> {
                        mensaje = "Completa el correo y la contraseña"
                    }

                    !Patterns.EMAIL_ADDRESS
                        .matcher(correoLimpio)
                        .matches() -> {
                        mensaje = "Escribe un correo electrónico válido"
                    }

                    else -> {
                        mensaje = ""
                        cargando = true

                        auth.signInWithEmailAndPassword(
                            correoLimpio,
                            contrasena
                        ).addOnCompleteListener { resultado ->

                            cargando = false

                            if (resultado.isSuccessful) {
                                onLoginExitoso()
                            } else {
                                val error = resultado.exception

                                mensaje = when (error) {
                                    is FirebaseNetworkException -> {
                                        "No se pudo conectar. Revisa tu conexión a internet"
                                    }

                                    is FirebaseTooManyRequestsException -> {
                                        "Demasiados intentos. Espera unos minutos"
                                    }

                                    is FirebaseAuthInvalidCredentialsException -> {
                                        "Correo o contraseña incorrectos"
                                    }

                                    is FirebaseAuthInvalidUserException -> {
                                        if (
                                            error.errorCode ==
                                            "ERROR_USER_DISABLED"
                                        ) {
                                            "Esta cuenta está deshabilitada"
                                        } else {
                                            "Correo o contraseña incorrectos"
                                        }
                                    }

                                    else -> {
                                        "No se pudo iniciar sesión. Inténtalo nuevamente"
                                    }
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text(
                text = if (cargando) {
                    "Ingresando..."
                } else {
                    "Iniciar sesión"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onVolverInicio,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text(text = "Volver")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}