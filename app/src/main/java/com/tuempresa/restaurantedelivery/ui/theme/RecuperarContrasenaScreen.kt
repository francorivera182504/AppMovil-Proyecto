package com.tuempresa.restaurantedelivery.ui.theme

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@Composable
fun RecuperarContrasenaScreen(
    onVolver: () -> Unit
) {
    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var correo by remember {
        mutableStateOf("")
    }

    var enviando by remember {
        mutableStateOf(false)
    }

    var solicitudEnviada by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var esError by remember {
        mutableStateOf(false)
    }

    fun enviarCorreo() {
        if (enviando) return

        val correoLimpio = correo.trim()

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(correoLimpio)
                .matches()
        ) {
            esError = true
            mensaje = "Escribe un correo electrónico válido."
            return
        }

        enviando = true
        solicitudEnviada = false
        mensaje = ""
        esError = false

        auth.setLanguageCode("es")

        auth.sendPasswordResetEmail(correoLimpio)
            .addOnCompleteListener { resultado ->
                enviando = false

                val error = resultado.exception

                // Mismo mensaje si la cuenta no existe:
                // no revelamos qué correos están registrados.
                val cuentaNoEncontrada =
                    error is FirebaseAuthInvalidUserException &&
                            error.errorCode == "ERROR_USER_NOT_FOUND"

                if (resultado.isSuccessful || cuentaNoEncontrada) {
                    solicitudEnviada = true
                    esError = false
                    mensaje =
                        "Si existe una cuenta con este correo, recibirás " +
                                "un enlace para restablecer tu contraseña. " +
                                "Revisa también Spam o Correo no deseado."
                } else {
                    esError = true

                    mensaje = when (error) {
                        is FirebaseNetworkException -> {
                            "No se pudo conectar. Revisa tu internet."
                        }

                        is FirebaseTooManyRequestsException -> {
                            "Demasiados intentos. Espera unos minutos."
                        }

                        else -> {
                            "No se pudo enviar la solicitud. Inténtalo nuevamente."
                        }
                    }
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
                        enabled = !enviando
                    ) {
                        Text("← Volver al inicio de sesión")
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Recupera tu cuenta",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Escribe el correo con el que te registraste. " +
                        "Te enviaremos un enlace para elegir una nueva contraseña.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    mensaje = ""
                    solicitudEnviada = false
                },
                label = {
                    Text("Correo electrónico")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !enviando,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
            )

            BotonDelivery(
                texto = when {
                    enviando -> "Enviando..."
                    solicitudEnviada -> "Solicitud enviada"
                    else -> "Enviar enlace"
                },
                enabled = !enviando && !solicitudEnviada,
                onClick = {
                    enviarCorreo()
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