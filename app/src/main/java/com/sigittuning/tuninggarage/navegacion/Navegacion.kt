package com.sigittuning.tuninggarage.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sigittuning.tuninggarage.pantallas.*

sealed class Rutas(val ruta: String) {
    object Login : Rutas("login")
    object BuscarProductos : Rutas("buscar_productos")
    object AsistenteAVT : Rutas("asistente_avt")
    object GarageSocial : Rutas("garage_social")
    object Marketplace : Rutas("marketplace")
    object MisPedidos : Rutas("mis_pedidos")
    object Perfil : Rutas("perfil") // NUEVO
    object Comentarios : Rutas("comentarios/{postID}") { // NUEVO
        fun crearRuta(postID: Int) = "comentarios/$postID"
    }
    object Chat : Rutas("chat/{listingID}") { // NUEVO
        fun crearRuta(listingID: Int) = "chat/$listingID"
    }
}

@Composable
fun NavegacionApp(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit // NUEVO
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Rutas.BuscarProductos.ruta else Rutas.Login.ruta
    ) {
        composable(Rutas.Login.ruta) {
            PantallaLogin(
                onLoginSuccess = {
                    onLoginSuccess()
                    navController.navigate(Rutas.BuscarProductos.ruta) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.BuscarProductos.ruta) {
            PantallaBuscarProductos()
        }

        composable(Rutas.AsistenteAVT.ruta) {
            PantallaAsistenteAVT()
        }

        composable(Rutas.GarageSocial.ruta) {
            PantallaGarageSocial(navController) // Pasar navController
        }

        composable(Rutas.Marketplace.ruta) {
            PantallaMarketplace(navController) // Pasar navController
        }

        composable(Rutas.MisPedidos.ruta) {
            PantallaMisPedidos()
        }

        // NUEVO: Perfil
        composable(Rutas.Perfil.ruta) {
            PantallaPerfil(
                onLogout = {
                    onLogout()
                    navController.navigate(Rutas.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // NUEVO: Comentarios
        composable(
            route = Rutas.Comentarios.ruta,
            arguments = listOf(navArgument("postID") { type = NavType.IntType })
        ) { backStackEntry ->
            val postID = backStackEntry.arguments?.getInt("postID") ?: 0
            PantallaComentarios(
                postID = postID,
                onBack = { navController.popBackStack() }
            )
        }

        // NUEVO: Chat
        composable(
            route = Rutas.Chat.ruta,
            arguments = listOf(navArgument("listingID") { type = NavType.IntType })
        ) { backStackEntry ->
            val listingID = backStackEntry.arguments?.getInt("listingID") ?: 0
            PantallaChat(
                listingID = listingID,
                onBack = { navController.popBackStack() }
            )
        }
    }
}