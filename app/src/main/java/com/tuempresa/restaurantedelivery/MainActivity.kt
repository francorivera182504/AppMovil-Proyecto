package com.tuempresa.restaurantedelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuempresa.restaurantedelivery.data.productosEjemplo
import com.tuempresa.restaurantedelivery.model.Producto
import com.tuempresa.restaurantedelivery.ui.theme.CarritoScreen
import com.tuempresa.restaurantedelivery.ui.theme.InicioScreen
import com.tuempresa.restaurantedelivery.ui.theme.LoginScreen
import com.tuempresa.restaurantedelivery.ui.theme.MenuScreen
import com.tuempresa.restaurantedelivery.ui.theme.ProductoDetalleScreen
import com.tuempresa.restaurantedelivery.ui.theme.RegistroScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AplicacionRestaurante()
            }
        }
    }
}

@Composable
fun AplicacionRestaurante() {
    val navController = rememberNavController()

    var carrito by remember {
        mutableStateOf<List<Producto>>(emptyList())
    }

    NavHost(
        navController = navController,
        startDestination = "inicio"
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
                    }
                },
                onVolverInicio = {
                    navController.popBackStack()
                }
            )
        }

        composable("menu") {
            MenuScreen(
                onSeleccionarProducto = { producto ->
                    navController.navigate(
                        "detalle/${producto.id}"
                    )
                },
                onAgregarProducto = { producto ->
                    carrito = carrito + producto
                },
                onVerCarrito = {
                    navController.navigate("carrito")
                }
            )
        }

        composable(
            route = "detalle/{productoId}",
            arguments = listOf(
                navArgument("productoId") {
                    type = NavType.IntType
                }
            )
        ) { entrada ->

            val productoId =
                entrada.arguments?.getInt("productoId")

            val producto =
                productosEjemplo.firstOrNull {
                    it.id == productoId
                }

            if (producto != null) {
                ProductoDetalleScreen(
                    producto = producto,
                    onAgregarProducto = {
                        carrito = carrito + producto
                        navController.popBackStack()
                    },
                    onVolverMenu = {
                        navController.popBackStack()
                    }
                )
            } else {
                Text(text = "Producto no encontrado")
            }
        }

        composable("carrito") {
            CarritoScreen(
                carrito = carrito,
                onEliminarProducto = { producto ->
                    carrito = carrito - producto
                },
                onVolverMenu = {
                    navController.popBackStack()
                }
            )
        }
    }
}