package com.sigittuning.tuninggarage.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sigittuning.tuninggarage.navegacion.Rutas

data class ItemNavegacion(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String,
    val tieneBadge: Boolean = false
)

@Composable
fun BarraNavegacionInferior(navController: NavController) {
    val items = listOf(
        ItemNavegacion("Inicio", Icons.Filled.Home, Rutas.BuscarProductos.ruta),
        ItemNavegacion("Buscar", Icons.Filled.Search, Rutas.AsistenteAVT.ruta),
        ItemNavegacion("", Icons.Filled.DirectionsCar, Rutas.GarageSocial.ruta),
        ItemNavegacion("Carrito", Icons.Filled.ShoppingCart, Rutas.Marketplace.ruta, tieneBadge = true),
        ItemNavegacion("Perfil", Icons.Filled.Person, Rutas.MisPedidos.ruta)
    )

    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.tieneBadge) {
                        BadgedBox(badge = { Badge { Text("1") } }) {
                            Icon(item.icono, contentDescription = item.titulo)
                        }
                    } else {
                        Icon(item.icono, contentDescription = item.titulo)
                    }
                },
                label = if (item.titulo.isNotEmpty()) {
                    { Text(item.titulo) }
                } else null,
                selected = rutaActual == item.ruta,
                onClick = {
                    if (rutaActual != item.ruta) {
                        navController.navigate(item.ruta) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}