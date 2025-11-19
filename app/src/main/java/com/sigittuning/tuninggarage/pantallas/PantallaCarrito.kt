package com.sigittuning.tuninggarage.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sigittuning.tuninggarage.data.models.CartItemDto
import com.sigittuning.tuninggarage.viewmodels.CartState
import com.sigittuning.tuninggarage.viewmodels.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCarrito(
    onCheckout: () -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val context = LocalContext.current
    val cartState by viewModel.cartState.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadCart(context)
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MI CARRITO",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (cartState is CartState.Success) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Vaciar carrito")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when (val state = cartState) {
            is CartState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando carrito...")
                    }
                }
            }

            is CartState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tu carrito está vacío",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Agrega productos desde la tienda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is CartState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Lista de productos
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.cart.items) { item ->
                            CartItemCard(item, viewModel, context)
                        }
                    }

                    // Footer con total y botón de checkout
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Total (${state.cart.totalItems} items)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "$${String.format("%.2f", state.cart.total)} MXN",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = onCheckout,
                                    modifier = Modifier.height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("COMPRAR")
                                }
                            }
                        }
                    }
                }
            }

            is CartState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Error al cargar carrito",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCart(context) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para vaciar carrito
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("¿Vaciar carrito?") },
            text = { Text("Se eliminarán todos los productos del carrito. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart(context)
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItemDto,
    viewModel: CartViewModel,
    context: android.content.Context
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            AsyncImage(
                model = item.productoImagen,
                contentDescription = item.productoNombre,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.productoNombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${String.format("%.2f", item.precioUnitario)} c/u",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Controles de cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón disminuir
                    FilledTonalIconButton(
                        onClick = {
                            if (item.cantidad > 1) {
                                viewModel.updateCartItem(context, item.cartItemID, item.cantidad - 1)
                            } else {
                                showDeleteDialog = true
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (item.cantidad == 1) Icons.Default.Delete else Icons.Default.Remove,
                            contentDescription = "Disminuir",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Cantidad
                    Text(
                        text = "${item.cantidad}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Botón aumentar
                    FilledTonalIconButton(
                        onClick = {
                            if (item.cantidad < item.stockDisponible) {
                                viewModel.updateCartItem(context, item.cartItemID, item.cantidad + 1)
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = item.cantidad < item.stockDisponible
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (item.cantidad >= item.stockDisponible) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Stock máximo alcanzado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Precio total del item
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", item.subtotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("¿Eliminar producto?") },
            text = { Text("Se eliminará ${item.productoNombre} del carrito.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeFromCart(context, item.cartItemID)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}