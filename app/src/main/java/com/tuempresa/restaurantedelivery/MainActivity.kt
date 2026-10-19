package com.tuempresa.restaurantedelivery

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tuempresa.restaurantedelivery.model.Producto
import com.tuempresa.restaurantedelivery.ui.theme.CarritoScreen
import com.tuempresa.restaurantedelivery.ui.theme.InicioScreen
import com.tuempresa.restaurantedelivery.ui.theme.LoginScreen
import com.tuempresa.restaurantedelivery.ui.theme.MenuScreen
import com.tuempresa.restaurantedelivery.ui.theme.ProductoDetalleScreen
import com.tuempresa.restaurantedelivery.ui.theme.RegistroScreen
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.tuempresa.restaurantedelivery.model.ItemCarrito
import com.tuempresa.restaurantedelivery.ui.theme.DireccionesScreen
import com.tuempresa.restaurantedelivery.ui.theme.ConfirmarPedidoScreen
import com.tuempresa.restaurantedelivery.ui.theme.HistorialScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tuempresa.restaurantedelivery.ui.theme.DeliveryTheme
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DeliveryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AplicacionRestaurante()
                }
        }
    }
}

@Composable
fun AplicacionRestaurante() {
    val navController = rememberNavController()

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    var usuarioId by remember {
        mutableStateOf(auth.currentUser?.uid)
    }

    val pantallaInicial = remember {
        if (auth.currentUser != null) {
            "menu"
        } else {
            "inicio"
        }
    }

    var productos by remember(usuarioId) {
        mutableStateOf<List<Producto>>(emptyList())
    }

    var cargandoProductos by remember(usuarioId) {
        mutableStateOf(usuarioId != null)
    }

    var errorProductos by remember(usuarioId) {
        mutableStateOf("")
    }

    val contexto = LocalContext.current

    var carrito by remember(usuarioId) {
        mutableStateOf<List<ItemCarrito>>(emptyList())
    }

    var cargandoCarrito by remember(usuarioId) {
        mutableStateOf(usuarioId != null)
    }

    var errorCarrito by remember(usuarioId) {
        mutableStateOf("")
    }

    var guardandoCarrito by remember(usuarioId) {
        mutableStateOf(false)
    }

// Recuperar el carrito del usuario conectado.
    DisposableEffect(usuarioId) {
        var escuchaActiva = true

        val escuchaCarrito = usuarioId?.let { uid ->
            db.collection("clientes")
                .document(uid)
                .collection("carrito")
                .addSnapshotListener { resultado, error ->

                    if (escuchaActiva) {
                        cargandoCarrito = false

                        if (error != null) {
                            carrito = emptyList()
                            errorCarrito =
                                "No se pudo cargar tu carrito. Inténtalo nuevamente."
                        } else {
                            try {
                                carrito = resultado
                                    ?.documents
                                    ?.map { documento ->
                                        val cantidad = documento
                                            .getLong("cantidad") ?: 0L

                                        require(cantidad > 0)

                                        ItemCarrito(
                                            productoId = documento.id,
                                            cantidad = cantidad
                                        )
                                    }
                                    ?: emptyList()

                                errorCarrito = ""

                            } catch (_: Exception) {
                                carrito = emptyList()
                                errorCarrito =
                                    "No se pudieron leer los datos de tu carrito."
                            }
                        }
                    }
                }
        }

        onDispose {
            escuchaActiva = false
            escuchaCarrito?.remove()
        }
    }

    // Agregar una unidad o quitar una unidad.
    fun cambiarCantidad(
        productoId: String,
        cambio: Long,
        alGuardar: () -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: return

        if (guardandoCarrito) {
            Toast.makeText(
                contexto,
                "Espera a que termine el cambio anterior",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        guardandoCarrito = true

        val referencia = db.collection("clientes")
            .document(uid)
            .collection("carrito")
            .document(productoId)

        db.runTransaction { transaccion ->
            val documento = transaccion.get(referencia)

            val cantidadActual = documento.getLong("cantidad") ?: 0L
            val cantidadNueva = Math.addExact(cantidadActual, cambio)

            if (cantidadNueva <= 0) {
                transaccion.delete(referencia)
            } else {
                transaccion.set(
                    referencia,
                    mapOf("cantidad" to cantidadNueva)
                )
            }

            null
        }.addOnSuccessListener {
            if (auth.currentUser?.uid == uid) {
                guardandoCarrito = false
                alGuardar()
            }
        }.addOnFailureListener {
            if (auth.currentUser?.uid == uid) {
                guardandoCarrito = false

                Toast.makeText(
                    contexto,
                    "No se pudo actualizar el carrito. Inténtalo nuevamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Eliminar todas las unidades de un producto.
    fun eliminarDelCarrito(productoId: String) {
        val uid = auth.currentUser?.uid ?: return

        if (guardandoCarrito) {
            return
        }

        guardandoCarrito = true

        db.collection("clientes")
            .document(uid)
            .collection("carrito")
            .document(productoId)
            .delete()
            .addOnSuccessListener {
                if (auth.currentUser?.uid == uid) {
                    guardandoCarrito = false
                }
            }
            .addOnFailureListener {
                if (auth.currentUser?.uid == uid) {
                    guardandoCarrito = false

                    Toast.makeText(
                        contexto,
                        "No se pudo eliminar el producto. Inténtalo nuevamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // Detectar el inicio y el cierre de sesión.
    DisposableEffect(auth) {
        val escuchaSesion = FirebaseAuth.AuthStateListener {
            usuarioId = it.currentUser?.uid
        }

        auth.addAuthStateListener(escuchaSesion)

        onDispose {
            auth.removeAuthStateListener(escuchaSesion)
        }
    }

    // Cargar el catálogo del usuario que inició sesión.
    DisposableEffect(usuarioId) {
        var escuchaActiva = true

        val escuchaProductos = if (usuarioId != null) {
            db.collection("productos")
                .addSnapshotListener { resultado, error ->

                    if (escuchaActiva) {
                        cargandoProductos = false

                        if (error != null) {
                            productos = emptyList()
                            errorProductos =
                                "No se pudo cargar el menú. " +
                                        "Revisa tu conexión y los permisos de Firestore."
                        } else {
                            try {
                                productos = resultado
                                    ?.documents
                                    ?.map { documento ->

                                        val producto = documento.toObject(
                                            Producto::class.java
                                        ) ?: throw IllegalArgumentException(
                                            "Producto inválido"
                                        )

                                        require(
                                            documento.contains("precio") &&
                                                    producto.nombre.isNotBlank() &&
                                                    producto.categoria.isNotBlank() &&
                                                    producto.imagenUrl.isNotBlank() &&
                                                    producto.precio.isFinite() &&
                                                    producto.precio >= 0
                                        )

                                        producto.copy(
                                            id = documento.id
                                        )
                                    }
                                    ?.sortedBy { it.nombre }
                                    ?: emptyList()

                                errorProductos = ""

                            } catch (_: Exception) {
                                productos = emptyList()
                                errorProductos =
                                    "Hay un producto con datos inválidos. " +
                                            "Revisa los campos y sus tipos en Firebase."
                            }
                        }
                    }
                }
        } else {
            null
        }

        onDispose {
            escuchaActiva = false
            escuchaProductos?.remove()
        }
    }

    NavHost(
        navController = navController,
        startDestination = pantallaInicial
    ) {
        composable("inicio") {
            InicioScreen(
                onIniciarSesion = {
                    navController.navigate("login")
                },
                onCrearCuenta = {
                    navController.navigate("registro")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate("menu") {
                        popUpTo("inicio") {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },
                onVolverInicio = {
                    navController.popBackStack()
                }
            )
        }

        composable("registro") {
            RegistroScreen(
                onRegistroExitoso = {
                    navController.navigate("menu") {
                        popUpTo("inicio") {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },
                onVolverInicio = {
                    navController.popBackStack()
                }
            )
        }

        composable("menu") {
            MenuScreen(
                productos = productos,
                cargando = cargandoProductos,
                mensajeError = errorProductos,

                onSeleccionarProducto = { producto ->
                    val idProducto = Uri.encode(producto.id)

                    navController.navigate(
                        "detalle/$idProducto"
                    )
                },

                onAgregarProducto = { producto ->
                    cambiarCantidad(
                        productoId = producto.id,
                        cambio = 1L
                    )
                },

                onVerCarrito = {
                    navController.navigate("carrito")
                },
                onVerPedidos = {
                    navController.navigate("pedidos") {
                        launchSingleTop = true
                    }
                },
                onVerDirecciones = {
                    navController.navigate("direcciones") {
                        launchSingleTop = true
                    }
                },

                onCerrarSesion = {
                    auth.signOut()
                    carrito = emptyList()

                    navController.navigate("inicio") {
                        popUpTo("menu") {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "detalle/{productoId}",
            arguments = listOf(
                navArgument("productoId") {
                    type = NavType.StringType
                }
            )
        ) { entrada ->

            val productoId = entrada.arguments
                ?.getString("productoId")

            val producto = productos.firstOrNull {
                it.id == productoId
            }

            when {
                cargandoProductos -> {
                    Text(text = "Cargando producto...")
                }

                errorProductos.isNotEmpty() -> {
                    Text(
                        text = errorProductos,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                producto != null -> {
                    ProductoDetalleScreen(
                        producto = producto,

                        onAgregarProducto = {
                            cambiarCantidad(
                                productoId = producto.id,
                                cambio = 1L,
                                alGuardar = {
                                    if (
                                        navController.currentBackStackEntry
                                            ?.destination
                                            ?.route == "detalle/{productoId}"
                                    ) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        },

                        onVolverMenu = {
                            navController.popBackStack()
                        }
                    )
                }

                else -> {
                    Text(text = "Producto no encontrado")
                }
            }
        }
        composable("direcciones") {
            val uid = usuarioId

            if (uid != null) {
                DireccionesScreen(
                    usuarioId = uid,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            } else {
                Text(
                    text = "Inicia sesión para gestionar tus direcciones."
                )
            }
        }
        composable("confirmarPedido") {
            val uid = usuarioId

            if (uid != null) {
                ConfirmarPedidoScreen(
                    usuarioId = uid,
                    carrito = carrito,
                    productos = productos,
                    cargando = cargandoCarrito ||
                            cargandoProductos ||
                            guardandoCarrito,
                    mensajeError = errorCarrito.ifEmpty {
                        errorProductos
                    },
                    onVerDirecciones = {
                        navController.navigate("direcciones") {
                            launchSingleTop = true
                        }
                    },
                    onVerPedidos = {
                        navController.navigate("pedidos") {
                            popUpTo("menu") {
                                inclusive = false
                            }

                            launchSingleTop = true
                        }
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            } else {
                Text("Inicia sesión para confirmar tu pedido.")
            }
        }

        composable("pedidos") {
            val uid = usuarioId

            if (uid != null) {
                HistorialScreen(
                    usuarioId = uid,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            } else {
                Text("Inicia sesión para consultar tus pedidos.")
            }
        }

        composable("carrito") {
            CarritoScreen(
                carrito = carrito,
                productos = productos,
                cargando = cargandoCarrito || cargandoProductos,
                mensajeError = errorCarrito.ifEmpty {
                    errorProductos
                },
                guardando = guardandoCarrito,

                onAumentar = { productoId ->
                    cambiarCantidad(
                        productoId = productoId,
                        cambio = 1L
                    )
                },

                onDisminuir = { productoId ->
                    cambiarCantidad(
                        productoId = productoId,
                        cambio = -1L
                    )
                },

                onEliminarProducto = { productoId ->
                    eliminarDelCarrito(productoId)
                },
                onContinuar = {
                    navController.navigate("confirmarPedido") {
                        launchSingleTop = true
                    }
                },

                onVolverMenu = {
                    navController.popBackStack()
                }
            )
        }
    }
}
}