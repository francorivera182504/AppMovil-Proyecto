package com.tuempresa.restaurantedelivery.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tuempresa.restaurantedelivery.model.Producto

@Composable
fun ProductoDetalleScreen(
    producto: Producto,
    onAgregarProducto: () -> Unit,
    onVolverMenu: () -> Unit
) {
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
                    TextButton(onClick = onVolverMenu) {
                        Text("← Volver al menú")
                    }
                }
            }
        },
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Precio",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "S/ %.2f".format(producto.precio),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    BotonDelivery(
                        texto = "Agregar al carrito",
                        onClick = onAgregarProducto
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
                FotoDelivery(
                    url = producto.imagenUrl,
                    descripcion = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(28.dp))
                )
            }

            item {
                EtiquetaDelivery(texto = producto.categoria)
            }

            item {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            item {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}