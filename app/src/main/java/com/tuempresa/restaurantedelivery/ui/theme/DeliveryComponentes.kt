package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun BotonDelivery(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    secundario: Boolean = false
) {
    if (secundario) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.heightIn(min = 56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp
            )
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.heightIn(min = 56.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp
            )
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun FotoDelivery(
    url: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    var cargando by remember(url) {
        mutableStateOf(true)
    }

    var error by remember(url) {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier.background(
            MaterialTheme.colorScheme.surfaceVariant
        ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = descripcion,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            onLoading = {
                cargando = true
                error = false
            },
            onSuccess = {
                cargando = false
                error = false
            },
            onError = {
                cargando = false
                error = true
            }
        )

        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.dp
            )
        }

        if (error) {
            Text(
                text = "Imagen no disponible",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun EtiquetaDelivery(texto: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium
        )
    }
}