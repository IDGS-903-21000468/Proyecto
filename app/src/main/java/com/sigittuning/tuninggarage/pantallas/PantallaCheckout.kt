package com.sigittuning.tuninggarage.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sigittuning.tuninggarage.viewmodels.CreateOrderState
import com.sigittuning.tuninggarage.viewmodels.OrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCheckout(
    onOrderSuccess: () -> Unit,
    viewModel: OrdersViewModel = viewModel()
) {
    val context = LocalContext.current
    val createOrderState by viewModel.createOrderState.collectAsState()

    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf("Tarjeta") }

    val metodosPago = listOf("Tarjeta", "Efectivo", "Transferencia", "PayPal")

    // Manejar éxito
    LaunchedEffect(createOrderState) {
        if (createOrderState is CreateOrderState.Success) {
            kotlinx.coroutines.delay(2000)
            onOrderSuccess()
            viewModel.resetCreateOrderState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FINALIZAR COMPRA",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Información de envío
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "INFORMACIÓN DE ENVÍO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección *") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ciudad,
                            onValueChange = { ciudad = it },
                            label = { Text("Ciudad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = estado,
                            onValueChange = { estado = it },
                            label = { Text("Estado") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = codigoPostal,
                        onValueChange = { codigoPostal = it },
                        label = { Text("Código Postal") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono de contacto *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            }

            // Método de pago
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "MÉTODO DE PAGO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    metodosPago.forEach { metodo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = metodoPago == metodo,
                                onClick = { metodoPago = metodo }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(metodo)
                        }
                    }
                }
            }

            // Mensajes de estado
            when (val state = createOrderState) {
                is CreateOrderState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "¡Pedido creado exitosamente!",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Pedido #${state.order.orderID}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                is CreateOrderState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.message)
                        }
                    }
                }
                else -> {}
            }

            // Botón de confirmar pedido
            Button(
                onClick = {
                    viewModel.createOrder(
                        context = context,
                        direccion = direccion,
                        ciudad = ciudad,
                        estado = estado,
                        codigoPostal = codigoPostal,
                        telefono = telefono,
                        metodoPago = metodoPago
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = createOrderState !is CreateOrderState.Loading &&
                        direccion.isNotBlank() && telefono.isNotBlank()
            ) {
                if (createOrderState is CreateOrderState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONFIRMAR PEDIDO")
                }
            }

            Text(
                "* Campos obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}