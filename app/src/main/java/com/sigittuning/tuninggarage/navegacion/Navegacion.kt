package com.sigittuning.tuninggarage.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sigittuning.tuninggarage.pantallas.*

sealed class Rutas(val ruta: String) {
    object BuscarProductos : Rutas("buscar_productos")
    object AsistenteAVT : Rutas("asistente_avt")
    object GarageSocial : Rutas("garage_social")
    object Marketplace : Rutas("marketplace")
    object MisPedidos : Rutas("mis_pedidos")
}

@Composable
fun NavegacionApp(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Rutas.BuscarProductos.ruta
    ) {
        composable(Rutas.BuscarProductos.ruta) {
            PantallaBuscarProductos()
        }
        composable(Rutas.AsistenteAVT.ruta) {
            PantallaAsistenteAVT()
        }
        composable(Rutas.GarageSocial.ruta) {
            PantallaGarageSocial()
        }
        composable(Rutas.Marketplace.ruta) {
            PantallaMarketplace()
        }
        composable(Rutas.MisPedidos.ruta) {
            PantallaMisPedidos()
        }
    }
}