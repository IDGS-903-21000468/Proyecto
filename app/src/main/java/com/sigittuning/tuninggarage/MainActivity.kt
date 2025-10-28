package com.sigittuning.tuninggarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sigittuning.tuninggarage.componentes.BarraNavegacionInferior
import com.sigittuning.tuninggarage.navegacion.NavegacionApp
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

@Composable
fun AppPrincipal() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BarraNavegacionInferior(navController = navController)
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            NavegacionApp(navController = navController)
        }
    }
}