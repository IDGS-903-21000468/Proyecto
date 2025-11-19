package com.sigittuning.tuninggarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sigittuning.tuninggarage.componentes.BarraNavegacionInferior
import com.sigittuning.tuninggarage.navegacion.NavegacionApp
import com.sigittuning.tuninggarage.navegacion.Rutas
import com.sigittuning.tuninggarage.ui.theme.TuningGarageTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuningGarageTheme {
                AppPrincipal()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPrincipal() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }

    // Observar la ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Mostrar TopBar solo si está logueado y no está en Login
            if (isLoggedIn && rutaActual != Rutas.Login.ruta) {
                TopAppBar(
                    title = { }, // Vacío para que el título lo ponga cada pantalla
                    actions = {
                        // Botón de perfil
                        IconButton(
                            onClick = {
                                navController.navigate(Rutas.Perfil.ruta)
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            // Mostrar barra de navegación solo si está logueado y no está en Login ni Perfil
            if (isLoggedIn &&
                rutaActual != Rutas.Login.ruta &&
                rutaActual != Rutas.Perfil.ruta &&
                !rutaActual.toString().startsWith("comentarios") &&
                !rutaActual.toString().startsWith("chat")
            ) {
                BarraNavegacionInferior(navController = navController)
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            NavegacionApp(
                navController = navController,
                isLoggedIn = isLoggedIn,
                onLoginSuccess = { isLoggedIn = true },
                onLogout = { isLoggedIn = false }
            )
        }
    }
}