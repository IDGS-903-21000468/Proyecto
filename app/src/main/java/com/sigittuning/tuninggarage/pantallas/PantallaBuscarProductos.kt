package com.sigittuning.tuninggarage.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.sigittuning.tuninggarage.data.models.ProductDto
import com.sigittuning.tuninggarage.viewmodels.CategoriesState
import com.sigittuning.tuninggarage.viewmodels.ProductsState
import com.sigittuning.tuninggarage.viewmodels.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBuscarProductos(
    viewModel: ProductsViewModel = viewModel()
) {
    val context = LocalContext.current
    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf<Int?>(null) }

    val productsState by viewModel.productsState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val cartMessage by viewModel.cartMessage.collectAsState()

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        viewModel.loadCategories(context)
        viewModel.loadProducts(context)
    }

    // Snackbar para mensajes del carrito
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cartMessage) {
        cartMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "BUSCAR PRODUCTOS",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de búsqueda
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por pieza, marca, modelo...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        trailingIcon = {
                            if (textoBusqueda.isNotEmpty()) {
                                Row {
                                    IconButton(onClick = {
                                        viewModel.searchProducts(context, textoBusqueda)
                                    }) {
                                        Icon(Icons.Default.Send, contentDescription = "Buscar")
                                    }
                                    IconButton(onClick = {
                                        textoBusqueda = ""
                                        viewModel.loadProducts(context, categoriaSeleccionada)
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categorías
                    when (val state = categoriesState) {
                        is CategoriesState.Success -> {
                            ScrollableTabRow(
                                selectedTabIndex = state.categories.indexOfFirst { it.categoryID == categoriaSeleccionada }.let { if (it == -1) 0 else it + 1 },
                                modifier = Modifier.fillMaxWidth(),
                                edgePadding = 0.dp,
                                containerColor = Color.Transparent,
                                divider = {}
                            ) {
                                // Tab "Todo"
                                Tab(
                                    selected = categoriaSeleccionada == null,
                                    onClick = {
                                        categoriaSeleccionada = null
                                        viewModel.loadProducts(context)
                                    },
                                    text = {
                                        Text(
                                            "Todo",
                                            fontWeight = if (categoriaSeleccionada == null)
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )

                                // Tabs de categorías
                                state.categories.forEach { categoria ->
                                    Tab(
                                        selected = categoriaSeleccionada == categoria.categoryID,
                                        onClick = {
                                            categoriaSeleccionada = categoria.categoryID
                                            viewModel.loadProducts(context, categoria.categoryID)
                                        },
                                        text = {
                                            Text(
                                                categoria.nombre,
                                                fontWeight = if (categoriaSeleccionada == categoria.categoryID)
                                                    FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            // Tabs básicos mientras cargan
                            ScrollableTabRow(
                                selectedTabIndex = 0,
                                modifier = Modifier.fillMaxWidth(),
                                edgePadding = 0.dp,
                                containerColor = Color.Transparent,
                                divider = {}
                            ) {
                                Tab(
                                    selected = true,
                                    onClick = { },
                                    text = { Text("Cargando...") }
                                )
                            }
                        }
                    }
                }
            }

            // Contenido principal
            when (val state = productsState) {
                is ProductsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando productos...")
                        }
                    }
                }

                is ProductsState.Success -> {
                    if (state.products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No se encontraron productos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "${state.products.size} productos encontrados",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.products) { producto ->
                                    TarjetaProducto(producto, viewModel, context)
                                }
                            }
                        }
                    }
                }

                is ProductsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                "Error al cargar productos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadProducts(context) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaProducto(
    producto: ProductDto,
    viewModel: ProductsViewModel,
    context: android.content.Context
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { mostrarDialogo = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del producto
            Box {
                AsyncImage(
                    model = producto.imagenURL,
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                )

                // Badge de stock
                Surface(
                    color = if (producto.disponible) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (producto.disponible) "Disponible" else "Agotado",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Categoría
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = producto.categoriaNombre,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${String.format("%.2f", producto.precio)} MXN",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Diálogo de detalle y agregar al carrito
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(producto.nombre) },
            text = {
                Column {
                    Text("Precio: $${String.format("%.2f", producto.precio)} MXN")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Categoría: ${producto.categoriaNombre}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Stock disponible: ${producto.stock}")
                    if (producto.descripcion != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(producto.descripcion)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addToCart(context, producto.productID)
                        mostrarDialogo = false
                    },
                    enabled = producto.disponible
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar al carrito")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}